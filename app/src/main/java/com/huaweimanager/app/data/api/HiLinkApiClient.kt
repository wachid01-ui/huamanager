package com.huaweimanager.app.data.api

import android.util.Log
import com.huaweimanager.app.data.crypto.HuaweiCrypto
import com.huaweimanager.app.data.model.DeviceInfo
import com.huaweimanager.app.data.model.NetworkStatus
import com.huaweimanager.app.data.model.SignalInfo
import com.huaweimanager.app.data.model.SmsMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class HiLinkApiClient(var baseUrl: String = "http://192.168.8.1") {

    companion object {
        private const val TAG = "HiLinkApiClient"
        private val XML_MEDIA_TYPE = "application/xml; charset=UTF-8".toMediaType()
    }

    private var currentToken: String = ""
    private var currentSession: String = ""
    private val sessionCookies = java.util.concurrent.ConcurrentHashMap<String, String>()
    var isLoggedIn: Boolean = false
        private set

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()

            // Attach verification token if available
            if (currentToken.isNotEmpty()) {
                requestBuilder.header("__RequestVerificationToken", currentToken)
            }

            // Attach SessionID cookie properly without duplicate prefix
            if (sessionCookies.isNotEmpty()) {
                val cookieVal = sessionCookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
                requestBuilder.header("Cookie", cookieVal)
            } else if (currentSession.isNotEmpty()) {
                val cleanSession = currentSession.trim().removePrefix("SessionID=").removeSuffix(";")
                requestBuilder.header("Cookie", "SessionID=$cleanSession")
            }

            requestBuilder.header("X-Requested-With", "XMLHttpRequest")
            requestBuilder.header("Accept", "*/*")

            val response = chain.proceed(requestBuilder.build())

            // Update verification token if router returns a fresh one in headers
            val newToken = response.header("__RequestVerificationToken")?.takeIf { it.isNotEmpty() }
                ?: response.header("__RequestVerificationTokenone")?.takeIf { it.isNotEmpty() }
                ?: response.header("__RequestVerificationTokentwo")?.takeIf { it.isNotEmpty() }
            if (!newToken.isNullOrEmpty()) {
                currentToken = newToken
            }

            // Track SessionID cookie from Set-Cookie headers if provided
            val setCookies = response.headers("Set-Cookie")
            for (cookieHeader in setCookies) {
                val parts = cookieHeader.split(";")
                if (parts.isNotEmpty()) {
                    val nameValue = parts[0].trim()
                    val eqIdx = nameValue.indexOf('=')
                    if (eqIdx > 0) {
                        val name = nameValue.substring(0, eqIdx).trim()
                        val value = nameValue.substring(eqIdx + 1).trim()
                        sessionCookies[name] = value
                        if (name.equals("SessionID", ignoreCase = true)) {
                            currentSession = value
                        }
                    }
                }
            }

            response
        }
        .build()

    fun clearSession() {
        isLoggedIn = false
        currentSession = ""
        currentToken = ""
        sessionCookies.clear()
    }

    fun updateBaseUrl(ip: String) {
        var cleanIp = ip.trim()
        if (!cleanIp.startsWith("http://") && !cleanIp.startsWith("https://")) {
            cleanIp = "http://$cleanIp"
        }
        if (cleanIp.endsWith("/")) {
            cleanIp = cleanIp.removeSuffix("/")
        }
        baseUrl = cleanIp
    }

    /**
     * Initializes session and retrieves verification tokens from the router
     */
    suspend fun refreshTokens(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/webserver/SesTokInfo")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful && body.contains("<response>")) {
                val ses = extractXmlTag(body, "SesInfo")
                val tok = extractXmlTag(body, "TokInfo")
                if (ses.isNotEmpty()) {
                    val cleanSes = ses.removePrefix("SessionID=").removeSuffix(";")
                    currentSession = cleanSes
                    sessionCookies["SessionID"] = cleanSes
                }
                if (tok.isNotEmpty()) currentToken = tok
                return@withContext true
            }

            // Fallback for older routers: /api/webserver/token
            val fallbackRequest = Request.Builder()
                .url("$baseUrl/api/webserver/token")
                .get()
                .build()
            val fallbackResponse = client.newCall(fallbackRequest).execute()
            val fallbackBody = fallbackResponse.body?.string() ?: ""
            val token = extractXmlTag(fallbackBody, "token")
            if (token.isNotEmpty()) {
                currentToken = token
                return@withContext true
            }

            false
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing tokens: ${e.message}")
            false
        }
    }

    /**
     * Test reachability to the router
     */
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val tokenRefreshed = refreshTokens()
            if (!tokenRefreshed) {
                // Try simple ping to monitoring status
                val request = Request.Builder()
                    .url("$baseUrl/api/monitoring/status")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) return@withContext Result.success(true)
                return@withContext Result.failure(IOException("Tidak dapat terhubung ke modem ($baseUrl)"))
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun cleanSecret(input: String): String {
        return input.trim()
            .replace("\u200B", "") // zero-width space
            .replace("\u200C", "") // zero-width non-joiner
            .replace("\u200D", "") // zero-width joiner
            .replace("\uFEFF", "") // zero-width no-break space (BOM)
            .replace("\u00A0", "") // non-breaking space
    }

    /**
     * Authenticate with the router supporting both modern SCRAM-SHA-256 and standard HiLink.
     * Includes automatic handshake recovery so login succeeds reliably on first attempt.
     */
    suspend fun login(username: String = "admin", password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim()
        val cleanPassword = cleanSecret(password)

        if (cleanPassword.isEmpty()) {
            return@withContext Result.failure<Boolean>(Exception("Password tidak boleh kosong"))
        }

        var lastError: Exception? = null
        for (attempt in 1..2) {
            val result = executeLoginHandshake(cleanUsername, cleanPassword)
            if (result.isSuccess) {
                return@withContext result
            }

            val err = result.exceptionOrNull() as? Exception ?: Exception("Login gagal")
            lastError = err

            // If the failure was an explicit incorrect password, don't retry (save retry lockout attempts)
            val msg = err.message ?: ""
            if (msg.contains("Password admin modem salah") || msg.contains("108002")) {
                return@withContext result
            }

            // Brief pause before clean retry
            if (attempt == 1) {
                delay(350)
            }
        }

        return@withContext Result.failure<Boolean>(lastError ?: Exception("Login gagal"))
    }

    private suspend fun executeLoginHandshake(cleanUsername: String, cleanPassword: String): Result<Boolean> {
        try {
            // Step 0: Reset session state and retrieve a fresh session token pair for login
            clearSession()
            val tokenReady = refreshTokens()
            if (!tokenReady) {
                return Result.failure<Boolean>(Exception("Gagal mengambil token sesi modem. Pastikan HP terhubung ke WiFi router."))
            }

            // Step 1: Try modern SCRAM challenge_login first (B311, B312, B535, Orbit Star, SoyeaLink)
            val clientNonce = generateRandomHex(32) // 32 bytes = 64 hex characters
            val challengeXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <username>$cleanUsername</username>
                    <firstnonce>$clientNonce</firstnonce>
                    <mode>1</mode>
                </request>
            """.trimIndent()

            val challengeRequest = Request.Builder()
                .url("$baseUrl/api/user/challenge_login")
                .post(challengeXml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            val challengeResponse = client.newCall(challengeRequest).execute()
            val challengeBody = challengeResponse.body?.string() ?: ""

            // Capture verification token if returned in challenge response headers
            val chalToken = challengeResponse.header("__RequestVerificationToken")?.takeIf { it.isNotEmpty() }
                ?: challengeResponse.header("__RequestVerificationTokenone")?.takeIf { it.isNotEmpty() }
                ?: challengeResponse.header("__RequestVerificationTokentwo")?.takeIf { it.isNotEmpty() }
            if (!chalToken.isNullOrEmpty()) {
                currentToken = chalToken
            }

            if (challengeResponse.isSuccessful && challengeBody.contains("<salt>")) {
                val salt = extractXmlTag(challengeBody, "salt")
                val iterations = extractXmlTag(challengeBody, "iterations").toIntOrNull() ?: 100
                val serverNonce = extractXmlTag(challengeBody, "servernonce")
                
                // Huawei SCRAM AuthMessage: clientNonce,serverNonce,serverNonce
                val authMsg = "$clientNonce,$serverNonce,$serverNonce"
                val clientProof = HuaweiCrypto.computeScramClientProof(cleanPassword, salt, iterations, authMsg)

                if (clientProof.isNotEmpty()) {
                    val authXml = """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <request>
                            <clientproof>$clientProof</clientproof>
                            <finalnonce>$serverNonce</finalnonce>
                        </request>
                    """.trimIndent()

                    val authRequest = Request.Builder()
                        .url("$baseUrl/api/user/authentication_login")
                        .post(authXml.toRequestBody(XML_MEDIA_TYPE))
                        .build()

                    val authResponse = client.newCall(authRequest).execute()
                    val authBody = authResponse.body?.string() ?: ""

                    val authToken = authResponse.header("__RequestVerificationToken")?.takeIf { it.isNotEmpty() }
                        ?: authResponse.header("__RequestVerificationTokenone")?.takeIf { it.isNotEmpty() }
                        ?: authResponse.header("__RequestVerificationTokentwo")?.takeIf { it.isNotEmpty() }
                    if (!authToken.isNullOrEmpty()) {
                        currentToken = authToken
                    }

                    if (authBody.contains("<response>OK</response>")) {
                        isLoggedIn = true
                        return Result.success(true)
                    }

                    val errCode = extractXmlTag(authBody, "code")
                    if (errCode == "108003") {
                        // User is already logged in
                        isLoggedIn = true
                        return Result.success(true)
                    }

                    // If error indicates password mismatch, try standard RFC 5802 SCRAM variant
                    if (errCode == "108002" || errCode == "108006" || errCode == "108007") {
                        val stdProof = HuaweiCrypto.computeStandardScramClientProof(cleanPassword, salt, iterations, authMsg)
                        if (stdProof.isNotEmpty() && stdProof != clientProof) {
                            val stdXml = """
                                <?xml version="1.0" encoding="UTF-8"?>
                                <request>
                                    <clientproof>$stdProof</clientproof>
                                    <finalnonce>$serverNonce</finalnonce>
                                </request>
                            """.trimIndent()

                            val stdReq = Request.Builder()
                                .url("$baseUrl/api/user/authentication_login")
                                .post(stdXml.toRequestBody(XML_MEDIA_TYPE))
                                .build()

                            val stdRes = client.newCall(stdReq).execute()
                            val stdBody = stdRes.body?.string() ?: ""
                            if (stdBody.contains("<response>OK</response>") || extractXmlTag(stdBody, "code") == "108003") {
                                isLoggedIn = true
                                return Result.success(true)
                            }
                        }

                        val errMsg = parseHuaweiErrorMessage(authBody, "Password admin modem salah.")
                        return Result.failure<Boolean>(Exception(errMsg))
                    }
                }
            }

            // Step 2: Standard HiLink login (B310, B315, E5577, E5573)
            refreshTokens()
            val passwordHash = HuaweiCrypto.createHiLinkPassword(cleanUsername, cleanPassword, currentToken)
            val loginXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <Username>$cleanUsername</Username>
                    <Password>$passwordHash</Password>
                    <password_type>4</password_type>
                </request>
            """.trimIndent()

            val loginRequest = Request.Builder()
                .url("$baseUrl/api/user/login")
                .post(loginXml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            val loginResponse = client.newCall(loginRequest).execute()
            val loginBody = loginResponse.body?.string() ?: ""

            if (loginBody.contains("<response>OK</response>")) {
                isLoggedIn = true
                return Result.success(true)
            }

            val loginErrCode = extractXmlTag(loginBody, "code")
            if (loginErrCode == "108003") {
                isLoggedIn = true
                return Result.success(true)
            }

            // Step 3: Fallback for older routers (password_type 0)
            if (loginErrCode == "100002" || loginErrCode == "100003") {
                refreshTokens()
                val plainBase64Pw = android.util.Base64.encodeToString(cleanPassword.toByteArray(), android.util.Base64.NO_WRAP)
                val fallbackXml = """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <request>
                        <Username>$cleanUsername</Username>
                        <Password>$plainBase64Pw</Password>
                        <password_type>0</password_type>
                    </request>
                """.trimIndent()

                val fallbackRequest = Request.Builder()
                    .url("$baseUrl/api/user/login")
                    .post(fallbackXml.toRequestBody(XML_MEDIA_TYPE))
                    .build()

                val fallbackResponse = client.newCall(fallbackRequest).execute()
                val fallbackBody = fallbackResponse.body?.string() ?: ""
                if (fallbackBody.contains("<response>OK</response>") || extractXmlTag(fallbackBody, "code") == "108003") {
                    isLoggedIn = true
                    return Result.success(true)
                }
            }

            val friendlyMsg = parseHuaweiErrorMessage(loginBody, "Login gagal.")
            return Result.failure<Boolean>(Exception(friendlyMsg))
        } catch (e: Exception) {
            val netError = e.message ?: "Koneksi terputus"
            return Result.failure<Boolean>(Exception("Gagal menghubungi modem ($netError). Pastikan HP terhubung ke WiFi router."))
        }
    }

    /**
     * Terminate admin session on the router and clear local session state
     */
    suspend fun logout(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val logoutXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <Logout>1</Logout>
                </request>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/api/user/logout")
                .post(logoutXml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            client.newCall(request).execute()
        } catch (e: Exception) {
            Log.w(TAG, "Logout error: ${e.message}")
        } finally {
            clearSession()
        }
        Result.success(true)
    }

    /**
     * Get Signal Parameters (RSRP, RSRQ, SINR, RSSI, PCI, CellID, Band)
     */
    suspend fun getSignalInfo(): Result<SignalInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/device/signal")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful || !body.contains("<response>")) {
                return@withContext Result.failure(IOException("Gagal mengambil data sinyal"))
            }

            val pci = extractXmlTag(body, "pci")
            val cellId = extractXmlTag(body, "cell_id").ifEmpty { extractXmlTag(body, "cellid") }
            val rsrq = extractXmlTag(body, "rsrq")
            val rsrp = extractXmlTag(body, "rsrp")
            val rssi = extractXmlTag(body, "rssi")
            val sinr = extractXmlTag(body, "sinr")
            val band = extractXmlTag(body, "band")
            val dlFreq = extractXmlTag(body, "dlfrequency")
            val mode = extractXmlTag(body, "mode")
            val plmn = extractXmlTag(body, "plmn")
            val tac = extractXmlTag(body, "tac")
            val earfcn = extractXmlTag(body, "earfcn")
            val enodebId = extractXmlTag(body, "enodeb_id").ifEmpty { extractXmlTag(body, "enodebid") }

            Result.success(
                SignalInfo(
                    pci = pci.ifEmpty { "-" },
                    cellId = cellId.ifEmpty { "-" },
                    rsrq = rsrq.ifEmpty { "-" },
                    rsrp = rsrp.ifEmpty { "-" },
                    rssi = rssi.ifEmpty { "-" },
                    sinr = sinr.ifEmpty { "-" },
                    band = band.ifEmpty { "-" },
                    dlFrequency = dlFreq.ifEmpty { "-" },
                    mode = mode.ifEmpty { "-" },
                    plmn = plmn.ifEmpty { "-" },
                    tac = tac.ifEmpty { "-" },
                    earfcn = earfcn.ifEmpty { "-" },
                    enodebId = enodebId.ifEmpty { "-" }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Connection & Network Status
     */
    suspend fun getNetworkStatus(): Result<NetworkStatus> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/monitoring/status")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful || !body.contains("<response>")) {
                return@withContext Result.failure(IOException("Gagal mengambil status jaringan"))
            }

            val connStatus = extractXmlTag(body, "ConnectionStatus")
            val signalStrength = extractXmlTag(body, "SignalStrength").toIntOrNull() ?: 0
            val netType = extractXmlTag(body, "CurrentNetworkType")
            val simStatus = extractXmlTag(body, "SimStatus")
            val primaryDns = extractXmlTag(body, "PrimaryDns")
            val secondaryDns = extractXmlTag(body, "SecondaryDns")

            // Try to get network operator name
            var operatorName = ""
            try {
                val plmnRequest = Request.Builder().url("$baseUrl/api/net/current-plmn").get().build()
                val plmnRes = client.newCall(plmnRequest).execute()
                val plmnBody = plmnRes.body?.string() ?: ""
                operatorName = extractXmlTag(plmnBody, "FullName")
                if (operatorName.isEmpty()) {
                    operatorName = extractXmlTag(plmnBody, "ShortName")
                }
            } catch (e: Exception) {
                // Ignore operator fetch error
            }

            Result.success(
                NetworkStatus(
                    connectionStatus = connStatus,
                    signalStrength = signalStrength,
                    currentNetworkType = netType,
                    currentNetworkName = operatorName.ifEmpty { "Terhubung" },
                    simStatus = simStatus,
                    isConnected = connStatus == "901" || connStatus == "900",
                    primaryDns = primaryDns,
                    secondaryDns = secondaryDns
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Device Hardware & Software info
     */
    suspend fun getDeviceInfo(): Result<DeviceInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/device/information")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful || !body.contains("<response>")) {
                return@withContext Result.failure(IOException("Gagal mengambil info perangkat"))
            }

            Result.success(
                DeviceInfo(
                    deviceName = extractXmlTag(body, "DeviceName").ifEmpty { "Huawei Router" },
                    hardwareVersion = extractXmlTag(body, "HardwareVersion"),
                    softwareVersion = extractXmlTag(body, "SoftwareVersion"),
                    webUIVersion = extractXmlTag(body, "WebUIVersion"),
                    imei = extractXmlTag(body, "Imei"),
                    imsi = extractXmlTag(body, "Imsi"),
                    macAddress1 = extractXmlTag(body, "MacAddress1"),
                    serialNumber = extractXmlTag(body, "SerialNumber")
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lock Band LTE / Net Mode
     * NetworkMode:
     *   "00" = Auto
     *   "03" = 4G LTE Only
     * LTEBand: Hexadecimal mask string (e.g., "4" for B3, "8000000000" for B40, "7FFFFFFFFFFFFFFF" for all)
     */
    suspend fun setLteBand(lteBandHex: String, networkMode: String = "03"): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            refreshTokens()

            val xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <NetworkMode>$networkMode</NetworkMode>
                    <NetworkBand>3FFFFFFF</NetworkBand>
                    <LTEBand>$lteBandHex</LTEBand>
                </request>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/api/net/net-mode")
                .post(xml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (body.contains("<response>OK</response>")) {
                Result.success(true)
            } else {
                val err = extractXmlTag(body, "code")
                Result.failure(Exception("Gagal mengunci band. Kode router: $err (Pastikan Anda sudah login)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Current Network Mode and LTE Band Mask
     */
    suspend fun getCurrentBand(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/net/net-mode")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (response.isSuccessful && body.contains("<response>")) {
                val lteBand = extractXmlTag(body, "LTEBand")
                Result.success(lteBand)
            } else {
                Result.failure(IOException("Gagal mengambil info band aktif"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Reboot Router
     */
    suspend fun rebootRouter(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            refreshTokens()
            val xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <Control>1</Control>
                </request>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/api/device/control")
                .post(xml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (body.contains("<response>OK</response>")) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal restart router. Pastikan sudah login."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mobile Data Switch (enable: 1, disable: 0)
     */
    suspend fun setMobileData(enabled: Boolean): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            refreshTokens()
            val valSwitch = if (enabled) "1" else "0"
            val xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <dataswitch>$valSwitch</dataswitch>
                </request>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/api/dialup/mobile-dataswitch")
                .post(xml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (body.contains("<response>OK</response>")) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal mengubah status data seluler"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get SMS List
     */
    suspend fun getSmsList(page: Int = 1, count: Int = 20): Result<List<SmsMessage>> = withContext(Dispatchers.IO) {
        try {
            refreshTokens()
            val xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <pageIndex>$page</pageIndex>
                    <readCount>$count</readCount>
                    <boxType>1</boxType>
                    <sortType>0</sortType>
                    <ascending>0</ascending>
                    <unreadPreferred>0</unreadPreferred>
                </request>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/api/sms/sms-list")
                .post(xml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!response.isSuccessful || !body.contains("<Messages>")) {
                return@withContext Result.success(emptyList())
            }

            val messages = ArrayList<SmsMessage>()
            val pattern = Pattern.compile("<Message>(.*?)</Message>", Pattern.DOTALL)
            val matcher = pattern.matcher(body)

            while (matcher.find()) {
                val msgXml = matcher.group(1) ?: ""
                val index = extractXmlTag(msgXml, "Index").toIntOrNull() ?: 0
                val phone = extractXmlTag(msgXml, "Phone")
                val content = extractXmlTag(msgXml, "Content")
                val date = extractXmlTag(msgXml, "Date")
                val smstat = extractXmlTag(msgXml, "Smstat")

                messages.add(
                    SmsMessage(
                        index = index,
                        phone = phone,
                        content = content,
                        date = date,
                        isRead = smstat != "0"
                    )
                )
            }

            Result.success(messages)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send SMS
     */
    suspend fun sendSms(phone: String, content: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            refreshTokens()
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val dateStr = sdf.format(Date())

            val xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <Index>-1</Index>
                    <Phones><Phone>$phone</Phone></Phones>
                    <Sca></Sca>
                    <Content>$content</Content>
                    <Length>${content.length}</Length>
                    <Reserved>1</Reserved>
                    <Date>$dateStr</Date>
                </request>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/api/sms/send-sms")
                .post(xml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (body.contains("<response>OK</response>")) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal mengirim SMS"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete SMS by index
     */
    suspend fun deleteSms(index: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            refreshTokens()
            val xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <Index>$index</Index>
                </request>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/api/sms/delete-sms")
                .post(xml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (body.contains("<response>OK</response>")) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal menghapus SMS"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send USSD code (e.g., *888# or *363#) and poll for answer
     */
    suspend fun sendUssd(code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            refreshTokens()
            val xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <content>$code</content>
                    <codeType>CodeType</codeType>
                    <timeout></timeout>
                </request>
            """.trimIndent()

            val request = Request.Builder()
                .url("$baseUrl/api/ussd/send")
                .post(xml.toRequestBody(XML_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""

            if (!body.contains("<response>OK</response>")) {
                return@withContext Result.failure(Exception("Gagal mengirim perintah USSD"))
            }

            // Poll for USSD response (up to 5 attempts)
            for (i in 0 until 5) {
                kotlinx.coroutines.delay(1500)
                val getReq = Request.Builder().url("$baseUrl/api/ussd/get").get().build()
                val getRes = client.newCall(getReq).execute()
                val getBody = getRes.body?.string() ?: ""
                val ussdContent = extractXmlTag(getBody, "content")
                if (ussdContent.isNotEmpty()) {
                    return@withContext Result.success(ussdContent)
                }
            }

            Result.failure(Exception("USSD Timeout: Tidak ada balasan dari operator"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Safe regex XML value extractor
     */
    private fun extractXmlTag(xml: String, tag: String): String {
        val pattern = Pattern.compile("<$tag>(.*?)</$tag>", Pattern.DOTALL)
        val matcher = pattern.matcher(xml)
        return if (matcher.find()) {
            matcher.group(1)?.trim() ?: ""
        } else {
            ""
        }
    }

    private fun generateRandomHex(byteCount: Int): String {
        val bytes = ByteArray(byteCount)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun parseHuaweiErrorMessage(xml: String, defaultMsg: String): String {
        val code = extractXmlTag(xml, "code")
        return when (code) {
            "108001" -> "Username atau pengguna tidak ditemukan (Pastikan username benar, default: admin)."
            "108002" -> "Password admin modem salah. Silakan periksa kembali password WebUI modem Anda (gunakan ikon mata untuk mengecek karakter dan huruf besar-kecil)."
            "108003" -> "Sesi login masih aktif pada perangkat lain."
            "108006", "108007" -> "Password admin salah atau batas percobaan terlampaui. Tunggu beberapa menit sebelum mencoba lagi."
            "100002" -> "Perintah login tidak didukung oleh versi firmware modem ini."
            "100003" -> "Akses ditolak (Token sesi kedaluwarsa). Silakan coba lagi."
            "100004" -> "Sistem modem sedang sibuk. Silakan tunggu beberapa saat."
            "100005" -> "Format request data tidak valid."
            "100006" -> "Parameter modem tidak valid."
            "100008" -> "Modem memerlukan verifikasi ulang."
            else -> if (code.isNotEmpty()) "$defaultMsg (Kode Error: $code)" else defaultMsg
        }
    }
}

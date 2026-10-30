package com.huaweimanager.app.data.api

import android.util.Log
import com.huaweimanager.app.data.crypto.HuaweiCrypto
import com.huaweimanager.app.data.model.DeviceInfo
import com.huaweimanager.app.data.model.NetworkStatus
import com.huaweimanager.app.data.model.SignalInfo
import com.huaweimanager.app.data.model.SmsMessage
import kotlinx.coroutines.Dispatchers
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
    var isLoggedIn: Boolean = false
        private set

    // In-memory cookie jar to retain session cookies across requests
    private val cookieStore = HashMap<String, MutableList<Cookie>>()
    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies.toMutableList()
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: ArrayList()
        }
    }

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
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

            // Attach SessionID cookie if available
            if (currentSession.isNotEmpty()) {
                requestBuilder.addHeader("Cookie", "SessionID=$currentSession")
            }

            requestBuilder.header("X-Requested-With", "XMLHttpRequest")
            requestBuilder.header("Accept", "*/*")

            val response = chain.proceed(requestBuilder.build())

            // Update verification token if router returns a fresh one in headers
            val newToken = response.header("__RequestVerificationToken")
                ?: response.header("__RequestVerificationTokenone")
            if (!newToken.isNullOrEmpty()) {
                currentToken = newToken
            }

            response
        }
        .build()

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
                if (ses.isNotEmpty()) currentSession = ses
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

    /**
     * Authenticate with the router
     */
    suspend fun login(username: String = "admin", password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            refreshTokens()

            // 1. Try modern challenge_login first (B311, B312, B535, Orbit Star)
            val challengeRequest = Request.Builder()
                .url("$baseUrl/api/user/challenge_login")
                .post("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <request>
                        <username>$username</username>
                        <firstnonce>c167d402123456781234567812345678</firstnonce>
                        <mode>1</mode>
                    </request>
                """.trimIndent().toRequestBody(XML_MEDIA_TYPE))
                .build()

            val challengeResponse = client.newCall(challengeRequest).execute()
            val challengeBody = challengeResponse.body?.string() ?: ""

            if (challengeResponse.isSuccessful && challengeBody.contains("<salt>")) {
                val salt = extractXmlTag(challengeBody, "salt")
                val iterations = extractXmlTag(challengeBody, "iterations").toIntOrNull() ?: 100
                val serverNonce = extractXmlTag(challengeBody, "servernonce")
                val authMsg = "c167d402123456781234567812345678,$serverNonce,$serverNonce"
                val clientProof = HuaweiCrypto.computeScramClientProof(password, salt, iterations, authMsg)

                if (clientProof.isNotEmpty()) {
                    val authRequest = Request.Builder()
                        .url("$baseUrl/api/user/authentication_login")
                        .post("""
                            <?xml version="1.0" encoding="UTF-8"?>
                            <request>
                                <clientproof>$clientProof</clientproof>
                                <finalnonce>$serverNonce</finalnonce>
                            </request>
                        """.trimIndent().toRequestBody(XML_MEDIA_TYPE))
                        .build()

                    val authResponse = client.newCall(authRequest).execute()
                    val authBody = authResponse.body?.string() ?: ""
                    if (authBody.contains("<response>OK</response>")) {
                        isLoggedIn = true
                        return@withContext Result.success(true)
                    }
                }
            }

            // 2. Standard HiLink login (B310, B315, E5577, E5573)
            refreshTokens()
            val passwordHash = HuaweiCrypto.createHiLinkPassword(username, password, currentToken)
            val loginXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <request>
                    <Username>$username</Username>
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
                return@withContext Result.success(true)
            } else {
                val errCode = extractXmlTag(loginBody, "code")
                return@withContext Result.failure(Exception("Login gagal. Kode error router: $errCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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
            val cellId = extractXmlTag(body, "cell_id")
            val rsrq = extractXmlTag(body, "rsrq")
            val rsrp = extractXmlTag(body, "rsrp")
            val rssi = extractXmlTag(body, "rssi")
            val sinr = extractXmlTag(body, "sinr")
            val band = extractXmlTag(body, "band")
            val dlFreq = extractXmlTag(body, "dlfrequency")
            val mode = extractXmlTag(body, "mode")
            val plmn = extractXmlTag(body, "plmn")

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
                    plmn = plmn.ifEmpty { "-" }
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
}

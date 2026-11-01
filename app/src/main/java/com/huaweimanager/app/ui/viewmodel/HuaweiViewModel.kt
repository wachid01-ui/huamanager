package com.huaweimanager.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.huaweimanager.app.data.api.HiLinkApiClient
import com.huaweimanager.app.data.model.DeviceInfo
import com.huaweimanager.app.data.model.NetworkStatus
import com.huaweimanager.app.data.model.SignalInfo
import com.huaweimanager.app.data.model.SmsMessage
import com.huaweimanager.app.ui.i18n.AppLanguage
import com.huaweimanager.app.ui.theme.AppThemeMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HuaweiViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("huawei_manager_prefs", Context.MODE_PRIVATE)

    val apiClient = HiLinkApiClient(
        baseUrl = prefs.getString("router_ip", "http://192.168.8.1") ?: "http://192.168.8.1"
    )

    private val _routerIp = MutableStateFlow(prefs.getString("router_ip", "192.168.8.1") ?: "192.168.8.1")
    val routerIp: StateFlow<String> = _routerIp.asStateFlow()

    private val _routerUsername = MutableStateFlow(prefs.getString("router_username", "admin") ?: "admin")
    val routerUsername: StateFlow<String> = _routerUsername.asStateFlow()

    private val _savedPassword = MutableStateFlow(prefs.getString("saved_password", "") ?: "")
    val savedPassword: StateFlow<String> = _savedPassword.asStateFlow()

    private val savedThemeModeStr = prefs.getString("app_theme", AppThemeMode.DARK_CYAN.name) ?: AppThemeMode.DARK_CYAN.name
    private val initialThemeMode = try {
        AppThemeMode.valueOf(savedThemeModeStr)
    } catch (e: Exception) {
        AppThemeMode.DARK_CYAN
    }

    private val _appTheme = MutableStateFlow(initialThemeMode)
    val appTheme: StateFlow<AppThemeMode> = _appTheme.asStateFlow()

    fun setAppTheme(theme: AppThemeMode) {
        _appTheme.value = theme
        prefs.edit().putString("app_theme", theme.name).apply()
    }

    private val savedLangStr = prefs.getString("app_language", AppLanguage.INDONESIAN.name) ?: AppLanguage.INDONESIAN.name
    private val initialLang = try {
        AppLanguage.valueOf(savedLangStr)
    } catch (e: Exception) {
        AppLanguage.INDONESIAN
    }

    private val _appLanguage = MutableStateFlow(initialLang)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    fun setAppLanguage(lang: AppLanguage) {
        _appLanguage.value = lang
        prefs.edit().putString("app_language", lang.name).apply()
    }

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _signalInfo = MutableStateFlow(SignalInfo())
    val signalInfo: StateFlow<SignalInfo> = _signalInfo.asStateFlow()

    private val _networkStatus = MutableStateFlow(NetworkStatus())
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()

    private val _smsList = MutableStateFlow<List<SmsMessage>>(emptyList())
    val smsList: StateFlow<List<SmsMessage>> = _smsList.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isAutoRefresh = MutableStateFlow(true)
    val isAutoRefresh: StateFlow<Boolean> = _isAutoRefresh.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _ussdResponse = MutableStateFlow<String?>(null)
    val ussdResponse: StateFlow<String?> = _ussdResponse.asStateFlow()

    private var pollingJob: Job? = null

    init {
        testConnectionAndStart()
    }

    fun setRouterIp(newIp: String) {
        val cleanIp = newIp.replace("http://", "").replace("https://", "").trim()
        _routerIp.value = cleanIp
        prefs.edit().putString("router_ip", cleanIp).apply()
        apiClient.updateBaseUrl(cleanIp)
        testConnectionAndStart()
    }

    fun testConnectionAndStart() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val conn = apiClient.testConnection()
            _isConnected.value = conn.isSuccess
            if (conn.isSuccess) {
                // Auto-login if previously logged in and credentials saved
                val wasLoggedIn = prefs.getBoolean("is_logged_in", false)
                val savedPw = prefs.getString("saved_password", "") ?: ""
                val savedUser = prefs.getString("router_username", "admin") ?: "admin"

                if (wasLoggedIn && savedPw.isNotEmpty()) {
                    val loginRes = apiClient.login(savedUser, savedPw)
                    if (loginRes.isSuccess) {
                        _isLoggedIn.value = true
                    }
                }

                refreshSignalAndStatus()
                loadDeviceInfo()
                if (_isAutoRefresh.value) {
                    startAutoRefresh()
                }
            } else {
                _toastMessage.value = "Tidak dapat terhubung ke modem di ${_routerIp.value}"
            }
            _isRefreshing.value = false
        }
    }

    fun toggleAutoRefresh() {
        val next = !_isAutoRefresh.value
        _isAutoRefresh.value = next
        if (next) {
            startAutoRefresh()
        } else {
            pollingJob?.cancel()
            pollingJob = null
        }
    }

    private fun startAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                refreshSignalAndStatus()
                delay(2500)
            }
        }
    }

    suspend fun refreshSignalAndStatus() {
        val signalResult = apiClient.getSignalInfo()
        if (signalResult.isSuccess) {
            _signalInfo.value = signalResult.getOrThrow()
            _isConnected.value = true
        }

        val netResult = apiClient.getNetworkStatus()
        if (netResult.isSuccess) {
            _networkStatus.value = netResult.getOrThrow()
        }
    }

    fun manualRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            refreshSignalAndStatus()
            _isRefreshing.value = false
        }
    }

    fun loadDeviceInfo() {
        viewModelScope.launch {
            val res = apiClient.getDeviceInfo()
            if (res.isSuccess) {
                _deviceInfo.value = res.getOrThrow()
            }
        }
    }

    fun setRouterUsername(username: String) {
        val clean = username.trim()
        _routerUsername.value = clean
        prefs.edit().putString("router_username", clean).apply()
    }

    fun login(password: String, username: String = _routerUsername.value, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            // Temporarily pause auto-refresh polling so it doesn't race with the login handshake
            pollingJob?.cancel()
            pollingJob = null

            _isRefreshing.value = true
            _loginError.value = null
            val cleanUsername = username.trim()
            val cleanPassword = apiClient.cleanSecret(password)
            setRouterUsername(cleanUsername)
            val result = apiClient.login(cleanUsername, cleanPassword)
            _isRefreshing.value = false
            if (result.isSuccess) {
                _isLoggedIn.value = true
                _loginError.value = null
                _savedPassword.value = cleanPassword
                prefs.edit()
                    .putString("router_username", cleanUsername)
                    .putString("saved_password", cleanPassword)
                    .putBoolean("is_logged_in", true)
                    .apply()
                _toastMessage.value = "Login berhasil!"
                loadDeviceInfo()
                refreshSignalAndStatus()
                if (_isAutoRefresh.value) {
                    startAutoRefresh()
                }
                onResult(true)
            } else {
                _isLoggedIn.value = false
                val err = result.exceptionOrNull()?.message ?: "Login gagal"
                _loginError.value = err
                _toastMessage.value = err
                if (_isAutoRefresh.value) {
                    startAutoRefresh()
                }
                onResult(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isRefreshing.value = true
            apiClient.logout()
            prefs.edit()
                .putBoolean("is_logged_in", false)
                .remove("saved_password")
                .apply()
            _isLoggedIn.value = false
            _savedPassword.value = ""
            _toastMessage.value = "Berhasil logout dari modem"
            _isRefreshing.value = false
        }
    }

    fun lockBand(hexMask: String, bandName: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val res = apiClient.setLteBand(hexMask, networkMode = if (hexMask == "7FFFFFFFFFFFFFFF") "00" else "03")
            _isRefreshing.value = false
            if (res.isSuccess) {
                _toastMessage.value = "Berhasil mengunci ke $bandName"
                delay(1500)
                refreshSignalAndStatus()
            } else {
                _toastMessage.value = res.exceptionOrNull()?.message ?: "Gagal mengunci band"
            }
        }
    }

    fun rebootRouter(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val res = apiClient.rebootRouter()
            _isRefreshing.value = false
            if (res.isSuccess) {
                _toastMessage.value = "Modem sedang reboot..."
                onComplete(true)
            } else {
                _toastMessage.value = res.exceptionOrNull()?.message ?: "Gagal reboot"
                onComplete(false)
            }
        }
    }

    fun toggleMobileData(enable: Boolean) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val res = apiClient.setMobileData(enable)
            _isRefreshing.value = false
            if (res.isSuccess) {
                _toastMessage.value = if (enable) "Data seluler diaktifkan" else "Data seluler dimatikan"
                refreshSignalAndStatus()
            } else {
                _toastMessage.value = "Gagal mengubah status data"
            }
        }
    }

    fun loadSms() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val res = apiClient.getSmsList()
            _isRefreshing.value = false
            if (res.isSuccess) {
                _smsList.value = res.getOrThrow()
            } else {
                _toastMessage.value = "Gagal memuat SMS (Pastikan sudah login)"
            }
        }
    }

    fun sendSms(phone: String, content: String, onDone: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isRefreshing.value = true
            val res = apiClient.sendSms(phone, content)
            _isRefreshing.value = false
            if (res.isSuccess) {
                _toastMessage.value = "SMS berhasil terkirim"
                onDone(true)
                loadSms()
            } else {
                _toastMessage.value = "Gagal mengirim SMS"
                onDone(false)
            }
        }
    }

    fun deleteSms(index: Int) {
        viewModelScope.launch {
            val res = apiClient.deleteSms(index)
            if (res.isSuccess) {
                _toastMessage.value = "SMS berhasil dihapus"
                loadSms()
            } else {
                _toastMessage.value = "Gagal menghapus SMS"
            }
        }
    }

    fun sendUssd(code: String) {
        viewModelScope.launch {
            _isRefreshing.value = true
            _ussdResponse.value = "Menghubungi operator..."
            val res = apiClient.sendUssd(code)
            _isRefreshing.value = false
            if (res.isSuccess) {
                _ussdResponse.value = res.getOrThrow()
            } else {
                _ussdResponse.value = res.exceptionOrNull()?.message ?: "Gagal eksekusi USSD"
            }
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun clearUssd() {
        _ussdResponse.value = null
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

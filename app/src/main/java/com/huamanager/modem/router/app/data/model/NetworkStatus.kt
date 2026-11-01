package com.huamanager.modem.router.app.data.model

data class NetworkStatus(
    val connectionStatus: String = "-",
    val signalStrength: Int = 0, // 0 to 5
    val currentNetworkType: String = "-",
    val currentNetworkName: String = "-",
    val simStatus: String = "-",
    val isConnected: Boolean = false,
    val primaryDns: String = "-",
    val secondaryDns: String = "-"
) {
    fun getNetworkTypeName(): String {
        return when (currentNetworkType) {
            "19", "LTE", "4G" -> "4G LTE"
            "41", "LTE_CA", "4G+" -> "4G+ (CA)"
            "7", "HSPA+", "3G" -> "3G HSPA+"
            "1" -> "2G GSM"
            else -> if (currentNetworkType.isNotEmpty() && currentNetworkType != "-") currentNetworkType else "Online"
        }
    }
}

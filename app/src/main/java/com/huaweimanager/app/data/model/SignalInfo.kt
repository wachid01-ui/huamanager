package com.huaweimanager.app.data.model

enum class SignalQualityLevel {
    EXCELLENT, GOOD, FAIR, POOR, UNKNOWN
}

data class SignalInfo(
    val pci: String = "-",
    val cellId: String = "-",
    val rsrq: String = "-",
    val rsrp: String = "-",
    val rssi: String = "-",
    val sinr: String = "-",
    val band: String = "-",
    val dlFrequency: String = "-",
    val mode: String = "-",
    val plmn: String = "-"
) {
    fun parseRsrpValue(): Int? {
        val clean = rsrp.replace("dBm", "").replace(">", "").replace("<", "").trim()
        return clean.toIntOrNull()
    }

    fun parseSinrValue(): Int? {
        val clean = sinr.replace("dB", "").replace(">", "").replace("<", "").trim()
        return clean.toIntOrNull()
    }

    fun parseRsrqValue(): Int? {
        val clean = rsrq.replace("dB", "").replace(">", "").replace("<", "").trim()
        return clean.toIntOrNull()
    }

    fun getRsrpQuality(): SignalQualityLevel {
        val valRsrp = parseRsrpValue() ?: return SignalQualityLevel.UNKNOWN
        return when {
            valRsrp >= -80 -> SignalQualityLevel.EXCELLENT
            valRsrp >= -90 -> SignalQualityLevel.GOOD
            valRsrp >= -100 -> SignalQualityLevel.FAIR
            else -> SignalQualityLevel.POOR
        }
    }

    fun getSinrQuality(): SignalQualityLevel {
        val valSinr = parseSinrValue() ?: return SignalQualityLevel.UNKNOWN
        return when {
            valSinr >= 20 -> SignalQualityLevel.EXCELLENT
            valSinr >= 13 -> SignalQualityLevel.GOOD
            valSinr >= 0 -> SignalQualityLevel.FAIR
            else -> SignalQualityLevel.POOR
        }
    }

    fun getOverallQuality(): SignalQualityLevel {
        val rsrpQ = getRsrpQuality()
        val sinrQ = getSinrQuality()
        return if (rsrpQ == SignalQualityLevel.UNKNOWN) sinrQ else rsrpQ
    }
}

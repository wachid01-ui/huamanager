package com.huamanager.modem.router.app.data.model

import com.huamanager.modem.router.app.ui.i18n.AppStrings

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
    val plmn: String = "-",
    val tac: String = "-",
    val earfcn: String = "-",
    val enodebId: String = "-"
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

    /**
     * Converts Cell ID to 28-bit ECI (E-UTRAN Cell Identifier) decimal
     */
    fun getEciDecimal(): Long? {
        val clean = cellId.trim()
        if (clean.isEmpty() || clean == "-") return null
        return if (clean.startsWith("0x", ignoreCase = true) || clean.any { it in 'a'..'f' || it in 'A'..'F' }) {
            clean.removePrefix("0x").removePrefix("0X").toLongOrNull(16)
        } else {
            clean.toLongOrNull()
        }
    }

    /**
     * ECI in Hexadecimal format (e.g. 0x1340B01)
     */
    fun getEciHex(): String {
        val eci = getEciDecimal() ?: return "-"
        return "0x" + eci.toString(16).uppercase()
    }

    /**
     * eNodeB ID (BTS ID) = ECI / 256
     */
    fun getCalculatedEnodebId(): Long? {
        val direct = enodebId.trim().toLongOrNull()
        if (direct != null && direct > 0) return direct

        val eci = getEciDecimal() ?: return null
        return if (eci > 256) eci / 256 else null
    }

    /**
     * Local Cell ID (LCID) = ECI % 256
     * (Range 0 - 255. Operator sering menggunakan offset 10-an atau 100-an untuk membedakan frekuensi/carrier)
     */
    fun getCalculatedSectorId(): Int? {
        val eci = getEciDecimal() ?: return null
        return (eci % 256).toInt()
    }

    /**
     * Normalisasi Local Cell ID ke Sektor Fisik Antena Menara (1, 2, atau 3):
     * - Seri 1-3, 4-6, 7-9
     * - Seri 11-13, 21-23
     * - Seri 101-103, 104-106, 107-109 (contoh: 107 = Sektor Fisik 1 pada Carrier ke-3)
     */
    fun getPhysicalSectorNumber(): Int {
        val lcid = getCalculatedSectorId() ?: return 1
        val base = when {
            lcid >= 200 -> lcid - 200
            lcid >= 100 -> lcid - 100
            lcid >= 10 -> lcid % 10
            else -> lcid
        }
        return when (base % 3) {
            1 -> 1
            2 -> 2
            0 -> 3
            else -> 1
        }
    }

    /**
     * Estimated sector antenna azimuth direction
     */
    fun getSectorAzimuthDescription(strings: AppStrings? = null): String {
        val physical = getPhysicalSectorNumber()
        val lcid = getCalculatedSectorId()
        if (strings != null) {
            return strings.getSectorAzimuthDescription(physical, lcid)
        }
        if (lcid == null) return "Sektor tidak diketahui"
        return when (physical) {
            1 -> "Sektor Fisik 1 (LCID $lcid): Menghadap ~0° - 120° (Utara – Timur Laut)"
            2 -> "Sektor Fisik 2 (LCID $lcid): Menghadap ~120° - 240° (Tenggara – Selatan)"
            3 -> "Sektor Fisik 3 (LCID $lcid): Menghadap ~240° - 360° (Barat Laut – Barat)"
            else -> "Sektor Fisik $physical (LCID $lcid)"
        }
    }

    /**
     * Perkiraan letak posisi menara BTS relatif dari lokasi rumah pengguna
     */
    fun getEstimatedTowerBearing(strings: AppStrings? = null): String {
        val physical = getPhysicalSectorNumber()
        if (strings != null) {
            return strings.getEstimatedTowerBearing(physical)
        }
        return when (physical) {
            1 -> "SELATAN (atau Barat Daya)"
            2 -> "UTARA (atau Barat Laut)"
            3 -> "TIMUR (atau Tenggara)"
            else -> "SELATAN"
        }
    }

    /**
     * Panduan rekomendasi arah hadap pointing antena luar
     */
    fun getAntennaPointingAdvice(strings: AppStrings? = null): String {
        val bearing = getEstimatedTowerBearing(strings)
        val physical = getPhysicalSectorNumber()
        val lcid = getCalculatedSectorId() ?: 0
        val lcidText = if (lcid > 0) " (LCID: $lcid)" else ""
        if (strings != null) {
            return strings.getAntennaPointingAdvice(bearing, physical, lcidText)
        }
        return "Menara BTS diperkirakan berada di sebelah $bearing dari lokasi Anda (karena antena pemancar Sektor $physical$lcidText menembak ke arah rumah Anda). Arahkan antena luar (Yagi/LPDA) menghadap ke arah $bearing."
    }

    fun getMcc(): String {
        val clean = plmn.trim()
        return if (clean.length >= 3) clean.substring(0, 3) else "510"
    }

    fun getMnc(): String {
        val clean = plmn.trim()
        return if (clean.length > 3) clean.substring(3) else "10"
    }

    fun getOperatorName(): String {
        val mcc = getMcc()
        val mnc = getMnc()
        if (mcc == "510") {
            return when (mnc) {
                "10", "010" -> "Telkomsel"
                "01", "001", "21", "021" -> "Indosat Ooredoo"
                "11", "011" -> "XL Axiata"
                "89", "089" -> "Tri (3 Indonesia)"
                "09", "009", "28", "028" -> "Smartfren"
                else -> "Operator Seluler ($plmn)"
            }
        }
        return if (plmn.isNotEmpty() && plmn != "-") "Operator ($plmn)" else "Operator Seluler"
    }

    fun getCellMapperUrl(): String {
        val mcc = getMcc()
        val mnc = getMnc()
        return "https://www.cellmapper.net/map?MCC=$mcc&MNC=$mnc&type=LTE"
    }
}

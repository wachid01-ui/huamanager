package com.huamanager.modem.router.app.data.model

data class LteBand(
    val bandNumber: Int,
    val name: String,
    val frequency: String,
    val hexMask: String,
    val description: String = ""
) {
    companion object {
        val ALL_BANDS = listOf(
            LteBand(0, "AUTO", "Semua Band LTE", "7FFFFFFFFFFFFFFF", "Mode otomatis / standar"),
            LteBand(1, "Band 1", "2100 MHz (FDD)", "1", "Telkomsel, Indosat, XL"),
            LteBand(3, "Band 3", "1800 MHz (FDD)", "4", "Paling Populer & Cepat di Indonesia"),
            LteBand(5, "Band 5", "850 MHz (FDD)", "10", "Smartfren"),
            LteBand(7, "Band 7", "2600 MHz (FDD)", "40", "LTE High Band"),
            LteBand(8, "Band 8", "900 MHz (FDD)", "80", "Jangkauan luas (Telkomsel, Indosat, XL)"),
            LteBand(20, "Band 20", "800 MHz (FDD)", "80000", "Jangkauan pedesaan"),
            LteBand(28, "Band 28", "700 MHz (FDD)", "8000000", "Low Band 700MHz"),
            LteBand(38, "Band 38", "2600 MHz (TDD)", "2000000000", "TDD 2.6GHz"),
            LteBand(40, "Band 40", "2300 MHz (TDD)", "8000000000", "Telkomsel Orbit, Smartfren"),
            LteBand(41, "Band 41", "2500 MHz (TDD)", "10000000000", "TDD 2.5GHz")
        )

        val PRESET_COMBINATIONS = listOf(
            Pair("B1 + B3 (Carrier Aggregation)", "5"),
            Pair("B3 + B40 (Orbit / Smartfren CA)", "8000000004"),
            Pair("B1 + B3 + B40 (Multi-Band CA)", "8000000005"),
            Pair("B3 + B8 (Coverage + Speed)", "84")
        )

        fun calculateCombinedHex(selectedBands: List<LteBand>): String {
            if (selectedBands.isEmpty() || selectedBands.any { it.bandNumber == 0 }) {
                return "7FFFFFFFFFFFFFFF"
            }
            var combined: Long = 0L
            for (b in selectedBands) {
                try {
                    val value = b.hexMask.toLong(16)
                    combined = combined or value
                } catch (e: Exception) {
                    // ignore parse error
                }
            }
            return java.lang.Long.toHexString(combined).uppercase()
        }
    }
}

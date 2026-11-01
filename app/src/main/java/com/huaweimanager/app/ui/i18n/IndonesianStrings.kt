package com.huaweimanager.app.ui.i18n

object IndonesianStrings : AppStrings {
    // Navigation items
    override val navSignal = "Sinyal"
    override val navTower = "Tower"
    override val navBandLock = "Kunci Band"
    override val navSms = "SMS"
    override val navTools = "Alat"

    // General & Status
    override val connected = "Terhubung"
    override val disconnected = "Terputus"
    override val autoRefreshActive = "Auto-refresh: 2.5s"
    override val autoRefreshPaused = "Auto-refresh: Jeda"
    override val processing = "Memproses..."
    override val success = "Berhasil"
    override val failed = "Gagal"
    override val save = "Simpan"
    override val close = "Tutup"
    override val cancel = "Batal"
    override val confirm = "Ya, Lanjutkan"
    override val refresh = "Refresh"
    override val copy = "Salin"
    override val copied = "Tersalin ke clipboard"

    // Signal Quality Labels
    override val qualityExcellent = "SANGAT BAIK"
    override val qualityGood = "BAIK"
    override val qualityFair = "CUKUP"
    override val qualityPoor = "LEMAH / BURUK"
    override val qualitySearching = "MENCARI..."

    // Signal Metrics Labels & Descriptions
    override val noiseGood = "Bagus"
    override val noiseFair = "Cukup"
    override val noisePoor = "Banyak Noise"
    override val signalGood = "Bagus"
    override val signalFair = "Cukup"
    override val signalPoor = "Lemah"

    // Dashboard
    override val realtimeSignalParams = "Parameter Sinyal Real-time"
    override val notConnectedMsg = "Tidak terhubung ke modem"
    override val signalMetricRSRP = "RSRP (Kekuatan Sinyal)"
    override val signalMetricSINR = "SINR (Noise Ratio)"
    override val signalMetricRSRQ = "RSRQ (Kualitas Sinyal)"
    override val signalMetricRSSI = "RSSI (Total Daya)"
    override val signalMetricPCI = "PCI (Physical Cell ID)"
    override val signalMetricCellId = "Cell ID (eNodeB)"
    override val signalMetricBand = "Pita Frekuensi (Band)"
    override val signalMetricFreq = "Frekuensi DL / UL"
    override val signalMetricDlUlFreq = "Frekuensi DL/UL"
    override val signalMetricBandwidth = "Lebar Pita (Bandwidth)"
    override val signalMetricNetworkType = "Tipe Jaringan"
    override val signalMetricOperator = "Operator Seluler"
    override val signalMetricRoaming = "Status Roaming"
    override val roamingActive = "Aktif (Roaming)"
    override val roamingInactive = "Non-aktif"

    // Tower & Antenna
    override val towerInfoTitle = "Informasi Cell ID & Lokasi Tower BTS"
    override val towerSubtitle = "Deteksi otomatis nomor identitas tower BTS eNodeB dan arah sektor antena pemancar"
    override val enodebSectorTitle = "eNodeB ID & Sektor Antena"
    override val enodebIdLabel = "eNodeB ID (ID Tower BTS)"
    override val sectorAntennaLabel = "Sektor Antena"
    override val physicalSectorLabel = "Sektor Fisik BTS"
    override val localCellIdLabel = "Local Cell ID (LCID)"
    override val physicalSectorDesc = "Berdasarkan format standar eNodeB / 256"
    override val antennaAzimuthTitle = "Arah Sektor Antena Pemancar (Azimuth)"
    override val antennaAzimuthSubtitle = "Perkiraan arah pancaran sinyal dari antena pemancar BTS operator"
    override val estimatedTowerDirTitle = "Perkiraan Letak Tower BTS Anda"
    override val estimatedTowerDirSubtitle = "Panduan posisi tower BTS terhadap rumah Anda & arah mengarahkan antena modem"
    override val towerAdviceHeader = "PANDUAN POSISI TOWER & ARAH ANTENA"
    override val towerAdviceIntro = "Posisi tower BTS berada di arah"
    override val towerAdvicePointing = "Arahkan antena modem Anda menuju arah"
    override val towerAdviceNote = "Gunakan kompas HP sebagai acuan arah saat mengarahkan antena luar/modem untuk mendapatkan sinyal RSRP dan SINR terbaik."
    override val openCellMapperBtn = "Buka Lokasi di CellMapper.net"
    override val manualCellIdCalcTitle = "Kalkulator Cell ID Manual"
    override val manualCellIdCalcSubtitle = "Hitung eNodeB ID dan sektor antena dari Cell ID operator mana pun"
    override val manualCellIdInputLabel = "Masukkan Cell ID (contoh: 27402347)"
    override val calculateBtn = "Kalkulasi"
    override val calcResultTitle = "Hasil Perhitungan:"
    override val calcResultEnodeB = "eNodeB ID:"
    override val calcResultSector = "Sektor:"
    override val calcResultAzimuth = "Pancaran Antena:"
    override val calcResultTowerDir = "Letak Tower BTS:"
    override val invalidCellIdError = "Cell ID harus berupa angka positif"
    override val directionNorth = "Utara (0° - 120°)"
    override val directionEast = "Timur / Tenggara (120° - 240°)"
    override val directionSouth = "Selatan (180°)"
    override val directionWest = "Barat / Barat Daya (240° - 360°)"
    override val directionNorthEast = "Timur Laut"
    override val directionSouthEast = "Tenggara"
    override val directionSouthWest = "Barat Daya"
    override val directionNorthWest = "Barat Laut"
    override val sectorWord = "Sektor"
    override val frequencyLabel = "Frekuensi"
    override val bearingSouthOrSouthwest = "SELATAN (atau Barat Daya)"
    override val bearingNorthOrNorthwest = "UTARA (atau Barat Laut)"
    override val bearingEastOrSoutheast = "TIMUR (atau Tenggara)"
    override val bearingSouth = "SELATAN"
    override val sectorUnknown = "Sektor tidak diketahui"

    override fun getEstimatedTowerBearing(physicalSector: Int): String {
        return when (physicalSector) {
            1 -> bearingSouthOrSouthwest
            2 -> bearingNorthOrNorthwest
            3 -> bearingEastOrSoutheast
            else -> bearingSouth
        }
    }

    override fun getSectorAzimuthDescription(physicalSector: Int, lcid: Int?): String {
        val lcidStr = if (lcid != null) " (LCID $lcid)" else ""
        return when (physicalSector) {
            1 -> "Sektor Fisik 1$lcidStr: Menghadap ~0° - 120° (Utara – Timur Laut)"
            2 -> "Sektor Fisik 2$lcidStr: Menghadap ~120° - 240° (Tenggara – Selatan)"
            3 -> "Sektor Fisik 3$lcidStr: Menghadap ~240° - 360° (Barat Laut – Barat)"
            else -> "Sektor Fisik $physicalSector$lcidStr"
        }
    }

    override fun sectorCarrierNote(lcid: Int, physicalSector: Int): String =
        "Info Sektor: Angka $lcid adalah Local Cell ID (LCID) operator. Operator seluler memakai seri 100-an untuk membedakan frekuensi/carrier ke-3, yang secara fisik mengarah ke Sektor $physicalSector."

    override fun getAntennaPointingAdvice(bearing: String, physicalSector: Int, lcidText: String): String =
        "Menara BTS diperkirakan berada di sebelah $bearing dari lokasi Anda (karena antena pemancar Sektor $physicalSector$lcidText menembak ke arah rumah Anda). Arahkan antena luar (Yagi/LPDA) menghadap ke arah $bearing."

    override val towerCopyHeader = "=== DATA MENARA BTS MODEM HUAWEI ==="
    override val towerCopyTowerLoc = "Letak Menara"
    override val towerCopyAimDir = "Arah Pointing"
    override val towerCopyPosPrefix = "Di sebelah %s posisi Anda"
    override val towerCopyAimPrefix = "Hadapkan antena luar ke %s"

    // Band Lock
    override val bandLockTitle = "Kunci Band LTE / Frekuensi 4G"
    override val bandLockSubtitle = "Pilih salah satu atau beberapa band frekuensi 4G LTE untuk mengunci koneksi modem"
    override val activeBandLabel = "Band LTE Aktif Saat Ini:"
    override val selectBandsToLock = "Pilih Frekuensi LTE yang Ingin Dikunci:"
    override val lockSelectedBandsBtn = "Kunci ke Band Terpilih"
    override val resetAllBandsBtn = "Reset (Otomatis Semua Band)"
    override val bandLockAuthRequired = "Kunci Band memerlukan autentikasi. Buka tab 'Alat' untuk memasukkan password admin modem dan login terlebih dahulu."
    override val bandLockSuccessMsg = "Berhasil mengunci ke"
    override val bandLockFailedMsg = "Gagal mengunci band"
    override val selectAtLeastOneBand = "Pilih minimal 1 band frekuensi"

    // SMS
    override val smsTitle = "Pesan SMS Kartu SIM Modem"
    override val smsSubtitle = "Kelola dan baca pesan SMS yang masuk ke kartu SIM di dalam modem Huawei"
    override val sendSmsCardTitle = "Kirim SMS Baru"
    override val sendSmsCardSubtitle = "Kirim SMS melalui kartu SIM modem langsung dari aplikasi"
    override val recipientPhoneLabel = "Nomor Telepon Tujuan (contoh: 08123456789)"
    override val messageContentLabel = "Tulis isi pesan SMS..."
    override val sendSmsBtn = "Kirim SMS"
    override val sendingSms = "Mengirim..."
    override val smsSentSuccess = "SMS berhasil terkirim"
    override val smsSentFailed = "Gagal mengirim SMS"
    override val inboxTitle = "Kotak Masuk SIM"
    override val inboxSubtitle = "Daftar pesan SMS di memori SIM"
    override val noSmsMessages = "Belum ada pesan SMS di kotak masuk"
    override val deleteSmsBtn = "Hapus"
    override val deleteSmsSuccess = "SMS berhasil dihapus"
    override val deleteSmsFailed = "Gagal menghapus SMS"

    // Tools & Settings
    override val connectionAuthTitle = "Koneksi & Autentikasi Modem"
    override val gatewayIpLabel = "IP Gateway Modem (Default: 192.168.8.1)"
    override val usernameLabel = "Username WebUI Modem (Default: admin)"
    override val passwordLabel = "Password Admin WebUI Modem"
    override val showPasswordDesc = "Lihat password"
    override val hidePasswordDesc = "Sembunyikan password"
    override val statusLoggedIn = "Status: Sudah Login"
    override val statusNotLoggedIn = "Status: Belum Login"
    override val loginBtn = "Login"
    override val logoutBtn = "Logout"
    override val reloginBtn = "Login Ulang"
    override val loginSuccess = "Login berhasil!"
    override val logoutSuccess = "Berhasil logout dari modem"
    override val loginFailed = "Login gagal"
    override val tipsTitle = "Petunjuk Penggunaan Login Modem"
    override val tip1 = "1. Pastikan HP Android Anda terhubung ke jaringan WiFi modem Huawei (bukan data seluler HP)."
    override val tip2 = "2. Password admin modem biasanya tercantum di stiker bawah router atau password WebUI yang Anda buat saat pertama kali aktivasi."
    override val tip3 = "3. Gunakan ikon mata pada kolom password untuk memastikan huruf besar/kecil (kapital) dan simbol sudah sesuai tanpa salah ketik."
    override val tip4 = "4. Aplikasi otomatis menyimpan sesi login sehingga Anda tetap login setiap kali membuka aplikasi sampai menekan tombol Logout."
    override val tip5 = "5. Setelah status 'Sudah Login', Anda dapat mengunci frekuensi (Band Locking), membaca/kirim SMS, dan restart router."
    override val modemControlsTitle = "Kontrol Modem"
    override val rebootBtn = "Restart Modem"
    override val mobileDataBtn = "Data Seluler"
    override val mobileDataOn = "Data seluler diaktifkan"
    override val mobileDataOff = "Data seluler dimatikan"
    override val rebootConfirmTitle = "Konfirmasi Restart Modem"
    override val rebootConfirmMsg = "Apakah Anda yakin ingin me-restart modem Huawei sekarang? Koneksi internet akan terputus selama proses restart (sekitar 1-2 menit)."
    override val rebootProgressMsg = "Modem sedang reboot..."
    override val rebootFailedMsg = "Gagal reboot"
    override val ussdTitle = "USSD Operator"
    override val ussdCodeLabel = "Kode USSD (contoh: *888# atau *363#)"
    override val ussdCallBtn = "Panggil"
    override val ussdCalling = "Menghubungi operator..."
    override val ussdResultTitle = "Hasil USSD:"
    override val deviceInfoTitle = "Informasi Perangkat"
    override val deviceInfoModel = "Tipe Modem"
    override val deviceInfoFirmware = "Versi Firmware"
    override val deviceInfoWebUi = "Versi WebUI"
    override val deviceInfoHardware = "Versi Hardware"
    override val deviceInfoImei = "IMEI"
    override val deviceInfoImsi = "IMSI"
    override val deviceInfoMac = "MAC Address"

    // Theme & Appearance
    override val themeSectionTitle = "Tema Tampilan Aplikasi"
    override val themeSectionSubtitle = "Pilih gaya warna & tampilan yang paling nyaman untuk Anda"
    override val activeThemePrefix = "Tema aktif:"

    // Language Section
    override val languageSectionTitle = "Bahasa Aplikasi / App Language"
    override val languageSectionSubtitle = "Pilih bahasa antarmuka aplikasi yang Anda inginkan"
    override val activeLanguagePrefix = "Bahasa aktif:"
}

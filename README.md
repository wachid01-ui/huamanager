# Huawei Manager - Aplikasi Android (Kotlin & Jetpack Compose)

Aplikasi Android modern untuk memonitor, mengunci frekuensi (band locking), membaca/kirim SMS, dan mengelola modem/router Huawei 4G LTE (seperti seri Huawei B310, B311, B312, B315, B525, B535, Telkomsel Orbit Star 1/2/3, MiFi E5577, dll.).

---

## 📱 Fitur Utama

1. **Dashboard Monitoring Sinyal Real-time**
   - **Signal Gauge**: Indikator visual kualitas sinyal (Sangat Baik / Baik / Cukup / Buruk).
   - **RSRP** (*Reference Signal Received Power*): Indikator utama kekuatan sinyal dalam dBm.
   - **SINR** (*Signal to Interference plus Noise Ratio*): Rasio sinyal murni terhadap gangguan/noise dalam dB.
   - **RSRQ** (*Reference Signal Received Quality*): Kualitas sinyal yang diterima.
   - **RSSI**: Total daya sinyal yang diterima.
   - **PCI & Cell ID**: Mengidentifikasi menara BTS yang sedang terhubung.
   - **Band Aktif & Frekuensi**: Menampilkan frekuensi yang sedang digunakan (misal Band 3 - 1800 MHz).
   - **Auto-Refresh**: Pembaharuan data otomatis setiap 2.5 detik.

2. **Kunci Frekuensi LTE (Band Locking)**
   - **Preset Cepat Operator Indonesia**:
     - **Band 3 (1800 MHz)**: Paling populer & cepat untuk Telkomsel, Indosat, XL, Tri.
     - **Band 40 (2300 MHz TDD)**: Khusus Telkomsel Orbit & Smartfren.
     - **Band 1 (2100 MHz)**: Alternatif kecepatan tinggi.
     - **Band 8 (900 MHz)**: Jangkauan sinyal terjauh dan tembus dinding.
     - **Auto (Semua Band)**: Mengembalikan modem ke pemilihan otomatis bawaan pabrik.
   - **Carrier Aggregation (4G+)**: Pilihan kombinasi multi-band (B1+B3, B3+B40, dll.).
   - **Custom Band Selector**: Checklist untuk mengunci kombinasi frekuensi apapun secara fleksibel.

3. **Kotak Masuk & Pengiriman SMS**
   - Membaca daftar SMS langsung dari kartu SIM di dalam router.
   - Mengirim SMS langsung dari aplikasi (sangat praktis untuk registrasi paket data atau kirim format aktivasi operator).
   - Menghapus SMS dari kartu SIM modem.

4. **Alat & Kontrol Router (Tools)**
   - **Konfigurasi IP Modem**: Ubah IP gateway sesuai router Anda (default: `192.168.8.1`).
   - **Autentikasi Aman**: Mendukung router lawas (HiLink standard) maupun router modern/Orbit (SCRAM-SHA-256).
   - **Reboot Modem**: Restart router Huawei langsung dari HP dengan konfirmasi keamanan.
   - **Saklar Data Seluler**: Nyalakan atau matikan sambungan mobile data modem.
   - **Tool Dial USSD**: Kirim perintah USSD (seperti `*888#` atau `*363#`) dan terima balasannya langsung di layar.
   - **Detail Informasi Perangkat**: Nama model, IMEI, IMSI, versi Firmware, versi WebUI, dan MAC Address.

---

## 🛠️ Persyaratan & Teknologi

- **Bahasa**: Kotlin
- **UI Framework**: Jetpack Compose + Material Design 3
- **Arsitektur**: MVVM (Model-View-ViewModel) dengan Coroutines & StateFlow
- **Jaringan**: OkHttp 3 dengan manajemen Cookie Jar & CSRF Token Interceptor
- **Target OS**: Android 7.0 (API Level 24) hingga Android 14+ (API Level 34)

---

## 🚀 Cara Membuka & Membuat File APK di Android Studio

1. **Buka Project di Android Studio**:
   - Buka aplikasi **Android Studio**.
   - Pilih menu **File** -> **Open...**
   - Arahkan ke folder:
     ```
     C:\Users\wachi\.gemini\antigravity\scratch\huawei-manager-android
     ```
   - Tunggu proses Gradle Sync selesai mengunduh library yang dibutuhkan.

2. **Build APK**:
   - Klik menu **Build** pada toolbar atas.
   - Pilih **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
   - Setelah selesai, Android Studio akan menampilkan notifikasi *APK(s) generated successfully*.
   - Klik tautan **locate** untuk mengambil file `app-debug.apk`.

3. **Install di HP Android**:
   - Kirim atau copy file `app-debug.apk` ke HP Android Anda.
   - Pastikan HP Android Anda terhubung ke jaringan **WiFi dari modem Huawei tersebut**.
   - Buka aplikasi **Huawei Manager**, lalu monitor sinyal atau kunci band sesuai kebutuhan Anda!

---

## 💡 Catatan Penggunaan

- **Koneksi WiFi**: HP Android **wajib** terhubung ke WiFi modem Huawei agar aplikasi dapat berkomunikasi dengan API router di `192.168.8.1`.
- **Login Password**: Untuk memonitor sinyal (RSRP/SINR), sebagian besar modem Huawei tidak memerlukan password login. Namun untuk **Kunci Band (Band Locking)**, **Kirim/Hapus SMS**, dan **Reboot**, silakan masukkan password admin WebUI Anda di tab **Alat** lalu klik **Login**.

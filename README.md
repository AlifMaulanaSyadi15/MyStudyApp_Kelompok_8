# MyStudy App — Praktek 7: Pengaturan Bahasa & Mode Gelap

Project Android Studio (Kotlin + XML Layout) dari modul **Praktek 7 – Studi Kasus Pengaturan Bahasa & Mode Gelap**,
lengkap dengan pengembangan tambahan.

## Cara membuka
1. Ekstrak zip, lalu di Android Studio pilih **File → Open** dan arahkan ke folder `MyStudyApp`.
2. Tunggu **Gradle Sync** selesai (butuh internet pada sync pertama).
3. Jalankan (**Run ▶**) di emulator / ponsel dengan Android 7.0 (API 24) ke atas.

Konfigurasi: AGP 8.2.2 · Gradle 8.2 · Kotlin 1.9.22 · compileSdk/targetSdk 34 · minSdk 24 · JDK 17
(bawaan Android Studio Hedgehog 2023.1.1 atau lebih baru). Bila Android Studio menawarkan *upgrade* AGP/Gradle,
boleh diterima atau diabaikan.

## Struktur
```
app/src/main/
├── java/com/example/mystudyapp/
│   ├── MyStudyApp.kt            Application: menerapkan pengaturan paling awal
│   ├── SettingsManager.kt       SATU-SATUNYA tempat logika pengaturan (+ RingkasanPengaturan)
│   ├── MainActivity.kt          Halaman utama
│   ├── PengaturanActivity.kt    Halaman pengaturan
│   ├── AksenHelper.kt           [tambahan] penerapan warna aksen
│   └── NotifikasiHelper.kt      [tambahan] notifikasi sungguhan
└── res/
    ├── layout/                  activity_main.xml, activity_pengaturan.xml
    ├── values/                  colors.xml (terang), strings.xml (Indonesia), themes.xml
    ├── values-en/strings.xml    teks English
    ├── values-night/            colors.xml & themes.xml (mode GELAP)
    └── drawable/, mipmap-*/     latar kartu, ikon aplikasi, ikon splash, ikon notifikasi
```

## Ketentuan modul → di mana dikerjakan
| Ketentuan | Lokasi |
|---|---|
| Halaman utama (nama app, judul, info, tombol Pengaturan) | `activity_main.xml`, `MainActivity.kt` |
| Halaman pengaturan ≥ 2 pengaturan | `activity_pengaturan.xml`, `PengaturanActivity.kt` |
| Bahasa Indonesia / English | `values/strings.xml`, `values-en/strings.xml`, `setApplicationLocales()` |
| Mode Terang / Gelap | `values/colors.xml`, `values-night/colors.xml`, `setDefaultNightMode()` |
| Disimpan lokal: `language` (id/en), `theme` (light/dark) | `SettingsManager` → SharedPreferences `pengaturan_app` |
| Diterapkan otomatis saat aplikasi dibuka | `MyStudyApp.onCreate()` |
| Perubahan langsung terlihat (state) | listener di `PengaturanActivity`, nilai selalu dibaca dari penyimpanan |
| Satu Settings Manager | `SettingsManager.kt` (tidak ada `getSharedPreferences()` di Activity) |

## Pengembangan tambahan
Dari modul (sudah termasuk): **ukuran teks**, **notifikasi**, **ringkasan pengaturan aktif**, **reset dengan konfirmasi**.

Tambahan di project ini (dari daftar "Pengembangan Tambahan yang Bisa Dilanjutkan" pada modul):

1. **Mengikuti sistem** — pilihan ketiga untuk Mode Tampilan (`MODE_NIGHT_FOLLOW_SYSTEM`) dan Bahasa (daftar locale kosong = bahasa ponsel).
2. **Pilihan warna aksen** (Hijau / Biru / Ungu), kunci `accent`; berlaku pada tombol, ikon kartu, radio, sakelar, dan nama aplikasi.
3. **Profil pengguna** — nama disimpan (kunci `user_name`) dan ditampilkan sebagai sapaan di halaman utama.
   Nama bukan pengaturan, jadi **tidak ikut terhapus** oleh Reset.
4. **Konfirmasi sebelum ganti bahasa** — dialog `AlertDialog`; bila dibatalkan, pilihan radio dikembalikan.
5. **Layar splash** — `core-splashscreen`, ditahan 0,6 detik hanya pada pembukaan pertama.
6. **Notifikasi sungguhan** — menyalakan sakelar meminta izin `POST_NOTIFICATIONS` (Android 13+) lalu menampilkan
   satu notifikasi konfirmasi. Bila izin ditolak, sakelar kembali mati. Bila izin dicabut lewat pengaturan sistem,
   sakelar otomatis mati saat halaman pengaturan dibuka.
7. **Ikon aplikasi** (adaptive icon + ikon monokrom) dan status bar yang menyesuaikan mode terang/gelap.
8. **Validasi nilai tersimpan** — nilai tak dikenal di SharedPreferences otomatis kembali ke nilai awal.

Isi `pengaturan_app.xml` (Device File Explorer → `/data/data/com.example.mystudyapp/shared_preferences/`):
```xml
<map>
    <string name="language">en</string>       <!-- id | en | system -->
    <string name="theme">dark</string>        <!-- light | dark | system -->
    <string name="text_size">medium</string>  <!-- small | medium | large -->
    <boolean name="notifikasi" value="true" />
    <string name="accent">green</string>      <!-- green | blue | purple -->
    <string name="user_name">...</string>
</map>
```

## Uji coba tambahan (selain 17 skenario pada modul)
| Skenario | Hasil yang diharapkan |
|---|---|
| Pilih English → tekan **Batal** pada dialog | Radio kembali ke pilihan semula, bahasa tidak berubah |
| Pilih English → **Ya, ganti** | Seluruh teks berganti, termasuk halaman utama saat kembali |
| Mode Tampilan → *Ikuti sistem*, lalu ganti tema sistem ponsel | Aplikasi ikut terang/gelap |
| Pilih aksen Biru → kembali ke halaman utama | Tombol, ikon kartu, dan nama aplikasi berwarna biru |
| Nyalakan Notifikasi (Android 13+) | Muncul dialog izin; setelah diizinkan muncul notifikasi |
| Tolak izin notifikasi | Sakelar kembali mati + toast "Izin notifikasi ditolak" |
| Isi nama → **Simpan Nama** → Back | Sapaan "Halo, <nama>! 👋" tampil di kepala halaman utama |
| Kosongkan nama → **Simpan Nama** | Sapaan hilang |
| Reset Pengaturan | Semua kembali ke awal, **nama tetap tersimpan** |

## Bahan laporan: mekanisme penyimpanan
1. **Penyimpanan**: SharedPreferences, file `pengaturan_app` (bukan database, karena datanya kecil & sederhana).
2. **Penulisan**: `SettingsManager` menyimpan lewat `edit()` → `put...` → `apply()` setiap pengguna mengubah pilihan.
3. **Pembacaan**: kelas Application (`MyStudyApp`) membaca pengaturan lalu memanggil `setDefaultNightMode()` (tema) dan
   `setApplicationLocales()` (bahasa) sebelum halaman pertama tampil.
4. **Penerapan**: teks → `@string` + `values-en/strings.xml`; warna → `@color` + `values-night/colors.xml`.
   Tampilan tidak menyimpan salinan nilai; ia selalu dibangun ulang dari pengaturan tersimpan (= *state*).
5. **Reset**: menghapus kunci pengaturan (`remove`) → diterapkan ulang → `recreate()`.

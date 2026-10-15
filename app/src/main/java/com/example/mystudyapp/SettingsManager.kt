package com.example.mystudyapp

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * SATU-SATUNYA tempat logika pengaturan: nama kunci, baca, tulis, reset,
 * dan penerapan tema/bahasa. Tidak ada getSharedPreferences() di Activity.
 */
class SettingsManager(context: Context) {

    // satu objek SharedPreferences untuk seluruh aplikasi
    private val prefs = context.applicationContext
        .getSharedPreferences(NAMA_PREFS, Context.MODE_PRIVATE)

    companion object {
        private const val NAMA_PREFS = "pengaturan_app"

        // ===== kunci penyimpanan =====
        const val KEY_BAHASA     = "language"
        const val KEY_TEMA       = "theme"
        const val KEY_UKURAN     = "text_size"
        const val KEY_NOTIFIKASI = "notifikasi"
        const val KEY_AKSEN      = "accent"      // pengembangan tambahan
        const val KEY_NAMA       = "user_name"   // pengembangan tambahan

        // ===== nilai yang sah =====
        const val BAHASA_ID     = "id"
        const val BAHASA_EN     = "en"
        const val BAHASA_SISTEM = "system"       // pengembangan tambahan

        const val TEMA_TERANG = "light"
        const val TEMA_GELAP  = "dark"
        const val TEMA_SISTEM = "system"         // pengembangan tambahan

        const val UKURAN_KECIL  = "small"
        const val UKURAN_SEDANG = "medium"
        const val UKURAN_BESAR  = "large"

        const val AKSEN_HIJAU = "green"
        const val AKSEN_BIRU  = "blue"
        const val AKSEN_UNGU  = "purple"

        /** Menerapkan tema & bahasa ke SELURUH aplikasi.
         *  Dipanggil dari MyStudyApp dan setiap kali pengaturan diubah. */
        fun terapkanKeAplikasi(context: Context) {
            val s = SettingsManager(context)

            // 1) tema: paksa terang, paksa gelap, atau ikuti sistem
            AppCompatDelegate.setDefaultNightMode(
                when (s.ambilTema()) {
                    TEMA_GELAP  -> AppCompatDelegate.MODE_NIGHT_YES
                    TEMA_SISTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    else        -> AppCompatDelegate.MODE_NIGHT_NO
                }
            )

            // 2) bahasa: pakai res/values atau res/values-en (atau ikuti bahasa ponsel)
            val bahasa = s.ambilBahasa()
            AppCompatDelegate.setApplicationLocales(
                if (bahasa == BAHASA_SISTEM) LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(bahasa)
            )
        }
    }

    // ================= BAHASA =================
    fun ambilBahasa(): String {
        val nilai = prefs.getString(KEY_BAHASA, BAHASA_ID)
        return if (nilai == BAHASA_EN || nilai == BAHASA_SISTEM) nilai else BAHASA_ID
    }
    fun simpanBahasa(nilai: String) = prefs.edit().putString(KEY_BAHASA, nilai).apply()

    // ================= TEMA =================
    fun ambilTema(): String {
        val nilai = prefs.getString(KEY_TEMA, TEMA_TERANG)
        return if (nilai == TEMA_GELAP || nilai == TEMA_SISTEM) nilai else TEMA_TERANG
    }
    fun simpanTema(nilai: String) = prefs.edit().putString(KEY_TEMA, nilai).apply()

    // ================= UKURAN TEKS =================
    fun ambilUkuran(): String {
        val nilai = prefs.getString(KEY_UKURAN, UKURAN_SEDANG)
        return if (nilai == UKURAN_KECIL || nilai == UKURAN_BESAR) nilai else UKURAN_SEDANG
    }
    fun simpanUkuran(nilai: String) = prefs.edit().putString(KEY_UKURAN, nilai).apply()

    /** ukuran teks halaman utama dalam satuan sp */
    fun ukuranTeksSp(): Float = when (ambilUkuran()) {
        UKURAN_KECIL -> 12f
        UKURAN_BESAR -> 17f
        else         -> 14f
    }

    // ================= NOTIFIKASI =================
    fun ambilNotifikasi(): Boolean = prefs.getBoolean(KEY_NOTIFIKASI, false)
    fun simpanNotifikasi(nilai: Boolean) = prefs.edit().putBoolean(KEY_NOTIFIKASI, nilai).apply()

    // ================= WARNA AKSEN (tambahan) =================
    fun ambilAksen(): String {
        val nilai = prefs.getString(KEY_AKSEN, AKSEN_HIJAU)
        return if (nilai == AKSEN_BIRU || nilai == AKSEN_UNGU) nilai else AKSEN_HIJAU
    }
    fun simpanAksen(nilai: String) = prefs.edit().putString(KEY_AKSEN, nilai).apply()

    // ================= PROFIL: NAMA (tambahan) =================
    fun ambilNama(): String = prefs.getString(KEY_NAMA, "") ?: ""
    fun simpanNama(nilai: String) {
        val bersih = nilai.trim()
        if (bersih.isEmpty()) prefs.edit().remove(KEY_NAMA).apply()
        else prefs.edit().putString(KEY_NAMA, bersih).apply()
    }

    // ================= RESET =================
    /** Mengembalikan SEMUA pengaturan ke nilai awal. Nama profil bukan
     *  pengaturan, jadi dibiarkan tersimpan. */
    fun reset() {
        prefs.edit()
            .remove(KEY_BAHASA)
            .remove(KEY_TEMA)
            .remove(KEY_UKURAN)
            .remove(KEY_NOTIFIKASI)
            .remove(KEY_AKSEN)
            .apply()
    }
}

/** Ringkasan pengaturan aktif — dipakai halaman utama DAN halaman pengaturan,
 *  jadi tulisannya tidak perlu diulang di dua tempat. */
object RingkasanPengaturan {

    /** contoh: "Indonesia • Terang • Teks Sedang" */
    fun teks(context: Context, s: SettingsManager): String {
        val bahasa = context.getString(
            when (s.ambilBahasa()) {
                SettingsManager.BAHASA_EN     -> R.string.singkat_en
                SettingsManager.BAHASA_SISTEM -> R.string.singkat_sistem
                else                          -> R.string.singkat_id
            }
        )
        val tema = context.getString(
            when (s.ambilTema()) {
                SettingsManager.TEMA_GELAP  -> R.string.singkat_gelap
                SettingsManager.TEMA_SISTEM -> R.string.singkat_sistem
                else                        -> R.string.singkat_terang
            }
        )
        val ukuran = context.getString(
            R.string.singkat_teks,
            context.getString(
                when (s.ambilUkuran()) {
                    SettingsManager.UKURAN_KECIL -> R.string.singkat_kecil
                    SettingsManager.UKURAN_BESAR -> R.string.singkat_besar
                    else                         -> R.string.singkat_sedang
                }
            )
        )
        return context.getString(R.string.ringkasan_format, bahasa, tema, ukuran)
    }

    /** contoh: "Notifikasi: Aktif" */
    fun statusNotifikasi(context: Context, s: SettingsManager): String = context.getString(
        R.string.status_format,
        context.getString(R.string.singkat_notifikasi),
        context.getString(
            if (s.ambilNotifikasi()) R.string.status_aktif else R.string.status_nonaktif
        )
    )

    /** contoh: "Aksen: Hijau" */
    fun statusAksen(context: Context, s: SettingsManager): String = context.getString(
        R.string.status_format,
        context.getString(R.string.singkat_aksen),
        context.getString(
            when (s.ambilAksen()) {
                SettingsManager.AKSEN_BIRU -> R.string.aksen_biru_nama
                SettingsManager.AKSEN_UNGU -> R.string.aksen_ungu_nama
                else                       -> R.string.aksen_hijau_nama
            }
        )
    )
}

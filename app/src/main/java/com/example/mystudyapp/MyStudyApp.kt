package com.example.mystudyapp

import android.app.Application

/**
 * Kelas Application: dijalankan PALING AWAL, sebelum halaman mana pun tampil.
 * Di sinilah pengaturan terakhir dibaca dan diterapkan ke seluruh aplikasi.
 */
class MyStudyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // baca pengaturan terakhir dan terapkan ke seluruh aplikasi
        SettingsManager.terapkanKeAplikasi(this)
    }
}

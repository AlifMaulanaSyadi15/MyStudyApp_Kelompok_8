package com.example.mystudyapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    companion object {
        // layar splash hanya ditahan sejenak pada saat aplikasi pertama kali dibuka
        private var splashSudahTampil = false
        private const val DURASI_SPLASH_MS = 600L
    }

    private lateinit var settings: SettingsManager

    // view yang ikut berubah ukurannya
    private lateinit var tvSubJudul: TextView
    private lateinit var tvMateriIsi: TextView
    private lateinit var tvJadwalIsi: TextView
    private lateinit var tvTugasIsi: TextView
    private lateinit var tvRingkasan: TextView

    // view yang ikut berubah warna aksennya
    private lateinit var tvNamaApp: TextView
    private lateinit var ivMateri: TextView
    private lateinit var ivJadwal: TextView
    private lateinit var ivTugas: TextView
    private lateinit var btnPengaturan: Button

    // sapaan nama profil
    private lateinit var tvSapaan: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // layar splash (pengembangan tambahan) — dipasang sebelum super.onCreate()
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)

        if (!splashSudahTampil && savedInstanceState == null) {
            splashSudahTampil = true
            var siap = false
            splash.setKeepOnScreenCondition { !siap }
            Handler(Looper.getMainLooper()).postDelayed({ siap = true }, DURASI_SPLASH_MS)
        }

        setContentView(R.layout.activity_main)

        // 1) siapkan pengelola pengaturan
        settings = SettingsManager(this)

        // 2) ambil view
        tvSubJudul  = findViewById(R.id.tvSubJudul)
        tvMateriIsi = findViewById(R.id.tvMateriIsi)
        tvJadwalIsi = findViewById(R.id.tvJadwalIsi)
        tvTugasIsi  = findViewById(R.id.tvTugasIsi)
        tvRingkasan = findViewById(R.id.tvRingkasan)

        tvNamaApp     = findViewById(R.id.tvNamaApp)
        ivMateri      = findViewById(R.id.ivMateri)
        ivJadwal      = findViewById(R.id.ivJadwal)
        ivTugas       = findViewById(R.id.ivTugas)
        btnPengaturan = findViewById(R.id.btnPengaturan)
        tvSapaan      = findViewById(R.id.tvSapaan)

        // 3) tombol menuju halaman pengaturan
        btnPengaturan.setOnClickListener {
            startActivity(Intent(this, PengaturanActivity::class.java))
        }
    }

    /** Dijalankan setiap kali halaman ini tampil kembali
     *  — termasuk setelah kembali dari halaman pengaturan. */
    override fun onResume() {
        super.onResume()
        terapkanPengaturanTampilan()
    }

    private fun terapkanPengaturanTampilan() {
        // 1) terapkan ukuran teks pengaturan ke semua kalimat isi
        val ukuran = settings.ukuranTeksSp()
        listOf(tvSubJudul, tvMateriIsi, tvJadwalIsi, tvTugasIsi).forEach {
            it.setTextSize(TypedValue.COMPLEX_UNIT_SP, ukuran)
        }

        // 2) tampilkan ringkasan pengaturan yang sedang aktif
        tvRingkasan.text = RingkasanPengaturan.teks(this, settings)

        // 3) pengembangan tambahan: warna aksen
        val aksen = AksenHelper.warna(this, settings)
        tvNamaApp.setTextColor(aksen)
        listOf(ivMateri, ivJadwal, ivTugas).forEach { AksenHelper.tintIkon(it, aksen) }
        AksenHelper.tintTombol(btnPengaturan, aksen)

        // 4) pengembangan tambahan: sapaan nama profil
        val nama = settings.ambilNama()
        if (nama.isEmpty()) {
            tvSapaan.visibility = View.GONE
        } else {
            tvSapaan.text = getString(R.string.sapaan_format, nama)
            tvSapaan.visibility = View.VISIBLE
        }
    }
}

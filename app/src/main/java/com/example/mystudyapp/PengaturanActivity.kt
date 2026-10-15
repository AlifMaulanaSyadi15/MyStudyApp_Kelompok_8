package com.example.mystudyapp

import android.Manifest
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

class PengaturanActivity : AppCompatActivity() {

    private lateinit var settings: SettingsManager

    private lateinit var tvRingkasan: TextView
    private lateinit var tvStatusNotifikasi: TextView
    private lateinit var tvStatusAksen: TextView

    private lateinit var rgBahasa: RadioGroup
    private lateinit var rgTema: RadioGroup
    private lateinit var rgUkuran: RadioGroup
    private lateinit var rgAksen: RadioGroup
    private lateinit var swNotifikasi: SwitchCompat
    private lateinit var etNama: EditText
    private lateinit var btnSimpanNama: Button

    // ====================================================================
    //  LISTENER (dipasang BELAKANGAN, setelah keadaan awal diisi)
    // ====================================================================

    /** Bahasa: tampilkan dialog konfirmasi dulu sebelum benar-benar diganti. */
    private val listenerBahasa = RadioGroup.OnCheckedChangeListener { _, id ->
        val baru = bahasaDariId(id)
        if (baru == settings.ambilBahasa()) return@OnCheckedChangeListener

        val label = findViewById<RadioButton>(id).text
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_bahasa_judul)
            .setMessage(getString(R.string.dialog_bahasa_pesan, label))
            .setNegativeButton(R.string.dialog_batal) { _, _ -> pulihkanBahasa() }
            .setPositiveButton(R.string.dialog_bahasa_ya) { _, _ ->
                settings.simpanBahasa(baru)
                SettingsManager.terapkanKeAplikasi(this)      // teks langsung berganti bahasa
            }
            .setOnCancelListener { pulihkanBahasa() }
            .show()
    }

    private val listenerTema = RadioGroup.OnCheckedChangeListener { _, id ->
        settings.simpanTema(
            when (id) {
                R.id.rbGelap      -> SettingsManager.TEMA_GELAP
                R.id.rbTemaSistem -> SettingsManager.TEMA_SISTEM
                else              -> SettingsManager.TEMA_TERANG
            }
        )
        SettingsManager.terapkanKeAplikasi(this)              // tampilan langsung berganti tema
    }

    private val listenerUkuran = RadioGroup.OnCheckedChangeListener { _, id ->
        val nilai = when (id) {
            R.id.rbKecil -> SettingsManager.UKURAN_KECIL
            R.id.rbBesar -> SettingsManager.UKURAN_BESAR
            else         -> SettingsManager.UKURAN_SEDANG
        }
        settings.simpanUkuran(nilai)
        perbaruiRingkasan()
    }

    private val listenerAksen = RadioGroup.OnCheckedChangeListener { _, id ->
        settings.simpanAksen(
            when (id) {
                R.id.rbAksenBiru -> SettingsManager.AKSEN_BIRU
                R.id.rbAksenUngu -> SettingsManager.AKSEN_UNGU
                else             -> SettingsManager.AKSEN_HIJAU
            }
        )
        terapkanAksen()
        perbaruiRingkasan()
    }

    private val listenerNotifikasi = CompoundButton.OnCheckedChangeListener { _, aktif ->
        when {
            !aktif -> {
                settings.simpanNotifikasi(false)
                NotifikasiHelper.batalkan(this)
                perbaruiRingkasan()
            }
            NotifikasiHelper.punyaIzin(this) -> aktifkanNotifikasi()
            else -> pemintaIzin.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /** Hasil permintaan izin notifikasi (Android 13+). */
    private val pemintaIzin = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { diberi ->
        if (diberi) {
            aktifkanNotifikasi()
        } else {
            Toast.makeText(this, R.string.toast_izin_notif, Toast.LENGTH_SHORT).show()
            setSakelarTanpaListener(false)
        }
    }

    // ====================================================================
    //  SIKLUS HIDUP
    // ====================================================================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pengaturan)

        settings = SettingsManager(this)

        tvRingkasan        = findViewById(R.id.tvRingkasan)
        tvStatusNotifikasi = findViewById(R.id.tvStatusNotifikasi)
        tvStatusAksen      = findViewById(R.id.tvStatusAksen)
        rgBahasa           = findViewById(R.id.rgBahasa)
        rgTema             = findViewById(R.id.rgTema)
        rgUkuran           = findViewById(R.id.rgUkuran)
        rgAksen            = findViewById(R.id.rgAksen)
        swNotifikasi       = findViewById(R.id.swNotifikasi)
        etNama             = findViewById(R.id.etNama)
        btnSimpanNama      = findViewById(R.id.btnSimpanNama)

        // Penyebab bug reset: recreate() otomatis memulihkan pilihan radio/sakelar
        // LAMA dari state tampilan, menimpa nilai baru dari SharedPreferences.
        // Karena itu state tampilan dimatikan; sumber kebenarannya hanya SharedPreferences.
        matikanSimpanState(rgBahasa, rgTema, rgUkuran, rgAksen, swNotifikasi)

        // 1) TAMPILKAN KEADAAN AWAL dari pengaturan yang tersimpan
        isiKeadaanAwal()
        terapkanAksen()

        // 2) BARU pasang listener: setiap perubahan disimpan & diterapkan
        rgBahasa.setOnCheckedChangeListener(listenerBahasa)
        rgTema.setOnCheckedChangeListener(listenerTema)
        rgUkuran.setOnCheckedChangeListener(listenerUkuran)
        rgAksen.setOnCheckedChangeListener(listenerAksen)
        swNotifikasi.setOnCheckedChangeListener(listenerNotifikasi)

        // profil: simpan nama lewat tombol atau tombol "Done" pada keyboard
        btnSimpanNama.setOnClickListener { simpanNama() }
        etNama.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                simpanNama()
                true
            } else false
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener { konfirmasiReset() }
    }

    override fun onResume() {
        super.onResume()
        perbaruiRingkasan()
    }

    /** Mematikan penyimpanan/pemulihan state otomatis pada view (dan anak-anaknya). */
    private fun matikanSimpanState(vararg views: View) {
        for (v in views) {
            v.isSaveEnabled = false
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) matikanSimpanState(v.getChildAt(i))
            }
        }
    }

    // ================= keadaan awal =================
    private fun isiKeadaanAwal() {
        rgBahasa.check(idDariBahasa(settings.ambilBahasa()))

        rgTema.check(
            when (settings.ambilTema()) {
                SettingsManager.TEMA_GELAP  -> R.id.rbGelap
                SettingsManager.TEMA_SISTEM -> R.id.rbTemaSistem
                else                        -> R.id.rbTerang
            }
        )

        rgUkuran.check(
            when (settings.ambilUkuran()) {
                SettingsManager.UKURAN_KECIL -> R.id.rbKecil
                SettingsManager.UKURAN_BESAR -> R.id.rbBesar
                else                         -> R.id.rbSedang
            }
        )

        rgAksen.check(
            when (settings.ambilAksen()) {
                SettingsManager.AKSEN_BIRU -> R.id.rbAksenBiru
                SettingsManager.AKSEN_UNGU -> R.id.rbAksenUngu
                else                       -> R.id.rbAksenHijau
            }
        )

        // Bila izin notifikasi dicabut lewat pengaturan sistem, sakelar kembali mati
        val aktif = settings.ambilNotifikasi() && NotifikasiHelper.punyaIzin(this)
        if (settings.ambilNotifikasi() && !aktif) settings.simpanNotifikasi(false)
        swNotifikasi.isChecked = aktif

        etNama.setText(settings.ambilNama())

        perbaruiRingkasan()
    }

    // ================= bahasa =================
    private fun bahasaDariId(id: Int): String = when (id) {
        R.id.rbEnglish      -> SettingsManager.BAHASA_EN
        R.id.rbBahasaSistem -> SettingsManager.BAHASA_SISTEM
        else                -> SettingsManager.BAHASA_ID
    }

    private fun idDariBahasa(bahasa: String): Int = when (bahasa) {
        SettingsManager.BAHASA_EN     -> R.id.rbEnglish
        SettingsManager.BAHASA_SISTEM -> R.id.rbBahasaSistem
        else                          -> R.id.rbIndonesia
    }

    /** Pengguna membatalkan pergantian bahasa: kembalikan pilihan radio
     *  tanpa memicu listener lagi. */
    private fun pulihkanBahasa() {
        rgBahasa.setOnCheckedChangeListener(null)
        rgBahasa.check(idDariBahasa(settings.ambilBahasa()))
        rgBahasa.setOnCheckedChangeListener(listenerBahasa)
    }

    // ================= notifikasi =================
    private fun aktifkanNotifikasi() {
        settings.simpanNotifikasi(true)
        NotifikasiHelper.tampilkan(this)
        perbaruiRingkasan()
    }

    private fun setSakelarTanpaListener(nilai: Boolean) {
        swNotifikasi.setOnCheckedChangeListener(null)
        swNotifikasi.isChecked = nilai
        swNotifikasi.setOnCheckedChangeListener(listenerNotifikasi)
    }

    // ================= profil =================
    private fun simpanNama() {
        settings.simpanNama(etNama.text.toString())
        val tersimpan = settings.ambilNama()
        etNama.setText(tersimpan)
        etNama.clearFocus()
        ContextCompat.getSystemService(this, InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(etNama.windowToken, 0)
        Toast.makeText(
            this,
            if (tersimpan.isEmpty()) R.string.toast_nama_dihapus else R.string.toast_nama_tersimpan,
            Toast.LENGTH_SHORT
        ).show()
    }

    // ================= warna aksen =================
    /** Menerapkan warna aksen ke radio, sakelar, dan tombol di halaman ini. */
    private fun terapkanAksen() {
        val aksen = AksenHelper.warna(this, settings)
        val netral = ContextCompat.getColor(this, R.color.warna_teks_sekunder)

        listOf(rgBahasa, rgTema, rgUkuran).forEach {
            AksenHelper.tintGrupRadio(it, aksen, netral)
        }

        // pilihan aksen: tiap radio berwarna sesuai warna yang diwakilinya
        listOf(
            R.id.rbAksenHijau to SettingsManager.AKSEN_HIJAU,
            R.id.rbAksenBiru  to SettingsManager.AKSEN_BIRU,
            R.id.rbAksenUngu  to SettingsManager.AKSEN_UNGU
        ).forEach { (id, kode) ->
            val warna = ContextCompat.getColor(this, AksenHelper.resWarna(kode))
            AksenHelper.tintRadio(findViewById<RadioButton>(id), warna, warna)
        }

        AksenHelper.tintSakelar(
            swNotifikasi,
            aktif = aksen,
            jejakMati = ContextCompat.getColor(this, R.color.warna_garis),
            jempolMati = netral
        )
        AksenHelper.tintTombol(btnSimpanNama, aksen)
    }

    // ================= ringkasan pengaturan aktif =================
    private fun perbaruiRingkasan() {
        tvRingkasan.text = RingkasanPengaturan.teks(this, settings)
        tvStatusNotifikasi.text = RingkasanPengaturan.statusNotifikasi(this, settings)
        tvStatusAksen.text = RingkasanPengaturan.statusAksen(this, settings)
    }

    // ================= reset dengan konfirmasi =================
    private fun konfirmasiReset() {
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_reset_judul)
            .setMessage(R.string.dialog_reset_pesan)
            .setNegativeButton(R.string.dialog_batal, null)
            .setPositiveButton(R.string.dialog_ya) { _, _ ->
                settings.reset()                                  // hapus semua pengaturan
                NotifikasiHelper.batalkan(this)
                Toast.makeText(
                    this, getString(R.string.toast_reset), Toast.LENGTH_SHORT
                ).show()
                SettingsManager.terapkanKeAplikasi(this)          // kembali ke bahasa & tema awal
                recreate()                                        // segarkan halaman ini
            }
            .show()
    }
}

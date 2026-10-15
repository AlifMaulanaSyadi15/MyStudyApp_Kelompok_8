package com.example.mystudyapp

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.Button
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.widget.CompoundButtonCompat

/**
 * Pengembangan tambahan: warna aksen yang bisa dipilih pengguna.
 * Warna aksen dipilih saat aplikasi berjalan (bukan lewat @color di layout),
 * jadi diterapkan lewat kode ke tombol, ikon kartu, radio, dan sakelar.
 */
object AksenHelper {

    fun resWarna(kode: String): Int = when (kode) {
        SettingsManager.AKSEN_BIRU -> R.color.aksen_biru
        SettingsManager.AKSEN_UNGU -> R.color.aksen_ungu
        else                       -> R.color.aksen_hijau
    }

    /** warna aksen yang sedang dipilih (sudah mengikuti mode terang/gelap) */
    fun warna(context: Context, s: SettingsManager): Int =
        ContextCompat.getColor(context, resWarna(s.ambilAksen()))

    /** tulisan gelap di atas aksen terang, tulisan putih di atas aksen gelap */
    fun warnaDiAtas(latar: Int): Int =
        if (ColorUtils.calculateLuminance(latar) > 0.4) 0xFF16213E.toInt() else Color.WHITE

    fun tintTombol(tombol: Button, warna: Int) {
        tombol.backgroundTintList = ColorStateList.valueOf(warna)
        tombol.setTextColor(warnaDiAtas(warna))
    }

    /** latar kotak ikon kartu: aksen dengan transparansi rendah */
    fun tintIkon(ikon: TextView, warna: Int) {
        val latar = ikon.background.mutate()
        if (latar is GradientDrawable) {
            latar.setColor(ColorUtils.setAlphaComponent(warna, 0x33))
        }
    }

    fun tintRadio(radio: CompoundButton, aktif: Int, tidakAktif: Int) {
        val state = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(aktif, tidakAktif)
        )
        CompoundButtonCompat.setButtonTintList(radio, state)
    }

    fun tintGrupRadio(grup: RadioGroup, aktif: Int, tidakAktif: Int) {
        for (i in 0 until grup.childCount) {
            val anak = grup.getChildAt(i)
            if (anak is CompoundButton) tintRadio(anak, aktif, tidakAktif)
        }
    }

    fun tintSakelar(sakelar: SwitchCompat, aktif: Int, jejakMati: Int, jempolMati: Int) {
        val checked = intArrayOf(android.R.attr.state_checked)
        sakelar.thumbTintList = ColorStateList(
            arrayOf(checked, intArrayOf()), intArrayOf(aktif, jempolMati)
        )
        sakelar.trackTintList = ColorStateList(
            arrayOf(checked, intArrayOf()),
            intArrayOf(ColorUtils.setAlphaComponent(aktif, 0x66), jejakMati)
        )
    }
}

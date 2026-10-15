package com.example.mystudyapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Pengembangan tambahan: notifikasi sungguhan.
 * Saat sakelar Notifikasi dinyalakan, aplikasi meminta izin (Android 13+)
 * lalu menampilkan satu notifikasi konfirmasi.
 */
object NotifikasiHelper {

    private const val CHANNEL_ID = "pengingat_mystudy"
    private const val ID_NOTIFIKASI = 1001

    /** Android 13 (API 33) ke atas mewajibkan izin POST_NOTIFICATIONS. */
    fun perluMinta(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    fun punyaIzin(context: Context): Boolean =
        !perluMinta() || ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

    private fun buatChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notif_channel_nama),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = context.getString(R.string.notif_channel_desc) }
            context.getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission") // izin sudah diperiksa lewat punyaIzin()
    fun tampilkan(context: Context) {
        if (!punyaIzin(context)) return
        buatChannel(context)

        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notifikasi = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif)
            .setContentTitle(context.getString(R.string.notif_judul))
            .setContentText(context.getString(R.string.notif_isi))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(ID_NOTIFIKASI, notifikasi)
    }

    fun batalkan(context: Context) {
        NotificationManagerCompat.from(context).cancel(ID_NOTIFIKASI)
    }
}

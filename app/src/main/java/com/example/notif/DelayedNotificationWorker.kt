package com.example.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class DelayedNotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val channelId = "test_notification_channel"
        val notificationId = 2

        createNotificationChannelIfNeeded()

        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText("Notifikasi ini muncul setelah 10 detik, bahkan setelah aplikasi ditutup! Ini adalah notifikasi yang dijadwalkan menggunakan WorkManager.")
            .setBigContentTitle("Notifikasi Tertunda")
            .setSummaryText("Kotlin Demo Notification")

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Notification Alert")
            .setContentText("Kotlin Demo Notification")
            .setStyle(bigTextStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                applicationContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, notification)
        }

        return Result.success()
    }

    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "test_notification_channel"
            val channel = NotificationChannel(
                channelId,
                "Test Notification Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel untuk testing notifikasi"
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

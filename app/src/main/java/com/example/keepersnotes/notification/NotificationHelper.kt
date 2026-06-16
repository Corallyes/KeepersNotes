package com.example.keepersnotes.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.keepersnotes.MainActivity
import com.example.keepersnotes.R

object NotificationHelper {

    const val CHANNEL_ID_ALARM = "keepers_notes_alarm"
    const val CHANNEL_ID_SYSTEM = "keepers_notes_system"
    const val CHANNEL_NAME_ALARM = "闹钟提醒"
    const val CHANNEL_NAME_SYSTEM = "系统通知"

    fun createNotificationChannels(context: Context) {
        val alarmChannel = NotificationChannel(
            CHANNEL_ID_ALARM,
            CHANNEL_NAME_ALARM,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "开团闹钟提醒"
            enableVibration(true)
            setBypassDnd(true)
        }

        val systemChannel = NotificationChannel(
            CHANNEL_ID_SYSTEM,
            CHANNEL_NAME_SYSTEM,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "开团系统通知提醒"
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(alarmChannel)
        notificationManager.createNotificationChannel(systemChannel)
    }

    fun showNotification(
        context: Context,
        channelId: String,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(
                if (channelId == CHANNEL_ID_ALARM) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notificationId, notification)
    }
}

package com.example.keepersnotes.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.keepersnotes.MainActivity
import com.example.keepersnotes.R
import com.example.keepersnotes.util.LocalizedStrings

object NotificationHelper {

    const val CHANNEL_ID_SYSTEM = "keepers_notes_system"

    fun createNotificationChannels(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val systemChannel = NotificationChannel(
            CHANNEL_ID_SYSTEM,
            LocalizedStrings.channelNameSystem,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = LocalizedStrings.channelDescSystem
        }
        notificationManager.createNotificationChannel(systemChannel)
    }

    fun showNotification(
        context: Context,
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

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SYSTEM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(notificationId, notification)
    }

    // ==================== Memo reminder scheduling ====================

    fun scheduleMemoNotification(
        context: Context,
        notificationId: Long,
        title: String,
        content: String,
        triggerTime: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_MESSAGE, content)
            putExtra(AlarmReceiver.EXTRA_IS_ALARM, false)
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, notificationId.toInt())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val showIntent = PendingIntent.getActivity(
                context, notificationId.toInt() + 20000,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerTime, showIntent),
                pendingIntent
            )
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelMemoNotification(context: Context, notificationId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, notificationId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }
}

package com.example.keepersnotes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "开团提醒"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "即将开团"
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: NotificationHelper.CHANNEL_ID_ALARM
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, System.currentTimeMillis().toInt())

        NotificationHelper.showNotification(
            context = context,
            channelId = channelId,
            title = title,
            message = message,
            notificationId = notificationId
        )
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_CHANNEL_ID = "extra_channel_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}

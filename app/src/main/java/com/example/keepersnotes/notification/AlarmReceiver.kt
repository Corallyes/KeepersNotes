package com.example.keepersnotes.notification

import android.provider.AlarmClock
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.keepersnotes.util.LocalizedStrings
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val isAlarm = intent.getBooleanExtra(EXTRA_IS_ALARM, false)

        if (isAlarm) {
            // Open system alarm app with the event time
            val eventTime = intent.getLongExtra(EXTRA_EVENT_TIME, 0L)
            val title = intent.getStringExtra(EXTRA_TITLE) ?: LocalizedStrings.defaultAlarmTitle

            if (eventTime > 0L) {
                try {
                    val cal = Calendar.getInstance().apply { timeInMillis = eventTime }
                    val alarmIntent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, cal.get(Calendar.HOUR_OF_DAY))
                        putExtra(AlarmClock.EXTRA_MINUTES, cal.get(Calendar.MINUTE))
                        putExtra(AlarmClock.EXTRA_MESSAGE, title)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(alarmIntent)
                } catch (_: Exception) {
                    // Fallback: show system notification if alarm app unavailable
                    NotificationHelper.showNotification(
                        context = context,
                        title = title,
                        message = LocalizedStrings.defaultAlarmMessage,
                        notificationId = System.currentTimeMillis().toInt()
                    )
                }
            }
        } else {
            // System notification
            val title = intent.getStringExtra(EXTRA_TITLE) ?: LocalizedStrings.defaultAlarmTitle
            val message = intent.getStringExtra(EXTRA_MESSAGE) ?: LocalizedStrings.defaultAlarmMessage
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, System.currentTimeMillis().toInt())

            NotificationHelper.showNotification(
                context = context,
                title = title,
                message = message,
                notificationId = notificationId
            )
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_IS_ALARM = "extra_is_alarm"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_EVENT_TIME = "extra_event_time"
    }
}

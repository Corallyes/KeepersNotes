package com.example.keepersnotes.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.keepersnotes.MainActivity
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import com.example.keepersnotes.util.LocalizedStrings
import java.util.*
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val PREF_NAME = "notification_settings"
    private const val KEY_ALARM_ENABLED = "alarm_enabled"
    private const val KEY_ALARM_MINUTES = "alarm_minutes"
    private const val KEY_SYSTEM_ENABLED = "system_enabled"
    private const val KEY_SYSTEM_MINUTES = "system_minutes"

    fun getSettings(context: Context): NotificationSettings {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return NotificationSettings(
            alarmEnabled = prefs.getBoolean(KEY_ALARM_ENABLED, false),
            alarmMinutesBefore = prefs.getInt(KEY_ALARM_MINUTES, 30),
            systemEnabled = prefs.getBoolean(KEY_SYSTEM_ENABLED, true),
            systemMinutesBefore = prefs.getInt(KEY_SYSTEM_MINUTES, 15)
        )
    }

    fun saveSettings(context: Context, settings: NotificationSettings) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putBoolean(KEY_ALARM_ENABLED, settings.alarmEnabled)
            putInt(KEY_ALARM_MINUTES, settings.alarmMinutesBefore)
            putBoolean(KEY_SYSTEM_ENABLED, settings.systemEnabled)
            putInt(KEY_SYSTEM_MINUTES, settings.systemMinutesBefore)
            apply()
        }
    }

    fun scheduleReminders(context: Context, events: List<CalendarEventEntity>) {
        val settings = getSettings(context)
        cancelAllReminders(context)

        if (!settings.alarmEnabled && !settings.systemEnabled) return

        events.forEach { event ->
            if (event.isRemindEnabled) {
                val eventTime = parseEventTime(event) ?: return@forEach

                if (settings.alarmEnabled) {
                    scheduleAlarm(context, event, eventTime, settings.alarmMinutesBefore)
                }

                if (settings.systemEnabled) {
                    scheduleWork(context, event, eventTime, settings.systemMinutesBefore)
                }
            }
        }
    }

    private fun parseEventTime(event: CalendarEventEntity): Long? {
        val dateCal = Calendar.getInstance().apply { timeInMillis = event.date }

        event.time?.let { timeStr ->
            try {
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    dateCal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                    dateCal.set(Calendar.MINUTE, parts[1].toInt())
                    dateCal.set(Calendar.SECOND, 0)
                    return dateCal.timeInMillis
                }
            } catch (_: Exception) {}
        }

        // Default to 9:00 AM if no time specified
        dateCal.set(Calendar.HOUR_OF_DAY, 9)
        dateCal.set(Calendar.MINUTE, 0)
        dateCal.set(Calendar.SECOND, 0)
        return dateCal.timeInMillis
    }

    private fun scheduleAlarm(context: Context, event: CalendarEventEntity, eventTime: Long, minutesBefore: Int) {
        val triggerTime = eventTime - minutesBefore * 60 * 1000L
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TITLE, event.title)
            putExtra(AlarmReceiver.EXTRA_IS_ALARM, true)
            putExtra(AlarmReceiver.EXTRA_NOTIFICATION_ID, event.eventId.hashCode())
            putExtra(AlarmReceiver.EXTRA_EVENT_TIME, eventTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        try {
            val showIntent = PendingIntent.getActivity(
                context, event.eventId.hashCode() + 20000,
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

    private fun scheduleWork(context: Context, event: CalendarEventEntity, eventTime: Long, minutesBefore: Int) {
        val delay = eventTime - minutesBefore * 60 * 1000L - System.currentTimeMillis()
        if (delay <= 0) return

        val data = workDataOf(
            "title" to LocalizedStrings.systemNotificationTitle,
            "message" to LocalizedStrings.alarmReminderMessage(event.title, minutesBefore),
            "notification_id" to (event.eventId.hashCode() + 1)
        )

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder_${event.eventId}")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun cancelAllReminders(context: Context) {
        // Cancel alarms
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }

        // Cancel work
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder")
    }

    fun cancelReminderForEvent(context: Context, eventId: String) {
        // Cancel alarm
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, eventId.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager.cancel(it)
        }

        // Cancel work
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$eventId")
    }
}

data class NotificationSettings(
    val alarmEnabled: Boolean = false,
    val alarmMinutesBefore: Int = 30,
    val systemEnabled: Boolean = true,
    val systemMinutesBefore: Int = 15
)

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        val title = inputData.getString("title") ?: return androidx.work.ListenableWorker.Result.failure()
        val message = inputData.getString("message") ?: return androidx.work.ListenableWorker.Result.failure()
        val notificationId = inputData.getInt("notification_id", 0)

        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            message = message,
            notificationId = notificationId
        )

        return androidx.work.ListenableWorker.Result.success()
    }
}

package com.example.keepersnotes.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.keepersnotes.data.local.database.AppDatabase
import com.example.keepersnotes.util.LocalizedStrings
import kotlinx.coroutines.flow.first

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationHelper.createNotificationChannels(context)
            val workRequest = OneTimeWorkRequestBuilder<BootReminderRescheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}

class BootReminderRescheduleWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val db = Room.databaseBuilder(context, AppDatabase::class.java, "keepers_notes.db")
            .fallbackToDestructiveMigration()
            .build()

        try {
            // 1. Reschedule calendar event reminders
            val now = System.currentTimeMillis()
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.MONTH, 6)
            val endDate = cal.timeInMillis

            val events = db.calendarEventDao().getEventsBetween(now, endDate).first()
            ReminderScheduler.scheduleReminders(context, events)

            // 2. Reschedule KP memo reminders
            val memos = db.kpMemoDao().getAllMemos().first()
            for (memo in memos) {
                if (memo.isNotificationEnabled && memo.notificationTime != null && memo.notificationTime > now) {
                    NotificationHelper.scheduleMemoNotification(
                        context = context,
                        notificationId = memo.notificationId.toLong(),
                        title = memo.title.ifBlank { LocalizedStrings.memoReminderTitle },
                        content = memo.content.take(100),
                        triggerTime = memo.notificationTime
                    )
                }
            }
        } catch (_: Exception) {
            // DB not available yet, will retry on next boot
        }

        return Result.success()
    }
}

package com.example.keepersnotes

import android.app.Application
import com.example.keepersnotes.notification.NotificationHelper
import com.example.keepersnotes.util.KpPreferences
import com.example.keepersnotes.util.ThemePreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KeeperNotesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        ThemePreferences.init(this)
        KpPreferences.init(this)
    }
}

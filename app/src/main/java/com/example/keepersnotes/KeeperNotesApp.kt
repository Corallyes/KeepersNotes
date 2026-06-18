package com.example.keepersnotes

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.keepersnotes.notification.NotificationHelper
import com.example.keepersnotes.util.KpPreferences
import com.example.keepersnotes.util.ThemePreferences
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KeeperNotesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Chaquopy Python runtime
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        NotificationHelper.createNotificationChannels(this)
        ThemePreferences.init(this)
        KpPreferences.init(this)
    }
}

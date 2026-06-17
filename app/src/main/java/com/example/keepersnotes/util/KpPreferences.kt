package com.example.keepersnotes.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.io.File

object KpPreferences {
    private const val PREF_NAME = "kp_preferences"
    private const val KEY_NICKNAME = "kp_nickname"
    private const val KEY_AVATAR_URI = "kp_avatar_uri"
    private const val AVATAR_FILENAME = "kp_avatar.jpg"

    private lateinit var prefs: SharedPreferences
    private lateinit var appContext: Context

    private val _nickname = mutableStateOf("KP")
    val nickname: String by _nickname

    private val _avatarUri = mutableStateOf<String?>(null)
    val avatarUri: String? by _avatarUri

    fun init(context: Context) {
        appContext = context.applicationContext
        prefs = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        _nickname.value = prefs.getString(KEY_NICKNAME, "KP") ?: "KP"
        _avatarUri.value = prefs.getString(KEY_AVATAR_URI, null)
    }

    fun setNickname(name: String) {
        _nickname.value = name
        prefs.edit().putString(KEY_NICKNAME, name).apply()
    }

    fun saveAvatarFromUri(sourceUri: Uri): Boolean {
        return try {
            val destFile = File(appContext.filesDir, AVATAR_FILENAME)
            // 删除旧头像
            if (destFile.exists()) destFile.delete()
            appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val savedPath = destFile.absolutePath
            _avatarUri.value = savedPath
            prefs.edit().putString(KEY_AVATAR_URI, savedPath).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clearAvatar() {
        _avatarUri.value = null
        prefs.edit().remove(KEY_AVATAR_URI).apply()
        try {
            File(appContext.filesDir, AVATAR_FILENAME).delete()
        } catch (_: Exception) {}
    }
}

package com.example.keepersnotes.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object ThemePreferences {
    private const val PREF_NAME = "theme_preferences"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_AUTO_SAVE = "auto_save_enabled"
    private const val KEY_LANGUAGE = "language_mode"

    const val LANGUAGE_SYSTEM = 0
    const val LANGUAGE_CHINESE = 1
    const val LANGUAGE_ENGLISH = 2

    const val THEME_SYSTEM = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    private lateinit var prefs: SharedPreferences
    private val _currentTheme = mutableStateOf(THEME_SYSTEM)
    val currentTheme: Int by _currentTheme

    private val _currentLanguage = mutableStateOf(LANGUAGE_SYSTEM)
    val currentLanguage: Int by _currentLanguage

    private val _autoSaveEnabled = mutableStateOf(true)
    val autoSaveEnabled: Boolean by _autoSaveEnabled

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        _currentTheme.value = prefs.getInt(KEY_THEME_MODE, THEME_SYSTEM)
        _autoSaveEnabled.value = prefs.getBoolean(KEY_AUTO_SAVE, true)
        _currentLanguage.value = prefs.getInt(KEY_LANGUAGE, LANGUAGE_SYSTEM)
    }

    fun setThemeMode(mode: Int) {
        _currentTheme.value = mode
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun setAutoSaveEnabled(enabled: Boolean) {
        _autoSaveEnabled.value = enabled
        prefs.edit().putBoolean(KEY_AUTO_SAVE, enabled).apply()
    }

    fun setLanguage(language: Int) {
        _currentLanguage.value = language
        prefs.edit().putInt(KEY_LANGUAGE, language).apply()
    }

    fun isDarkTheme(isSystemDarkTheme: Boolean): Boolean {
        return when (_currentTheme.value) {
            THEME_LIGHT -> false
            THEME_DARK -> true
            else -> isSystemDarkTheme
        }
    }
}

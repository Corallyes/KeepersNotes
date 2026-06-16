package com.example.keepersnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.keepersnotes.navigation.BottomNavBar
import com.example.keepersnotes.navigation.KeeperNotesNavGraph
import com.example.keepersnotes.navigation.screen.Screen
import com.example.keepersnotes.ui.theme.KeepersNotesTheme
import com.example.keepersnotes.util.ThemePreferences
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyLanguage(ThemePreferences.currentLanguage)
        enableEdgeToEdge()
        setContent {
            KeepersNotesTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val showBottomBar = currentRoute !in listOf(
                    Screen.Splash.route,
                    Screen.Brand.route
                )

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController)
                        }
                    }
                ) { innerPadding ->
                    KeeperNotesNavGraph(
                        navController = navController,
                        innerPadding = innerPadding
                    )
                }
            }
        }
    }

    private fun applyLanguage(language: Int) {
        val localeList = when (language) {
            ThemePreferences.LANGUAGE_CHINESE -> LocaleListCompat.forLanguageTags("zh")
            ThemePreferences.LANGUAGE_ENGLISH -> LocaleListCompat.forLanguageTags("en")
            else -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(localeList)
    }
}

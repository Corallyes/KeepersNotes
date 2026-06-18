package com.example.keepersnotes.ui.screen.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.util.LocalizedStrings
import com.example.keepersnotes.util.ThemePreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToLicense: () -> Unit = {}
) {
    var themeMode by remember { mutableStateOf(ThemePreferences.currentTheme) }
    var languageMode by remember { mutableStateOf(ThemePreferences.currentLanguage) }
    var autoSave by remember { mutableStateOf(ThemePreferences.autoSaveEnabled) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = LocalizedStrings.settingsTitle,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                LocalizedStrings.settingsAppearance,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text(LocalizedStrings.settingsTheme) },
                supportingContent = {
                    Text(
                        when (themeMode) {
                            ThemePreferences.THEME_LIGHT -> LocalizedStrings.settingsLight
                            ThemePreferences.THEME_DARK -> LocalizedStrings.settingsDark
                            else -> LocalizedStrings.settingsFollowSystem
                        }
                    )
                },
                leadingContent = { Icon(Icons.Default.Palette, contentDescription = null) },
                trailingContent = {
                    ThemeSelector(
                        selectedMode = themeMode,
                        onModeSelected = { mode ->
                            themeMode = mode
                            ThemePreferences.setThemeMode(mode)
                        }
                    )
                }
            )

            ListItem(
                headlineContent = { Text(LocalizedStrings.settingsLanguage) },
                supportingContent = {
                    Text(
                        when (languageMode) {
                            ThemePreferences.LANGUAGE_CHINESE -> LocalizedStrings.languageChinese
                            ThemePreferences.LANGUAGE_ENGLISH -> LocalizedStrings.languageEnglish
                            else -> LocalizedStrings.settingsFollowSystem
                        }
                    )
                },
                leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                trailingContent = {
                    LanguageSelector(
                        selectedLanguage = languageMode,
                        onLanguageSelected = { lang ->
                            languageMode = lang
                            ThemePreferences.setLanguage(lang)
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Text(
                LocalizedStrings.settingsNotification,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text(LocalizedStrings.settingsNotification) },
                supportingContent = { Text(LocalizedStrings.settingsNotificationDesc) },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                trailingContent = {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.clickable(onClick = onNavigateToNotificationSettings)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Text(
                LocalizedStrings.settingsData,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text(LocalizedStrings.settingsAutoSave) },
                supportingContent = { Text(LocalizedStrings.settingsAutoSaveDesc) },
                leadingContent = { Icon(Icons.Default.Save, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = autoSave,
                        onCheckedChange = {
                            autoSave = it
                            ThemePreferences.setAutoSaveEnabled(it)
                        }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Text(
                LocalizedStrings.settingsAbout,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text(LocalizedStrings.settingsVersion) },
                supportingContent = { Text("v1.1") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
            )

            ListItem(
                headlineContent = { Text(LocalizedStrings.settingsLicense) },
                leadingContent = { Icon(Icons.Default.Code, contentDescription = null) },
                trailingContent = {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.clickable(onClick = onNavigateToLicense)
            )
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        onClick = { expanded = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = when (selectedMode) {
                    ThemePreferences.THEME_LIGHT -> LocalizedStrings.settingsLightShort
                    ThemePreferences.THEME_DARK -> LocalizedStrings.settingsDarkShort
                    else -> LocalizedStrings.settingsFollowSystem
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(LocalizedStrings.settingsFollowSystem) },
                onClick = {
                    onModeSelected(ThemePreferences.THEME_SYSTEM)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(LocalizedStrings.settingsLight) },
                onClick = {
                    onModeSelected(ThemePreferences.THEME_LIGHT)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(LocalizedStrings.settingsDark) },
                onClick = {
                    onModeSelected(ThemePreferences.THEME_DARK)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: Int,
    onLanguageSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        onClick = { expanded = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = when (selectedLanguage) {
                    ThemePreferences.LANGUAGE_CHINESE -> LocalizedStrings.languageChinese
                    ThemePreferences.LANGUAGE_ENGLISH -> "EN"
                    else -> LocalizedStrings.settingsFollowSystem
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(LocalizedStrings.settingsFollowSystem) },
                onClick = {
                    onLanguageSelected(ThemePreferences.LANGUAGE_SYSTEM)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(LocalizedStrings.languageChinese) },
                onClick = {
                    onLanguageSelected(ThemePreferences.LANGUAGE_CHINESE)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(LocalizedStrings.languageEnglish) },
                onClick = {
                    onLanguageSelected(ThemePreferences.LANGUAGE_ENGLISH)
                    expanded = false
                }
            )
        }
    }
}

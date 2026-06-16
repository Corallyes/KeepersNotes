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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.util.ThemePreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit = {},
    onNavigateToLicense: () -> Unit = {}
) {
    var themeMode by remember { mutableStateOf(ThemePreferences.currentTheme) }
    var autoSave by remember { mutableStateOf(ThemePreferences.autoSaveEnabled) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "设置",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            // Appearance section
            Text(
                "外观",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("外观模式") },
                supportingContent = {
                    Text(
                        when (themeMode) {
                            ThemePreferences.THEME_LIGHT -> "浅色模式"
                            ThemePreferences.THEME_DARK -> "深色模式"
                            else -> "跟随系统"
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

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Notification section
            Text(
                "通知",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("通知提醒") },
                supportingContent = { Text("设置闹钟和通知提醒方式") },
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

            // Data section
            Text(
                "数据",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("自动保存") },
                supportingContent = { Text("编辑后自动保存数据") },
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

            // About section
            Text(
                "关于",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("版本") },
                supportingContent = { Text("v1.1") },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
            )

            ListItem(
                headlineContent = { Text("开源许可") },
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
            Icon(
                imageVector = when (selectedMode) {
                    ThemePreferences.THEME_LIGHT -> Icons.Default.LightMode
                    ThemePreferences.THEME_DARK -> Icons.Default.DarkMode
                    else -> Icons.Default.SettingsBrightness
                },
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = when (selectedMode) {
                    ThemePreferences.THEME_LIGHT -> "浅色"
                    ThemePreferences.THEME_DARK -> "深色"
                    else -> "跟随系统"
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
                text = { Text("跟随系统") },
                onClick = {
                    onModeSelected(ThemePreferences.THEME_SYSTEM)
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Default.SettingsBrightness, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("浅色模式") },
                onClick = {
                    onModeSelected(ThemePreferences.THEME_LIGHT)
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Default.LightMode, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("深色模式") },
                onClick = {
                    onModeSelected(ThemePreferences.THEME_DARK)
                    expanded = false
                },
                leadingIcon = { Icon(Icons.Default.DarkMode, contentDescription = null) }
            )
        }
    }
}

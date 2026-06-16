package com.example.keepersnotes.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.notification.NotificationSettings
import com.example.keepersnotes.notification.ReminderScheduler
import com.example.keepersnotes.ui.component.CompactTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(ReminderScheduler.getSettings(context)) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "通知提醒",
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
            // Alarm section
            Text(
                "闹钟提醒",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("开启闹钟提醒") },
                supportingContent = { Text("使用系统闹钟进行提醒，声音较大，适合重要日程") },
                leadingContent = { Icon(Icons.Default.Alarm, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = settings.alarmEnabled,
                        onCheckedChange = {
                            settings = settings.copy(alarmEnabled = it)
                            ReminderScheduler.saveSettings(context, settings)
                        }
                    )
                }
            )

            if (settings.alarmEnabled) {
                ListItem(
                    headlineContent = { Text("提前提醒时间") },
                    supportingContent = { Text("开团前多少分钟提醒") },
                    leadingContent = { Icon(Icons.Default.Timer, contentDescription = null) },
                    trailingContent = {
                        MinutesSelector(
                            value = settings.alarmMinutesBefore,
                            onValueChange = {
                                settings = settings.copy(alarmMinutesBefore = it)
                                ReminderScheduler.saveSettings(context, settings)
                            }
                        )
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // System notification section
            Text(
                "系统通知提醒",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text("开启通知提醒") },
                supportingContent = { Text("使用系统通知栏提醒，较为温和") },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = settings.systemEnabled,
                        onCheckedChange = {
                            settings = settings.copy(systemEnabled = it)
                            ReminderScheduler.saveSettings(context, settings)
                        }
                    )
                }
            )

            if (settings.systemEnabled) {
                ListItem(
                    headlineContent = { Text("提前提醒时间") },
                    supportingContent = { Text("开团前多少分钟提醒") },
                    leadingContent = { Icon(Icons.Default.Timer, contentDescription = null) },
                    trailingContent = {
                        MinutesSelector(
                            value = settings.systemMinutesBefore,
                            onValueChange = {
                                settings = settings.copy(systemMinutesBefore = it)
                                ReminderScheduler.saveSettings(context, settings)
                            }
                        )
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Info section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "提醒说明：\n" +
                                "• 闹钟提醒：使用系统闹钟，会发出声音和震动\n" +
                                "• 系统通知：在通知栏显示静默通知\n" +
                                "• 只有开启了「提醒」的日程才会触发提醒\n" +
                                "• 创建团时设置的开团日和结束日默认开启提醒",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MinutesSelector(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val options = listOf(5, 10, 15, 30, 60)
    var expanded by remember { mutableStateOf(false) }

    OutlinedCard(
        onClick = { expanded = true }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${value}分钟",
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
            options.forEach { minutes ->
                DropdownMenuItem(
                    text = { Text("${minutes}分钟") },
                    onClick = {
                        onValueChange(minutes)
                        expanded = false
                    }
                )
            }
        }
    }
}

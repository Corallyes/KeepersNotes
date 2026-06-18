package com.example.keepersnotes.ui.screen.profile

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.keepersnotes.notification.NotificationSettings
import com.example.keepersnotes.notification.ReminderScheduler
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.util.LocalizedStrings

private fun canScheduleExactAlarms(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }
    return true
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(ReminderScheduler.getSettings(context)) }

    // 通知权限状态
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // 闹钟权限状态
    var hasAlarmPermission by remember { mutableStateOf(canScheduleExactAlarms(context)) }

    // 通知权限请求回调
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    // 闹钟设置页返回回调（用于刷新闹钟权限状态）
    val alarmSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        hasAlarmPermission = canScheduleExactAlarms(context)
    }

    // 每次进入页面时重新检查权限
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        hasAlarmPermission = canScheduleExactAlarms(context)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = LocalizedStrings.notificationTitle,
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
            // ===== 闹钟提醒 =====
            Text(
                LocalizedStrings.notificationAlarm,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text(LocalizedStrings.notificationAlarmEnable) },
                supportingContent = { Text(LocalizedStrings.notificationAlarmDesc) },
                leadingContent = { Icon(Icons.Default.Alarm, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = settings.alarmEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !hasAlarmPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                alarmSettingsLauncher.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                            }
                            settings = settings.copy(alarmEnabled = enabled)
                            ReminderScheduler.saveSettings(context, settings)
                        }
                    )
                }
            )

            // 闹钟权限未授予时显示提示
            if (settings.alarmEnabled && !hasAlarmPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                LocalizedStrings.alarmPermissionTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                LocalizedStrings.alarmPermissionDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    alarmSettingsLauncher.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(LocalizedStrings.alarmPermissionGrant)
                            }
                        }
                    }
                }
            }

            if (settings.alarmEnabled) {
                ListItem(
                    headlineContent = { Text(LocalizedStrings.notificationAdvanceTime) },
                    supportingContent = { Text(LocalizedStrings.notificationAdvanceDesc) },
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

            // ===== 系统通知 =====
            Text(
                LocalizedStrings.notificationSystem,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            ListItem(
                headlineContent = { Text(LocalizedStrings.notificationSystemEnable) },
                supportingContent = { Text(LocalizedStrings.notificationSystemDesc) },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = settings.systemEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            settings = settings.copy(systemEnabled = enabled)
                            ReminderScheduler.saveSettings(context, settings)
                        }
                    )
                }
            )

            // 通知权限未授予时显示提示
            if (settings.systemEnabled && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                LocalizedStrings.notificationPermissionTitle,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                LocalizedStrings.notificationPermissionDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(LocalizedStrings.notificationPermissionGrant)
                            }
                        }
                    }
                }
            }

            if (settings.systemEnabled) {
                ListItem(
                    headlineContent = { Text(LocalizedStrings.notificationAdvanceTime) },
                    supportingContent = { Text(LocalizedStrings.notificationAdvanceDesc) },
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
                        LocalizedStrings.notificationInfoText,
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
                text = LocalizedStrings.notificationMinutes(value),
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
                    text = { Text(LocalizedStrings.notificationMinutes(minutes)) },
                    onClick = {
                        onValueChange(minutes)
                        expanded = false
                    }
                )
            }
        }
    }
}

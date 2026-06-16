package com.example.keepersnotes.ui.screen.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.component.CompactTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToAnnouncement: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {}
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(title = "我的")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // User info section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar placeholder
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "KP",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "守密人笔记 用户",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider()

            // Menu items
            ProfileMenuItem(
                icon = Icons.Default.Campaign,
                label = "公告",
                subtitle = "查看最新公告和更新日志",
                onClick = onNavigateToAnnouncement
            )

            ProfileMenuItem(
                icon = Icons.Default.Backup,
                label = "数据备份",
                subtitle = "导出/导入数据",
                onClick = onNavigateToBackup
            )

            ProfileMenuItem(
                icon = Icons.Default.Settings,
                label = "设置",
                subtitle = "主题、通知等",
                onClick = onNavigateToSettings
            )

            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                label = "帮助中心",
                subtitle = "常见问题与反馈",
                onClick = onNavigateToHelp
            )

            Spacer(modifier = Modifier.weight(1f))

            // App version
            Text(
                text = "守密人笔记 v1.1",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    subtitle: String = "",
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = if (subtitle.isNotBlank()) {
            { Text(subtitle, style = MaterialTheme.typography.bodySmall) }
        } else null,
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

package com.example.keepersnotes.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.component.CompactTopBar

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val type: AnnouncementType = AnnouncementType.INFO
)

enum class AnnouncementType {
    INFO, UPDATE, IMPORTANT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(
    onBack: () -> Unit
) {
    // 示例公告数据
    val announcements = remember {
        listOf(
            Announcement(
                id = "1",
                title = "欢迎使用守密人笔记 v1.1",
                content = "本次更新新增了以下功能：\n• 日历日程管理\n• 通知提醒功能（闹钟提醒和系统通知）\n• 团关系网络\n• 模组实体管理（NPC、地点、组织、线索）\n\n感谢您的使用，如有问题请在帮助中心反馈。",
                date = "2026-06-16",
                type = AnnouncementType.UPDATE
            ),
            Announcement(
                id = "2",
                title = "通知提醒功能上线",
                content = "现在您可以在「设置 → 通知提醒」中配置提醒方式：\n• 闹钟提醒：使用系统闹钟，声音较大\n• 系统通知：在通知栏显示静默通知\n\n两种提醒可独立设置提前时间（5-60分钟）。",
                date = "2026-06-16",
                type = AnnouncementType.INFO
            )
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "公告",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (announcements.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "暂无公告",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(announcements, key = { it.id }) { announcement ->
                    AnnouncementCard(announcement = announcement)
                }
            }
        }
    }
}

@Composable
private fun AnnouncementCard(announcement: Announcement) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (announcement.type) {
                            AnnouncementType.UPDATE -> Icons.Default.SystemUpdate
                            AnnouncementType.IMPORTANT -> Icons.Default.PriorityHigh
                            AnnouncementType.INFO -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (announcement.type) {
                            AnnouncementType.UPDATE -> MaterialTheme.colorScheme.primary
                            AnnouncementType.IMPORTANT -> MaterialTheme.colorScheme.error
                            AnnouncementType.INFO -> MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = announcement.date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

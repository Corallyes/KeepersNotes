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
import com.example.keepersnotes.util.AnnouncementData
import com.example.keepersnotes.util.LocalizedStrings

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val type: AnnouncementType = AnnouncementType.INFO
)

enum class AnnouncementType {
    INFO, UPDATE, IMPORTANT, WARNING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(
    onBack: () -> Unit
) {
    val latestAnnouncements = AnnouncementData.latestAnnouncements
    val updateLogs = AnnouncementData.updateLogs

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = LocalizedStrings.announcementTitle,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 最新公告
            item {
                Text(LocalizedStrings.announcementLatest, style = MaterialTheme.typography.titleLarge)
            }
            if (latestAnnouncements.isEmpty()) {
                item {
                    Text(
                        LocalizedStrings.announcementNoLatest,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(latestAnnouncements, key = { it.id }) { announcement ->
                    AnnouncementCard(announcement = announcement)
                }
            }

            // 更新日志
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(LocalizedStrings.announcementChangelog, style = MaterialTheme.typography.titleLarge)
            }
            if (updateLogs.isEmpty()) {
                item {
                    Text(
                        LocalizedStrings.announcementNoChangelog,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(updateLogs, key = { it.id }) { announcement ->
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
                            AnnouncementType.WARNING -> Icons.Default.Warning
                            AnnouncementType.INFO -> Icons.Default.Info
                        },
                        contentDescription = null,
                        tint = when (announcement.type) {
                            AnnouncementType.UPDATE -> MaterialTheme.colorScheme.primary
                            AnnouncementType.IMPORTANT -> MaterialTheme.colorScheme.error
                            AnnouncementType.WARNING -> MaterialTheme.colorScheme.error
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

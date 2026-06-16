package com.example.keepersnotes.ui.screen.groupdetail.tab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.GroupEntity
import com.example.keepersnotes.ui.component.GroupStatusBadge
import com.example.keepersnotes.ui.component.StatsCard
import com.example.keepersnotes.ui.screen.groupdetail.GroupDetailUiState
import com.example.keepersnotes.util.Constants

@Composable
fun GroupOverviewTab(
    uiState: GroupDetailUiState,
    onStatusChange: (String) -> Unit
) {
    val group = uiState.group
    val dateFormat = remember { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Group info card
        group?.let { g ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = g.groupName,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                        GroupStatusBadge(status = g.status)
                    }
                    if (g.moduleName.isNotBlank()) {
                        Text(
                            text = "模组: ${g.moduleName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Additional info
                    if (g.system.isNotBlank() || g.gameFormat.isNotBlank() || g.scale.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (g.system.isNotBlank()) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(g.system, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            if (g.gameFormat.isNotBlank()) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(g.gameFormat, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            if (g.scale.isNotBlank()) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(g.scale, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }

                    // Time info
                    if (g.startTime != null || g.expectedEndTime != null || g.defaultSessionTime.isNotBlank()) {
                        HorizontalDivider()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            g.startTime?.let { time ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("开团时间", style = MaterialTheme.typography.bodySmall)
                                    Text(dateFormat.format(java.util.Date(time)), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            g.expectedEndTime?.let { time ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("预计结束", style = MaterialTheme.typography.bodySmall)
                                    Text(dateFormat.format(java.util.Date(time)), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            if (g.defaultSessionTime.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("默认开团时间", style = MaterialTheme.typography.bodySmall)
                                    Text(g.defaultSessionTime, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }

                    // Status change buttons
                    HorizontalDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (g.status != Constants.GROUP_STATUS_ACTIVE) {
                            OutlinedButton(onClick = { onStatusChange(Constants.GROUP_STATUS_ACTIVE) }) {
                                Text("恢复进行")
                            }
                        }
                        if (g.status == Constants.GROUP_STATUS_ACTIVE) {
                            OutlinedButton(onClick = { onStatusChange(Constants.GROUP_STATUS_PAUSED) }) {
                                Text("暂停")
                            }
                        }
                        if (g.status != Constants.GROUP_STATUS_COMPLETED) {
                            OutlinedButton(onClick = { onStatusChange(Constants.GROUP_STATUS_COMPLETED) }) {
                                Text("完结")
                            }
                        }
                    }
                }
            }
        }

        // Stats summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                icon = Icons.Default.Event,
                label = "Session数",
                value = "${uiState.sessions.size}",
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                icon = Icons.Default.Person,
                label = "PC数",
                value = "${uiState.pcs.size}",
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                icon = Icons.Default.People,
                label = "NPC数",
                value = "${uiState.npcs.size}",
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                icon = Icons.Default.CheckCircle,
                label = "待办",
                value = "${uiState.pendingTodos.size}",
                modifier = Modifier.weight(1f)
            )
        }

        // Last session summary
        if (uiState.sessions.isNotEmpty()) {
            val lastSession = uiState.sessions.first()
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("上次Session", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Session ${lastSession.sessionNumber}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (lastSession.summary.isNotBlank()) {
                        Text(
                            text = lastSession.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    }
                }
            }
        }

        // Pending todos preview
        if (uiState.pendingTodos.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("待办事项", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    uiState.pendingTodos.take(3).forEach { todo ->
                        Text(
                            text = "· ${todo.title.ifBlank { todo.content }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (uiState.pendingTodos.size > 3) {
                        Text(
                            text = "...还有${uiState.pendingTodos.size - 3}项",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

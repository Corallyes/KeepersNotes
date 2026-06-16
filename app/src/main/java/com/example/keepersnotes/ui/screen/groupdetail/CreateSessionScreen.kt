package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.component.CompactTopBar
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionScreen(
    groupId: String,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdSessionId) {
        uiState.createdSessionId?.let { onCreated(it) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "添加记录",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Duration
            item {
                OutlinedTextField(
                    value = uiState.durationMinutes,
                    onValueChange = viewModel::updateDurationMinutes,
                    label = { Text("时长（分钟）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // PC participant multi-selector
            item {
                Text("参与PC", style = MaterialTheme.typography.labelMedium)
            }
            if (uiState.pcs.isEmpty()) {
                item {
                    Text(
                        "该团暂无PC角色",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                itemsIndexed(uiState.pcs) { _, pc ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = pc.pcId in uiState.selectedPcIds,
                            onCheckedChange = { viewModel.togglePcSelection(pc.pcId) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(pc.characterName, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                pc.playerName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Summary
            item {
                OutlinedTextField(
                    value = uiState.summary,
                    onValueChange = viewModel::updateSummary,
                    label = { Text("本场摘要") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )
            }

            // Important events
            item {
                Text("重要事件", style = MaterialTheme.typography.labelMedium)
            }
            itemsIndexed(uiState.importantEvents) { index, event ->
                ListItem(
                    headlineContent = { Text(event) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.removeEvent(index) }) {
                            Icon(Icons.Default.Close, contentDescription = "删除", modifier = Modifier.size(18.dp))
                        }
                    }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.newEventText,
                        onValueChange = viewModel::updateNewEventText,
                        placeholder = { Text("添加重要事件...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = viewModel::addEvent) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                }
            }

            // Clues found
            item {
                Text("发现线索", style = MaterialTheme.typography.labelMedium)
            }
            itemsIndexed(uiState.cluesFound) { index, clue ->
                ListItem(
                    headlineContent = { Text(clue) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.removeClue(index) }) {
                            Icon(Icons.Default.Close, contentDescription = "删除", modifier = Modifier.size(18.dp))
                        }
                    }
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.newClueText,
                        onValueChange = viewModel::updateNewClueText,
                        placeholder = { Text("添加发现的线索...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = viewModel::addClue) {
                        Icon(Icons.Default.Add, contentDescription = "添加")
                    }
                }
            }

            // Next session notes
            item {
                OutlinedTextField(
                    value = uiState.nextSessionNotes,
                    onValueChange = viewModel::updateNextSessionNotes,
                    label = { Text("下局预告/准备事项") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            // Submit button
            item {
                Button(
                    onClick = viewModel::submit,
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("保存")
                    }
                }
            }
        }
    }
}

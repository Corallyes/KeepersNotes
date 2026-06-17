package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.RichTextEditor
import com.example.keepersnotes.util.Chapter
import com.example.keepersnotes.util.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMemoScreen(
    groupId: String,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateMemoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.createdMemoId) {
        uiState.createdMemoId?.let { onCreated(it) }
    }

    val types = listOf(
        Constants.MEMO_TYPE_TODO to "待办",
        Constants.MEMO_TYPE_REMINDER to "提醒",
        Constants.MEMO_TYPE_RULE to "规则笔记",
        Constants.MEMO_TYPE_PLOT to "剧情笔记",
        Constants.MEMO_TYPE_CLUE to "线索笔记",
        Constants.MEMO_TYPE_HIDDEN to "暗线笔记"
    )

    var showChapterSelector by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "添加备忘",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Type selector
            Text("类型", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.take(3).forEach { (value, label) ->
                    FilterChip(
                        selected = uiState.type == value,
                        onClick = { viewModel.updateType(value) },
                        label = { Text(label) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.drop(3).forEach { (value, label) ->
                    FilterChip(
                        selected = uiState.type == value,
                        onClick = { viewModel.updateType(value) },
                        label = { Text(label) }
                    )
                }
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::updateTitle,
                label = { Text("标题") },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // 富文本编辑器
            Text("内容", style = MaterialTheme.typography.labelMedium)
            RichTextEditor(
                value = uiState.content,
                onValueChange = viewModel::updateContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp),
                placeholder = "输入备忘内容，支持Markdown格式..."
            )

            // 章节选择（仅当团关联了模组时显示）
            if (uiState.selectedModuleId != null && uiState.chapters.isNotEmpty()) {
                Text("关联章节（可选）", style = MaterialTheme.typography.labelMedium)
                OutlinedCard(
                    onClick = { showChapterSelector = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Article,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.selectedChapterTitle.ifBlank { "选择关联的章节" },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.selectedChapterId != null) {
                            IconButton(
                                onClick = { viewModel.selectChapter("", "") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, "清除", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Priority selector
            Text("优先级", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "普通", 1 to "重要", 2 to "紧急").forEach { (value, label) ->
                    FilterChip(
                        selected = uiState.priority == value,
                        onClick = { viewModel.updatePriority(value) },
                        label = { Text(label) }
                    )
                }
            }

            // 提醒类型 - 定时通知选项
            if (uiState.type == Constants.MEMO_TYPE_REMINDER) {
                HorizontalDivider()
                Text("定时通知", style = MaterialTheme.typography.labelMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = uiState.isNotificationEnabled,
                        onCheckedChange = viewModel::updateNotificationEnabled
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开启定时提醒")
                }

                if (uiState.isNotificationEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // 日期选择
                    val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                    val dateText = uiState.notificationDate?.let { dateFormat.format(Date(it)) } ?: "选择日期"
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dateText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 时间选择
                    val timeText = String.format("%02d:%02d", uiState.notificationHour, uiState.notificationMinute)
                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Schedule, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(timeText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.submit(context) },
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

    // 章节选择对话框
    if (showChapterSelector) {
        ChapterSelectorDialog(
            chapters = uiState.chapters,
            selectedChapterId = uiState.selectedChapterId,
            onChapterSelected = { chapter ->
                viewModel.selectChapter(chapter.id, chapter.title)
                showChapterSelector = false
            },
            onDismiss = { showChapterSelector = false }
        )
    }

    // 日期选择对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.notificationDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateNotificationDate(it) }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 时间选择对话框
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.notificationHour,
            initialMinute = uiState.notificationMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateNotificationTime(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun ChapterSelectorDialog(
    chapters: List<Chapter>,
    selectedChapterId: String?,
    onChapterSelected: (Chapter) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择章节") },
        text = {
            LazyColumn {
                chapters.forEach { chapter ->
                    addChapterItems(
                        chapter = chapter,
                        depth = 0,
                        selectedChapterId = selectedChapterId,
                        onChapterClick = onChapterSelected
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun androidx.compose.foundation.lazy.LazyListScope.addChapterItems(
    chapter: Chapter,
    depth: Int,
    selectedChapterId: String?,
    onChapterClick: (Chapter) -> Unit
) {
    item {
        ListItem(
            headlineContent = {
                Text(
                    chapter.title,
                    style = if (depth == 0) MaterialTheme.typography.bodyMedium
                    else MaterialTheme.typography.bodySmall
                )
            },
            leadingContent = {
                if (chapter.children.isEmpty()) {
                    Icon(Icons.Default.Article, null, modifier = Modifier.size(16.dp))
                } else {
                    Icon(Icons.Default.Folder, null, modifier = Modifier.size(16.dp))
                }
            },
            modifier = Modifier
                .padding(start = (depth * 16).dp)
                .clickable { onChapterClick(chapter) }
        )
    }
    chapter.children.forEach { child ->
        addChapterItems(child, depth + 1, selectedChapterId, onChapterClick)
    }
}

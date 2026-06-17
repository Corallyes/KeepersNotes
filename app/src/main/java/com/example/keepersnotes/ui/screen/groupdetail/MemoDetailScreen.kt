package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.layout.*
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
import com.example.keepersnotes.data.local.entity.KpMemoEntity
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.MemoTypeBadge
import com.example.keepersnotes.util.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDetailScreen(
    memoId: String,
    onBack: () -> Unit,
    viewModel: MemoDetailViewModel = hiltViewModel()
) {
    val memo by viewModel.memo.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEditSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = memo?.title ?: "备忘详情",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { padding ->
        val currentMemo = memo
        if (currentMemo == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("加载中...", modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MemoTypeBadge(type = currentMemo.type)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentMemo.title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (currentMemo.isHidden) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "暗线笔记",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (currentMemo.priority > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (currentMemo.priority) {
                                1 -> "重要"
                                2 -> "紧急"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (currentMemo.priority == 2) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Content
            if (currentMemo.content.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("内容", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentMemo.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 关联的模组和章节
            if (currentMemo.moduleId != null || currentMemo.chapterTitle.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("关联内容", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (currentMemo.chapterTitle.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    currentMemo.chapterTitle,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Tags
            if (currentMemo.tags.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("标签", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentMemo.tags, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Status for todo type
            if (currentMemo.type == Constants.MEMO_TYPE_TODO) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = currentMemo.isCompleted,
                            onCheckedChange = { viewModel.toggleCompleted() }
                        )
                        Text(
                            if (currentMemo.isCompleted) "已完成" else "未完成",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 通知信息（提醒类型）
            if (currentMemo.type == Constants.MEMO_TYPE_REMINDER && currentMemo.isNotificationEnabled && currentMemo.notificationTime != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("定时提醒", style = MaterialTheme.typography.titleSmall)
                            val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault())
                            Text(
                                dateFormat.format(Date(currentMemo.notificationTime!!)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit sheet
    val editMemo = memo
    if (showEditSheet && editMemo != null) {
        EditMemoSheet(
            memo = editMemo,
            onDismiss = { showEditSheet = false },
            onSave = { updatedMemo ->
                viewModel.updateMemo(updatedMemo, context)
                showEditSheet = false
            }
        )
    }

    // Delete confirmation dialog
    val deleteMemo = memo
    if (showDeleteDialog && deleteMemo != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除备忘") },
            text = { Text("确定要删除「${deleteMemo.title}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMemo(context) {
                            showDeleteDialog = false
                            onBack()
                        }
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMemoSheet(
    memo: KpMemoEntity,
    onDismiss: () -> Unit,
    onSave: (KpMemoEntity) -> Unit
) {
    var type by remember { mutableStateOf(memo.type) }
    var title by remember { mutableStateOf(memo.title) }
    var content by remember { mutableStateOf(memo.content) }
    var isHidden by remember { mutableStateOf(memo.isHidden) }
    var priority by remember { mutableStateOf(memo.priority) }
    var tags by remember { mutableStateOf(memo.tags) }
    var isNotificationEnabled by remember { mutableStateOf(memo.isNotificationEnabled) }
    var notificationDate by remember { mutableStateOf(memo.notificationTime?.let { time ->
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = time }
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }) }
    var notificationHour by remember { mutableIntStateOf(memo.notificationTime?.let {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = it }
        cal.get(java.util.Calendar.HOUR_OF_DAY)
    } ?: 9) }
    var notificationMinute by remember { mutableIntStateOf(memo.notificationTime?.let {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = it }
        cal.get(java.util.Calendar.MINUTE)
    } ?: 0) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val types = listOf(
        Constants.MEMO_TYPE_TODO to "待办",
        Constants.MEMO_TYPE_REMINDER to "提醒",
        Constants.MEMO_TYPE_RULE to "规则笔记",
        Constants.MEMO_TYPE_PLOT to "剧情笔记",
        Constants.MEMO_TYPE_CLUE to "线索笔记",
        Constants.MEMO_TYPE_HIDDEN to "暗线笔记"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text("编辑备忘", style = MaterialTheme.typography.titleLarge)
            }
            item {
                Text("类型", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.take(3).forEach { (value, label) ->
                        FilterChip(
                            selected = type == value,
                            onClick = { type = value },
                            label = { Text(label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.drop(3).forEach { (value, label) ->
                        FilterChip(
                            selected = type == value,
                            onClick = { type = value },
                            label = { Text(label) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("内容") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isHidden,
                        onCheckedChange = { isHidden = it }
                    )
                    Text("暗线笔记（仅KP可见）")
                }
            }
            item {
                Text("优先级", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "普通", 1 to "重要", 2 to "紧急").forEach { (value, label) ->
                        FilterChip(
                            selected = priority == value,
                            onClick = { priority = value },
                            label = { Text(label) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("标签（逗号分隔）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            // 提醒类型 - 定时通知选项
            if (type == Constants.MEMO_TYPE_REMINDER) {
                item {
                    HorizontalDivider()
                    Text("定时通知", style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开启定时提醒")
                    }
                }
                if (isNotificationEnabled) {
                    item {
                        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
                        val dateText = notificationDate?.let { dateFormat.format(Date(it)) } ?: "选择日期"
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
                    }
                    item {
                        val timeText = String.format("%02d:%02d", notificationHour, notificationMinute)
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
            }
            item {
                Button(
                    onClick = {
                        val notificationTime: Long? = if (type == Constants.MEMO_TYPE_REMINDER && isNotificationEnabled && notificationDate != null) {
                            notificationDate!! + notificationHour * 3600_000L + notificationMinute * 60_000L
                        } else null
                        val notificationId = if (type == Constants.MEMO_TYPE_REMINDER && isNotificationEnabled) {
                            memo.notificationId.takeIf { it != 0 } ?: (memo.memoId.hashCode() and 0x7FFFFFFF)
                        } else memo.notificationId
                        onSave(
                            memo.copy(
                                type = type,
                                title = title.trim(),
                                content = content.trim(),
                                isHidden = type == Constants.MEMO_TYPE_HIDDEN || isHidden,
                                priority = priority,
                                tags = tags.trim(),
                                isNotificationEnabled = type == Constants.MEMO_TYPE_REMINDER && isNotificationEnabled,
                                notificationTime = notificationTime,
                                notificationId = notificationId,
                                updateTime = System.currentTimeMillis()
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("保存")
                }
            }
        }
    }

    // 日期选择对话框
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = notificationDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { notificationDate = it }
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
            initialHour = notificationHour,
            initialMinute = notificationMinute,
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
                    notificationHour = timePickerState.hour
                    notificationMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            }
        )
    }
}

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
import com.example.keepersnotes.util.LocalizedStrings
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
                title = memo?.title ?: LocalizedStrings.memoDetail,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, contentDescription = LocalizedStrings.edit)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = LocalizedStrings.delete)
                    }
                }
            )
        }
    ) { padding ->
        val currentMemo = memo
        if (currentMemo == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text(LocalizedStrings.groupLoading, modifier = Modifier.padding(16.dp))
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
                    if (currentMemo.priority > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (currentMemo.priority) {
                                1 -> LocalizedStrings.memoPriorityImportant
                                2 -> LocalizedStrings.memoPriorityUrgent
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
                        Text(LocalizedStrings.memoContent, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentMemo.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 关联的模组和章节
            if (currentMemo.chapterTitle.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(LocalizedStrings.memoLinkedContent, style = MaterialTheme.typography.titleSmall)
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
                        Text(LocalizedStrings.memoTags, style = MaterialTheme.typography.titleSmall)
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
                            if (currentMemo.isCompleted) LocalizedStrings.memoCompleted else LocalizedStrings.memoNotCompleted,
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
                            Text(LocalizedStrings.memoNotification, style = MaterialTheme.typography.titleSmall)
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
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
            title = { Text(LocalizedStrings.memoDeleteTitle) },
            text = { Text(LocalizedStrings.memoDeleteConfirm(deleteMemo.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMemo(context) {
                            showDeleteDialog = false
                            onBack()
                        }
                    }
                ) {
                    Text(LocalizedStrings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(LocalizedStrings.cancel)
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
        Constants.MEMO_TYPE_TODO to LocalizedStrings.memoType(Constants.MEMO_TYPE_TODO),
        Constants.MEMO_TYPE_REMINDER to LocalizedStrings.memoType(Constants.MEMO_TYPE_REMINDER),
        Constants.MEMO_TYPE_RULE to LocalizedStrings.memoType(Constants.MEMO_TYPE_RULE),
        Constants.MEMO_TYPE_PLOT to LocalizedStrings.memoType(Constants.MEMO_TYPE_PLOT),
        Constants.MEMO_TYPE_CLUE to LocalizedStrings.memoType(Constants.MEMO_TYPE_CLUE)
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
                Text(LocalizedStrings.memoEdit, style = MaterialTheme.typography.titleLarge)
            }
            item {
                Text(LocalizedStrings.memoTypeLabel, style = MaterialTheme.typography.labelMedium)
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
                    label = { Text(LocalizedStrings.memoTitle) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(LocalizedStrings.memoContent) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )
            }
            item {
                Text(LocalizedStrings.memoPriority, style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to LocalizedStrings.memoPriorityNormal, 1 to LocalizedStrings.memoPriorityImportant, 2 to LocalizedStrings.memoPriorityUrgent).forEach { (value, label) ->
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
                    label = { Text(LocalizedStrings.memoTagsPlaceholder) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            // 提醒类型 - 定时通知选项
            if (type == Constants.MEMO_TYPE_REMINDER) {
                item {
                    HorizontalDivider()
                    Text(LocalizedStrings.memoNotification, style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isNotificationEnabled = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(LocalizedStrings.memoNotificationEnable)
                    }
                }
                if (isNotificationEnabled) {
                    item {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val dateText = notificationDate?.let { dateFormat.format(Date(it)) } ?: LocalizedStrings.memoSelectDate
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
                    Text(LocalizedStrings.save)
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
                }) { Text(LocalizedStrings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(LocalizedStrings.cancel) }
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
            title = { Text(LocalizedStrings.groupSelectTime) },
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
                }) { Text(LocalizedStrings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(LocalizedStrings.cancel) }
            }
        )
    }
}

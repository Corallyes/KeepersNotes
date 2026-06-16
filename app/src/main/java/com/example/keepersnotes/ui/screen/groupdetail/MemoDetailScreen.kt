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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.KpMemoEntity
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.MemoTypeBadge
import com.example.keepersnotes.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoDetailScreen(
    memoId: String,
    onBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val memo = uiState.memos.find { it.memoId == memoId }
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
        if (memo == null) {
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
                        MemoTypeBadge(type = memo.type)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = memo.title,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (memo.isHidden) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "暗线笔记",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (memo.priority > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (memo.priority) {
                                1 -> "重要"
                                2 -> "紧急"
                                else -> ""
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (memo.priority == 2) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Content
            if (memo.content.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("内容", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(memo.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // 关联的模组和章节
            if (memo.moduleId != null || memo.chapterTitle.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("关联内容", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (memo.chapterTitle.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    memo.chapterTitle,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Tags
            if (memo.tags.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("标签", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(memo.tags, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Status for todo type
            if (memo.type == Constants.MEMO_TYPE_TODO) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = memo.isCompleted,
                            onCheckedChange = { viewModel.toggleMemoCompleted(memo.memoId) }
                        )
                        Text(
                            if (memo.isCompleted) "已完成" else "未完成",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // Edit sheet
    if (showEditSheet && memo != null) {
        EditMemoSheet(
            memo = memo,
            onDismiss = { showEditSheet = false },
            onSave = { updatedMemo ->
                viewModel.updateMemo(updatedMemo)
                showEditSheet = false
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && memo != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除备忘") },
            text = { Text("确定要删除「${memo.title}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMemo(memo.memoId)
                        showDeleteDialog = false
                        onBack()
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val types = listOf(
        Constants.MEMO_TYPE_GENERAL to "备忘",
        Constants.MEMO_TYPE_TODO to "待办",
        Constants.MEMO_TYPE_CLUE to "线索",
        Constants.MEMO_TYPE_PLOT to "剧情",
        Constants.MEMO_TYPE_REMINDER to "提醒",
        Constants.MEMO_TYPE_RULE to "规则"
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
            item {
                Button(
                    onClick = {
                        onSave(
                            memo.copy(
                                type = type,
                                title = title.trim(),
                                content = content.trim(),
                                isHidden = isHidden,
                                priority = priority,
                                tags = tags.trim(),
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
}

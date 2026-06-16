package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.clickable
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
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.RichTextEditor
import com.example.keepersnotes.util.Chapter
import com.example.keepersnotes.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMemoScreen(
    groupId: String,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateMemoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdMemoId) {
        uiState.createdMemoId?.let { onCreated(it) }
    }

    val types = listOf(
        Constants.MEMO_TYPE_GENERAL to "备忘",
        Constants.MEMO_TYPE_TODO to "待办",
        Constants.MEMO_TYPE_CLUE to "线索",
        Constants.MEMO_TYPE_PLOT to "剧情",
        Constants.MEMO_TYPE_REMINDER to "提醒",
        Constants.MEMO_TYPE_RULE to "规则"
    )

    var showModuleSelector by remember { mutableStateOf(false) }
    var showChapterSelector by remember { mutableStateOf(false) }

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

            // 模组关联
            Text("关联模组（可选）", style = MaterialTheme.typography.labelMedium)
            OutlinedCard(
                onClick = { showModuleSelector = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.modules.find { it.moduleId == uiState.selectedModuleId }?.title
                            ?: "选择关联的模组",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (uiState.selectedModuleId != null) {
                        IconButton(
                            onClick = { viewModel.selectModule(null) },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, "清除", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // 章节选择（仅当选择了模组时显示）
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

            // Hidden toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.isHidden,
                    onCheckedChange = viewModel::updateIsHidden
                )
                Text("暗线笔记（仅KP可见）")
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

            Spacer(modifier = Modifier.weight(1f))

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

    // 模组选择对话框
    if (showModuleSelector) {
        AlertDialog(
            onDismissRequest = { showModuleSelector = false },
            title = { Text("选择模组") },
            text = {
                if (uiState.modules.isEmpty()) {
                    Text("暂无可用模组")
                } else {
                    Column {
                        uiState.modules.forEach { module ->
                            ListItem(
                                headlineContent = { Text(module.title) },
                                supportingContent = {
                                    if (module.author.isNotBlank()) {
                                        Text(module.author)
                                    }
                                },
                                leadingContent = {
                                    Icon(Icons.Default.Book, null)
                                },
                                modifier = Modifier.clickable {
                                    viewModel.selectModule(module.moduleId)
                                    showModuleSelector = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModuleSelector = false }) {
                    Text("取消")
                }
            }
        )
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

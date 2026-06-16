package com.example.keepersnotes.ui.screen.modulelibrary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.AnnotationEntity
import com.example.keepersnotes.data.local.entity.HighlightEntity
import com.example.keepersnotes.ui.component.*
import com.example.keepersnotes.ui.component.EditModuleDialog
import com.example.keepersnotes.util.Chapter
import com.example.keepersnotes.util.ModuleContentParser
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleReaderScreen(
    moduleId: String,
    onBack: () -> Unit,
    viewModel: ModuleReaderViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var expandedChapterIds by remember { mutableStateOf(setOf<String>()) }
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // 批注弹窗状态
    var showAnnotationDialog by remember { mutableStateOf(false) }
    var selectedTextForAnnotation by remember { mutableStateOf("") }
    var selectionStart by remember { mutableIntStateOf(0) }
    var selectionEnd by remember { mutableIntStateOf(0) }

    // 查看批注弹窗
    var showAnnotationViewDialog by remember { mutableStateOf(false) }
    var selectedAnnotation by remember { mutableStateOf<AnnotationEntity?>(null) }

    // 清除确认弹窗
    var showClearDialog by remember { mutableStateOf(false) }

    // 批注管理面板
    var showAnnotationPanel by remember { mutableStateOf(false) }

    // 书签面板
    var showBookmarkPanel by remember { mutableStateOf(false) }

    // 书签添加弹窗
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkNote by remember { mutableStateOf("") }

    // 编辑/删除模组弹窗
    var showEditModuleDialog by remember { mutableStateOf(false) }
    var showDeleteModuleDialog by remember { mutableStateOf(false) }

    // 编辑章节内容弹窗
    var showEditChapterDialog by remember { mutableStateOf(false) }
    var editChapterContent by remember { mutableStateOf("") }

    // Expand all chapters by default
    LaunchedEffect(uiState.chapters) {
        expandedChapterIds = collectAllIds(uiState.chapters).toSet()
    }

    // Search results
    val searchResults = remember(searchQuery, uiState.chapters) {
        if (searchQuery.isBlank()) emptyList()
        else ModuleContentParser.searchChapters(uiState.chapters, searchQuery)
    }

    // Use modal navigation drawer for chapter tree on mobile
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Text(
                    "目录",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    uiState.chapters.forEach { chapter ->
                        addChapterDrawerItems(
                            chapter = chapter,
                            depth = 0,
                            selectedChapter = uiState.selectedChapter,
                            onChapterClick = { viewModel.selectChapter(it) },
                            expandedIds = expandedChapterIds,
                            onToggleExpand = { id ->
                                expandedChapterIds = if (id in expandedChapterIds) {
                                    expandedChapterIds - id
                                } else {
                                    expandedChapterIds + id
                                }
                            }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CompactTopBar(
                    title = uiState.module?.title ?: "模组阅读",
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    actions = {
                        // Search toggle
                        IconButton(onClick = { showSearch = !showSearch; searchQuery = "" }) {
                            Icon(
                                if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (showSearch) "关闭搜索" else "搜索"
                            )
                        }
                        // Chapter tree toggle
                        IconButton(onClick = {
                            coroutineScope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "目录")
                        }
                        // Favorite
                        IconButton(onClick = viewModel::toggleFavorite) {
                            Icon(
                                if (uiState.module?.isFavorite == true) Icons.Default.Favorite
                                else Icons.Default.FavoriteBorder,
                                contentDescription = if (uiState.module?.isFavorite == true) "取消收藏" else "收藏",
                                tint = if (uiState.module?.isFavorite == true) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // More options
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("编辑模组信息") },
                                onClick = {
                                    showMenu = false
                                    showEditModuleDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("删除模组", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    showDeleteModuleDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("编辑本节内容") },
                                onClick = {
                                    showMenu = false
                                    editChapterContent = uiState.selectedChapter?.content ?: ""
                                    showEditChapterDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("分享") },
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("清除本章高亮") },
                                onClick = {
                                    showMenu = false
                                    uiState.selectedChapter?.let {
                                        viewModel.clearHighlightsByChapter(it.id)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Highlight, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("清除本章批注") },
                                onClick = {
                                    showMenu = false
                                    uiState.selectedChapter?.let {
                                        viewModel.clearAnnotationsByChapter(it.id)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Comment, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("清除全部标注") },
                                onClick = {
                                    showMenu = false
                                    showClearDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteSweep,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                )
            },
            bottomBar = {
                // 标注工具栏
                AnnotationToolbar(
                    activeTool = uiState.activeTool,
                    selectedColor = uiState.selectedColor,
                    eraserMode = uiState.eraserMode,
                    onToolSelected = { tool ->
                        if (tool == AnnotationTool.BOOKMARK) {
                            // 书签工具直接弹出添加对话框
                            showAddBookmarkDialog = true
                        } else {
                            viewModel.setActiveTool(tool)
                        }
                    },
                    onColorSelected = viewModel::setSelectedColor,
                    onEraserModeChanged = viewModel::setEraserMode,
                    onClearAll = { showClearDialog = true }
                )
            }
        ) { padding ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@Scaffold
            }

            if (uiState.chapters.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "模组内容为空",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Scaffold
            }

            val selectedChapter = uiState.selectedChapter

            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Search bar
                if (showSearch) {
                    Surface(
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("搜索模组内容...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = "清除")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            // Search results
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                if (searchResults.isEmpty()) {
                                    Text(
                                        "未找到匹配内容",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        "找到 ${searchResults.size} 个章节",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        items(searchResults.size) { index ->
                                            val chapter = searchResults[index]
                                            ListItem(
                                                headlineContent = {
                                                    Text(
                                                        chapter.title,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                },
                                                supportingContent = {
                                                    Text(
                                                        chapter.content.take(80) + if (chapter.content.length > 80) "..." else "",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        maxLines = 1
                                                    )
                                                },
                                                leadingContent = {
                                                    Icon(
                                                        Icons.Default.Search,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                },
                                                modifier = Modifier.clickable {
                                                    viewModel.selectChapter(chapter)
                                                    showSearch = false
                                                    searchQuery = ""
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Chapter breadcrumb / navigation bar
                if (selectedChapter != null) {
                    Surface(
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous/Next navigation
                            val prevChapter = findAdjacentChapter(uiState.chapters, selectedChapter, -1)
                            val nextChapter = findAdjacentChapter(uiState.chapters, selectedChapter, 1)

                            IconButton(
                                onClick = { prevChapter?.let { viewModel.selectChapter(it) } },
                                enabled = prevChapter != null,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "上一节", modifier = Modifier.size(20.dp))
                            }
                            Text(
                                text = selectedChapter.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { nextChapter?.let { viewModel.selectChapter(it) } },
                                enabled = nextChapter != null,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "下一节", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // 标注统计
                val chapterHighlights = uiState.highlights.filter { it.chapterId == selectedChapter?.id }
                val chapterAnnotations = uiState.annotations.filter { it.chapterId == selectedChapter?.id }
                val chapterBookmarks = uiState.bookmarks.filter { it.chapterId == selectedChapter?.id }
                if (chapterHighlights.isNotEmpty() || chapterAnnotations.isNotEmpty() || chapterBookmarks.isNotEmpty() || uiState.bookmarks.isNotEmpty()) {
                    Surface(
                        tonalElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (chapterHighlights.isNotEmpty()) {
                                Text(
                                    "${chapterHighlights.size} 高亮",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (chapterAnnotations.isNotEmpty()) {
                                Text(
                                    "${chapterAnnotations.size} 批注",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            if (uiState.bookmarks.isNotEmpty()) {
                                Text(
                                    "${uiState.bookmarks.size} 书签",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))

                            // 阅读进度
                            uiState.readingProgress?.let { progress ->
                                if (progress.totalReadTimeMinutes > 0) {
                                    Text(
                                        "${progress.totalReadTimeMinutes}分钟",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // 书签管理按钮
                            IconButton(
                                onClick = { showBookmarkPanel = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Bookmarks,
                                    contentDescription = "管理书签",
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // 标注管理按钮
                            IconButton(
                                onClick = { showAnnotationPanel = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.List,
                                    contentDescription = "管理标注",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Content area
                if (selectedChapter != null) {
                    if (selectedChapter.content.isNotBlank()) {
                        // 显示带标注的内容
                        AnnotatedContent(
                            content = selectedChapter.content,
                            highlights = chapterHighlights,
                            annotations = chapterAnnotations,
                            activeTool = uiState.activeTool,
                            onAnnotationClick = { annotation ->
                                if (uiState.activeTool == AnnotationTool.ERASER) {
                                    viewModel.eraseByAnnotation(annotation)
                                } else {
                                    selectedAnnotation = annotation
                                    showAnnotationViewDialog = true
                                }
                            },
                            onHighlightClick = { highlight ->
                                if (uiState.activeTool == AnnotationTool.ERASER) {
                                    viewModel.eraseByHighlight(highlight)
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "本节无内容",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "从目录中选择一个章节开始阅读",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // 批注输入弹窗
    if (showAnnotationDialog) {
        AnnotationDialog(
            selectedText = selectedTextForAnnotation,
            onDismiss = { showAnnotationDialog = false },
            onSave = { note ->
                uiState.selectedChapter?.let { chapter ->
                    viewModel.addAnnotation(
                        chapterId = chapter.id,
                        startIndex = selectionStart,
                        endIndex = selectionEnd,
                        selectedText = selectedTextForAnnotation,
                        note = note
                    )
                }
                showAnnotationDialog = false
            }
        )
    }

    // 查看批注弹窗
    if (showAnnotationViewDialog && selectedAnnotation != null) {
        AnnotationViewDialog(
            selectedText = selectedAnnotation!!.selectedText,
            note = selectedAnnotation!!.note,
            onDismiss = { showAnnotationViewDialog = false },
            onEdit = {
                showAnnotationViewDialog = false
                // TODO: 实现编辑功能
            },
            onDelete = {
                viewModel.deleteAnnotation(selectedAnnotation!!)
                showAnnotationViewDialog = false
            }
        )
    }

    // 清除确认弹窗
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清除标注") },
            text = { Text("确定要清除所有高亮和批注吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        showClearDialog = false
                    }
                ) {
                    Text("清除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 批注管理面板
    if (showAnnotationPanel) {
        AnnotationPanel(
            highlights = uiState.highlights,
            annotations = uiState.annotations,
            onHighlightClick = { highlight ->
                viewModel.selectChapter(
                    uiState.chapters.flatMap { collectLeaves(listOf(it)) }
                        .find { it.id == highlight.chapterId } ?: return@AnnotationPanel
                )
                showAnnotationPanel = false
            },
            onAnnotationClick = { annotation ->
                selectedAnnotation = annotation
                showAnnotationViewDialog = true
                showAnnotationPanel = false
            },
            onDeleteHighlight = viewModel::deleteHighlight,
            onDeleteAnnotation = viewModel::deleteAnnotation,
            onDismiss = { showAnnotationPanel = false }
        )
    }

    // 书签面板
    if (showBookmarkPanel) {
        BookmarkPanel(
            bookmarks = uiState.bookmarks,
            onBookmarkClick = { bookmark ->
                viewModel.navigateToBookmark(bookmark)
                showBookmarkPanel = false
            },
            onDeleteBookmark = viewModel::deleteBookmark,
            onDismiss = { showBookmarkPanel = false }
        )
    }

    // 添加书签弹窗
    if (showAddBookmarkDialog) {
        AlertDialog(
            onDismissRequest = { showAddBookmarkDialog = false },
            title = { Text("添加书签") },
            text = {
                Column {
                    Text(
                        "章节: ${uiState.selectedChapter?.title ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bookmarkNote,
                        onValueChange = { bookmarkNote = it },
                        label = { Text("备注（可选）") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        uiState.selectedChapter?.let { chapter ->
                            viewModel.addBookmark(
                                chapterId = chapter.id,
                                note = bookmarkNote.trim()
                            )
                        }
                        bookmarkNote = ""
                        showAddBookmarkDialog = false
                    }
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookmarkDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 编辑模组信息弹窗
    if (showEditModuleDialog && uiState.module != null) {
        EditModuleDialog(
            module = uiState.module!!,
            onDismiss = { showEditModuleDialog = false },
            onSave = { updatedModule ->
                viewModel.updateModule(updatedModule)
                showEditModuleDialog = false
            }
        )
    }

    // 删除模组确认弹窗
    if (showDeleteModuleDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteModuleDialog = false },
            title = { Text("删除模组") },
            text = { Text("确定要删除「${uiState.module?.title ?: ""}」吗？此操作将同时删除该模组的所有高亮、批注和书签，且不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteModule()
                        showDeleteModuleDialog = false
                        onBack()
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteModuleDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 编辑章节内容弹窗
    if (showEditChapterDialog) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showEditChapterDialog = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    "编辑: ${uiState.selectedChapter?.title ?: ""}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                RichTextEditor(
                    value = editChapterContent,
                    onValueChange = { editChapterContent = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 300.dp, max = 500.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showEditChapterDialog = false }) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            uiState.selectedChapter?.let { chapter ->
                                viewModel.updateChapterContent(chapter.id, editChapterContent)
                            }
                            showEditChapterDialog = false
                        }
                    ) {
                        Text("保存")
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AnnotatedContent(
    content: String,
    highlights: List<HighlightEntity>,
    annotations: List<AnnotationEntity>,
    activeTool: AnnotationTool,
    onAnnotationClick: (AnnotationEntity) -> Unit,
    onHighlightClick: (HighlightEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // 构建带标注的文本
    val annotatedString = buildAnnotatedString {
        append(content)

        // 应用高亮
        highlights.forEach { highlight ->
            val start = highlight.startIndex.coerceIn(0, content.length)
            val end = highlight.endIndex.coerceIn(0, content.length)
            if (start < end) {
                addStyle(
                    style = SpanStyle(
                        background = Color(highlight.color).copy(alpha = 0.3f)
                    ),
                    start = start,
                    end = end
                )
                addStringAnnotation(
                    tag = "highlight",
                    annotation = highlight.highlightId,
                    start = start,
                    end = end
                )
            }
        }

        // 应用批注标记
        annotations.forEach { annotation ->
            val start = annotation.startIndex.coerceIn(0, content.length)
            val end = annotation.endIndex.coerceIn(0, content.length)
            if (start < end) {
                addStringAnnotation(
                    tag = "annotation",
                    annotation = annotation.annotationId,
                    start = start,
                    end = end
                )
                addStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = Color(annotation.color)
                    ),
                    start = start,
                    end = end
                )
            }
        }
    }

    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.8
    )

    // 使用基础Text组件显示带标注的文本
    // 由于Compose的ClickableText在新版本中已更改，使用简化实现
    Text(
        text = annotatedString,
        style = textStyle,
        modifier = modifier
    )
}

private fun LazyListScope.addChapterDrawerItems(
    chapter: Chapter,
    depth: Int,
    selectedChapter: Chapter?,
    onChapterClick: (Chapter) -> Unit,
    expandedIds: Set<String>,
    onToggleExpand: (String) -> Unit
) {
    item(key = chapter.id) {
        val hasChildren = chapter.children.isNotEmpty()
        val isExpanded = chapter.id in expandedIds
        NavigationDrawerItem(
            label = {
                Text(
                    chapter.title,
                    style = if (depth == 0) MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ) else MaterialTheme.typography.bodySmall
                )
            },
            selected = selectedChapter?.id == chapter.id,
            onClick = {
                if (hasChildren) {
                    onToggleExpand(chapter.id)
                } else {
                    onChapterClick(chapter)
                }
            },
            modifier = Modifier.padding(start = (depth * 12).dp),
            badge = if (hasChildren) {
                {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else null
        )
    }
    val isExpanded = chapter.id in expandedIds
    if (chapter.children.isNotEmpty() && isExpanded) {
        chapter.children.forEach { child ->
            addChapterDrawerItems(
                chapter = child,
                depth = depth + 1,
                selectedChapter = selectedChapter,
                onChapterClick = onChapterClick,
                expandedIds = expandedIds,
                onToggleExpand = onToggleExpand
            )
        }
    }
}

/**
 * Find the previous (-1) or next (+1) leaf chapter relative to the current one.
 */
private fun findAdjacentChapter(chapters: List<Chapter>, current: Chapter, direction: Int): Chapter? {
    val leaves = collectLeaves(chapters)
    val currentIndex = leaves.indexOfFirst { it.id == current.id }
    if (currentIndex < 0) return null
    val targetIndex = currentIndex + direction
    return if (targetIndex in leaves.indices) leaves[targetIndex] else null
}

private fun collectLeaves(chapters: List<Chapter>): List<Chapter> {
    val result = mutableListOf<Chapter>()
    for (chapter in chapters) {
        if (chapter.children.isEmpty()) {
            result.add(chapter)
        } else {
            result.addAll(collectLeaves(chapter.children))
        }
    }
    return result
}

private fun collectAllIds(chapters: List<Chapter>): List<String> {
    val result = mutableListOf<String>()
    for (chapter in chapters) {
        if (chapter.children.isNotEmpty()) {
            result.add(chapter.id)
            result.addAll(collectAllIds(chapter.children))
        }
    }
    return result
}

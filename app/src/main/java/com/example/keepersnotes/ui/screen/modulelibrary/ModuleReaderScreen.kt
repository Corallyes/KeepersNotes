package com.example.keepersnotes.ui.screen.modulelibrary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.data.local.entity.AnnotationEntity
import com.example.keepersnotes.data.local.entity.DocumentNodeEntity
import com.example.keepersnotes.data.local.entity.HighlightEntity
import com.example.keepersnotes.ui.component.*
import com.example.keepersnotes.ui.component.EditModuleDialog
import com.example.keepersnotes.util.Chapter
import com.example.keepersnotes.util.LocalizedStrings
import com.example.keepersnotes.util.ModuleContentParser
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collect
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

    // 生命周期感知阅读计时
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> viewModel.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> viewModel.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
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

    // 纯享阅读模式
    var pureReadingMode by remember { mutableStateOf(false) }

    // 字体大小（双指缩放）
    var fontSize by remember { mutableFloatStateOf(16f) }
    var showFontSize by remember { mutableStateOf(false) }
    var fontSizeGeneration by remember { mutableIntStateOf(0) }
    // 数据库加载完成后恢复字体大小
    LaunchedEffect(uiState.readingProgress) {
        val saved = uiState.readingProgress?.lastFontSize
        if (saved != null && saved > 0f) {
            fontSize = saved
        }
    }
    LaunchedEffect(fontSizeGeneration) {
        if (fontSizeGeneration > 0) {
            showFontSize = true
            kotlinx.coroutines.delay(1500)
            showFontSize = false
        }
    }

    // 编辑章节内容弹窗
    var showEditChapterDialog by remember { mutableStateOf(false) }
    var editChapterContent by remember { mutableStateOf("") }

    // 结构化节点文本选择状态
    val activeNodeSelection by viewModel.activeNodeSelection.collectAsStateWithLifecycle()
    var showNodeAnnotationDialog by remember { mutableStateOf(false) }
    var nodeAnnotationSelection by remember { mutableStateOf<NodeSelection?>(null) }

    // Expand all chapters by default
    LaunchedEffect(uiState.chapters) {
        expandedChapterIds = collectAllIds(uiState.chapters).toSet()
    }

    // Search results
    val searchResults = remember(searchQuery, uiState.chapters) {
        if (searchQuery.isBlank()) emptyList()
        else ModuleContentParser.searchChapters(uiState.chapters, searchQuery)
    }

    // List state for structured nodes (hoisted so drawer can access it)
    val structuredListState = remember { androidx.compose.foundation.lazy.LazyListState() }

    // Scroll to initial chapter/node on first load
    val initialChapterId = viewModel.initialChapterId
    LaunchedEffect(uiState.documentNodes, initialChapterId) {
        if (uiState.useStructuredNodes && uiState.documentNodes.isNotEmpty()) {
            // 优先从 chapterId 跳转，否则恢复上次阅读位置
            val targetIndex = if (initialChapterId != null) {
                uiState.documentNodes.indexOfFirst { it.nodeId == initialChapterId }
            } else -1

            if (targetIndex >= 0) {
                structuredListState.animateScrollToItem(targetIndex)
            } else if (viewModel.initialScrollIndex > 0 || viewModel.initialScrollOffset > 0) {
                structuredListState.scrollToItem(
                    viewModel.initialScrollIndex.coerceIn(0, uiState.documentNodes.size - 1),
                    viewModel.initialScrollOffset
                )
            }
        }
    }

    // 持续跟踪滚动位置，供 ViewModel 保存（跳过恢复阶段的初始事件）
    var scrollRestored by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.documentNodes) {
        if (uiState.useStructuredNodes && uiState.documentNodes.isNotEmpty()) {
            // 等待恢复完成后再开始监听
            kotlinx.coroutines.delay(500)
            scrollRestored = true
        }
    }
    LaunchedEffect(structuredListState, scrollRestored) {
        if (!scrollRestored) return@LaunchedEffect
        snapshotFlow {
            Pair(structuredListState.firstVisibleItemIndex, structuredListState.firstVisibleItemScrollOffset)
        }.collect { (index, offset) ->
            val nodeId = uiState.documentNodes.getOrNull(index)?.nodeId ?: ""
            if (nodeId.isNotEmpty()) {
                viewModel.updateScrollPosition(nodeId, offset)
            }
        }
    }

    // Use modal navigation drawer for chapter tree on mobile
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
                Text(
                    LocalizedStrings.readerToc,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (uiState.useStructuredNodes) {
                        // Structured nodes mode: show heading nodes as TOC
                        val headings = uiState.documentNodes.filter { it.type == "heading" }
                        items(headings.size) { index ->
                            val heading = headings[index]
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        heading.content,
                                        style = when (heading.level) {
                                            1 -> MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                            2 -> MaterialTheme.typography.bodySmall
                                            else -> MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    )
                                },
                                selected = false,
                                onClick = {
                                    val nodeIndex = uiState.documentNodes.indexOfFirst { it.nodeId == heading.nodeId }
                                    coroutineScope.launch {
                                        drawerState.close()
                                        if (nodeIndex >= 0) structuredListState.animateScrollToItem(nodeIndex)
                                    }
                                },
                                modifier = Modifier.padding(start = ((heading.level - 1) * 12).dp)
                            )
                        }
                    } else {
                        // Old mode: chapter tree
                        uiState.chapters.forEach { chapter ->
                            addChapterDrawerItems(
                                chapter = chapter,
                                depth = 0,
                                selectedChapter = uiState.selectedChapter,
                                onChapterClick = {
                                    viewModel.selectChapter(it)
                                    coroutineScope.launch { drawerState.close() }
                                },
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
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                if (pureReadingMode) return@Scaffold
                CompactTopBar(
                    title = uiState.module?.title ?: LocalizedStrings.readerModuleReading,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                        }
                    },
                    actions = {
                        // Search toggle
                        IconButton(onClick = { showSearch = !showSearch; searchQuery = "" }) {
                            Icon(
                                if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (showSearch) LocalizedStrings.readerCloseSearch else LocalizedStrings.readerSearch
                            )
                        }
                        // Chapter tree toggle
                        IconButton(onClick = {
                            coroutineScope.launch {
                                if (drawerState.isOpen) drawerState.close() else drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = LocalizedStrings.readerToc)
                        }
                        // Favorite
                        IconButton(onClick = viewModel::toggleFavorite) {
                            Icon(
                                if (uiState.module?.isFavorite == true) Icons.Default.Favorite
                                else Icons.Default.FavoriteBorder,
                                contentDescription = if (uiState.module?.isFavorite == true) LocalizedStrings.readerUnfavorite else LocalizedStrings.readerFavorite,
                                tint = if (uiState.module?.isFavorite == true) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // More options
                        var showMenu by remember { mutableStateOf(false) }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = LocalizedStrings.readerMore)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(LocalizedStrings.readerEditModuleInfo) },
                                onClick = {
                                    showMenu = false
                                    showEditModuleDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(LocalizedStrings.readerDeleteModule, color = MaterialTheme.colorScheme.error) },
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
                                text = { Text(LocalizedStrings.readerEditSectionContent) },
                                onClick = {
                                    showMenu = false
                                    editChapterContent = uiState.selectedChapter?.content ?: ""
                                    showEditChapterDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(LocalizedStrings.readerShare) },
                                onClick = { showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(LocalizedStrings.readerClearChapterHighlights) },
                                onClick = {
                                    showMenu = false
                                    uiState.selectedChapter?.let {
                                        viewModel.clearHighlightsByChapter(it.id)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Highlight, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(LocalizedStrings.readerClearChapterAnnotations) },
                                onClick = {
                                    showMenu = false
                                    uiState.selectedChapter?.let {
                                        viewModel.clearAnnotationsByChapter(it.id)
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Comment, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text(LocalizedStrings.readerClearAllAnnotations) },
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
                if (pureReadingMode || activeNodeSelection != null) return@Scaffold
                AnnotationToolbar(
                    pureReadingMode = pureReadingMode,
                    onTogglePureReadingMode = { pureReadingMode = !pureReadingMode },
                    onShowAnnotationPanel = { showAnnotationPanel = true },
                    onShowBookmarkPanel = { showBookmarkPanel = true }
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

            if (!uiState.useStructuredNodes && uiState.chapters.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        LocalizedStrings.readerContentEmpty,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Scaffold
            }

            val selectedChapter = uiState.selectedChapter

            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search bar
                if (showSearch && !pureReadingMode) {
                    Surface(
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text(LocalizedStrings.readerSearchPlaceholder) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = ""; showSearch = false }) {
                                            Icon(Icons.Default.Clear, contentDescription = LocalizedStrings.clear)
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
                                        LocalizedStrings.readerNoMatch,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        LocalizedStrings.readerMatchCount(searchResults.size),
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

                // Chapter breadcrumb / navigation bar (only for old chapter mode)
                if (!uiState.useStructuredNodes && selectedChapter != null && !pureReadingMode && !showSearch) {
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
                                Icon(Icons.Default.ChevronLeft, contentDescription = LocalizedStrings.readerPrevSection, modifier = Modifier.size(20.dp))
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
                                Icon(Icons.Default.ChevronRight, contentDescription = LocalizedStrings.readerNextSection, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // 标注统计
                val chapterHighlights = uiState.highlights.filter { it.chapterId == selectedChapter?.id }
                val chapterAnnotations = uiState.annotations.filter { it.chapterId == selectedChapter?.id }
                val chapterBookmarks = uiState.bookmarks.filter { it.chapterId == selectedChapter?.id }
                if (!pureReadingMode && (chapterHighlights.isNotEmpty() || chapterAnnotations.isNotEmpty() || chapterBookmarks.isNotEmpty() || uiState.bookmarks.isNotEmpty())) {
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
                                    "${chapterHighlights.size} ${LocalizedStrings.readerHighlights}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (chapterAnnotations.isNotEmpty()) {
                                Text(
                                    "${chapterAnnotations.size} ${LocalizedStrings.readerAnnotations}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            if (uiState.bookmarks.isNotEmpty()) {
                                Text(
                                    "${uiState.bookmarks.size} ${LocalizedStrings.readerBookmarks}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))

                            // 阅读进度
                            uiState.readingProgress?.let { progress ->
                                val sec = progress.totalReadTimeSeconds
                                if (sec > 0) {
                                    val h = sec / 3600
                                    val m = (sec % 3600) / 60
                                    val s = sec % 60
                                    val timeStr = buildString {
                                        if (h > 0) append("${h}h")
                                        if (m > 0) append("${m}m")
                                        append("${s}s")
                                    }
                                    Text(
                                        timeStr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (progress.totalReadTimeMinutes > 0) {
                                    Text(
                                        "${progress.totalReadTimeMinutes}${LocalizedStrings.readerMinutes}",
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
                                    contentDescription = LocalizedStrings.readerManageBookmarks,
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
                                    contentDescription = LocalizedStrings.readerManageAnnotations,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Content area
                if (uiState.useStructuredNodes) {
                    // New flow: structured document nodes
                    if (uiState.documentNodes.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(pureReadingMode) {
                                    if (pureReadingMode) {
                                        detectTapGestures(
                                            onTap = { pureReadingMode = false }
                                        )
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, _, zoom, _ ->
                                        val newSize = (fontSize * zoom).coerceIn(12f, 32f)
                                        if (newSize != fontSize) {
                                            fontSize = newSize
                                            fontSizeGeneration++
                                            viewModel.updateFontSize(newSize)
                                        }
                                    }
                                }
                        ) {
                            DocumentNodeList(
                                nodes = uiState.documentNodes,
                                highlights = uiState.highlights,
                                annotations = uiState.annotations,
                                pureReadingMode = pureReadingMode,
                                fontSize = fontSize,
                                listState = structuredListState,
                                activeNodeSelection = activeNodeSelection,
                                selectedColor = uiState.selectedColor,
                                onSelectionChanged = viewModel::setActiveNodeSelection,
                                onHighlight = { nodeId, text, start, end, color ->
                                    viewModel.addNodeHighlight(nodeId, start, end, text, color)
                                    viewModel.setActiveNodeSelection(null)
                                },
                                onAnnotate = { nodeId, text, start, end ->
                                    nodeAnnotationSelection = NodeSelection(nodeId, text, start, end)
                                    showNodeAnnotationDialog = true
                                },
                                onBookmark = { nodeId, text ->
                                    viewModel.addNodeBookmark(nodeId, text)
                                    viewModel.setActiveNodeSelection(null)
                                },
                                onErase = { nodeId, start, end ->
                                    viewModel.eraseOverlapping(nodeId, start, end)
                                    viewModel.setActiveNodeSelection(null)
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Floating action bar at top-left
                            val sel = activeNodeSelection
                            if (sel != null) {
                                SelectionActionBar(
                                    selectedText = sel.text,
                                    onHighlight = { color ->
                                        viewModel.addNodeHighlight(sel.nodeId, sel.startIndex, sel.endIndex, sel.text, color)
                                        viewModel.setActiveNodeSelection(null)
                                    },
                                    onAnnotate = {
                                        nodeAnnotationSelection = sel
                                        showNodeAnnotationDialog = true
                                    },
                                    onBookmark = {
                                        viewModel.addNodeBookmark(sel.nodeId, sel.text)
                                        viewModel.setActiveNodeSelection(null)
                                    },
                                    onErase = {
                                        viewModel.eraseOverlapping(sel.nodeId, sel.startIndex, sel.endIndex)
                                        viewModel.setActiveNodeSelection(null)
                                    },
                                    onCancel = { viewModel.setActiveNodeSelection(null) },
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }

                            // Right-side scroll progress bar
                            if (!pureReadingMode) {
                                val totalItems = uiState.documentNodes.size
                                if (totalItems > 0) {
                                    val firstVisible = structuredListState.firstVisibleItemIndex
                                    val scrollProgress = firstVisible.toFloat() / totalItems.toFloat()
                                    val sliderHeight = 40.dp

                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(12.dp)
                                            .align(Alignment.CenterEnd)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(2.dp)
                                                .align(Alignment.Center)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(6.dp)
                                                .height(sliderHeight)
                                                .align(Alignment.TopStart)
                                                .offset(x = 3.dp, y = (scrollProgress * 600).dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                        )
                                    }
                                }
                            }

                            // Font size indicator overlay (top-right)
                            if (showFontSize) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 16.dp)
                                        .background(
                                            MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "${fontSize.toInt()} sp",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.inverseOnSurface
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                LocalizedStrings.readerContentEmpty,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (selectedChapter != null) {
                    // Old flow: chapter-based rendering
                    if (selectedChapter.content.isNotBlank()) {
                        val scrollState = rememberScrollState()
                        LaunchedEffect(selectedChapter.id) {
                            scrollState.scrollTo(0)
                        }
                        val scrollProgress = if (scrollState.maxValue > 0)
                            scrollState.value.toFloat() / scrollState.maxValue.toFloat()
                        else 0f

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(pureReadingMode) {
                                    if (pureReadingMode) {
                                        detectTapGestures(
                                            onTap = { pureReadingMode = false }
                                        )
                                    }
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                            ) {
                                AnnotatedContent(
                                    content = selectedChapter.content,
                                    highlights = chapterHighlights,
                                    annotations = chapterAnnotations,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = if (pureReadingMode) 24.dp else 20.dp,
                                            vertical = 16.dp
                                        )
                                )
                            }

                            // 右侧阅读进度条
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(20.dp)
                                    .align(Alignment.CenterEnd)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(3.dp)
                                        .align(Alignment.Center)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                )
                                val sliderHeight = 40.dp
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(sliderHeight)
                                        .align(Alignment.TopStart)
                                        .offset(y = (scrollProgress * 600).dp)
                                        .padding(horizontal = 4.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (pureReadingMode)
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                            else
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                )
                                if (pureReadingMode) {
                                    Text(
                                        text = "${(scrollProgress * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 4.dp)
                                    )
                                }
                            }

                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                LocalizedStrings.readerSectionEmpty,
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
                            LocalizedStrings.readerSelectFromToc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            } // end Box for progress bar overlay
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
            title = { Text(LocalizedStrings.readerClearAnnotations) },
            text = { Text(LocalizedStrings.readerClearConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAll()
                        showClearDialog = false
                    }
                ) {
                    Text(LocalizedStrings.clear, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(LocalizedStrings.cancel)
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
            title = { Text(LocalizedStrings.readerAddBookmark) },
            text = {
                Column {
                    Text(
                        "${LocalizedStrings.readerChapter}: ${uiState.selectedChapter?.title ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bookmarkNote,
                        onValueChange = { bookmarkNote = it },
                        label = { Text(LocalizedStrings.readerNoteOptional) },
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
                    Text(LocalizedStrings.add)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookmarkDialog = false }) {
                    Text(LocalizedStrings.cancel)
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
        android.util.Log.d("DeleteModule", "title='${uiState.module?.title}', titleBytes=${uiState.module?.title?.toByteArray()?.joinToString(",") { it.toString() }}")
        AlertDialog(
            onDismissRequest = { showDeleteModuleDialog = false },
            title = { Text(LocalizedStrings.readerDeleteModuleTitle) },
            text = { Text(LocalizedStrings.readerDeleteModuleConfirm(uiState.module?.title ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteModule()
                        showDeleteModuleDialog = false
                        onBack()
                    }
                ) {
                    Text(LocalizedStrings.delete, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteModuleDialog = false }) {
                    Text(LocalizedStrings.cancel)
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
                    "${LocalizedStrings.readerEdit}: ${uiState.selectedChapter?.title ?: ""}",
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
                        Text(LocalizedStrings.cancel)
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
                        Text(LocalizedStrings.save)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // 结构化节点批注弹窗
    if (showNodeAnnotationDialog && nodeAnnotationSelection != null) {
        val sel = nodeAnnotationSelection!!
        AnnotationDialog(
            selectedText = sel.text,
            onDismiss = {
                showNodeAnnotationDialog = false
                nodeAnnotationSelection = null
            },
            onSave = { note ->
                viewModel.addNodeAnnotation(
                    nodeId = sel.nodeId,
                    startIndex = sel.startIndex,
                    endIndex = sel.endIndex,
                    selectedText = sel.text,
                    note = note
                )
                showNodeAnnotationDialog = false
                nodeAnnotationSelection = null
                viewModel.setActiveNodeSelection(null)
            }
        )
    }
}

@Composable
private fun AnnotatedContent(
    content: String,
    highlights: List<HighlightEntity>,
    annotations: List<AnnotationEntity>,
    modifier: Modifier = Modifier
) {
    val annotatedString = buildAnnotatedString {
        append(content)

        highlights.forEach { highlight ->
            val start = highlight.startIndex.coerceIn(0, content.length)
            val end = highlight.endIndex.coerceIn(0, content.length)
            if (start < end) {
                addStyle(
                    style = SpanStyle(background = Color(highlight.color).copy(alpha = 0.3f)),
                    start = start, end = end
                )
            }
        }

        annotations.forEach { annotation ->
            val start = annotation.startIndex.coerceIn(0, content.length)
            val end = annotation.endIndex.coerceIn(0, content.length)
            if (start < end) {
                addStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = Color(annotation.color)
                    ),
                    start = start, end = end
                )
            }
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = MaterialTheme.typography.bodyLarge.fontSize * 1.8
        ),
        modifier = modifier
    )
}

// ==================== Document Node Rendering ====================

@Composable
private fun DocumentNodeList(
    nodes: List<DocumentNodeEntity>,
    highlights: List<HighlightEntity>,
    annotations: List<AnnotationEntity>,
    pureReadingMode: Boolean,
    fontSize: Float = 16f,
    listState: androidx.compose.foundation.lazy.LazyListState,
    activeNodeSelection: NodeSelection?,
    selectedColor: Long,
    onSelectionChanged: (NodeSelection?) -> Unit,
    onHighlight: (nodeId: String, text: String, startIndex: Int, endIndex: Int, color: Long) -> Unit,
    onAnnotate: (nodeId: String, text: String, startIndex: Int, endIndex: Int) -> Unit,
    onBookmark: (nodeId: String, text: String) -> Unit,
    onErase: (nodeId: String, startIndex: Int, endIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hPad = if (pureReadingMode) 24.dp else 20.dp
    val nodeHighlights = remember(highlights) { highlights.filter { it.nodeId != null } }
    val nodeAnnotations = remember(annotations) { annotations.filter { it.nodeId != null } }

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = hPad, vertical = 16.dp)
        ) {
            items(nodes.size, key = { nodes[it].nodeId }) { index ->
                val node = nodes[index]
                val nodeHl = remember(nodeHighlights, node.nodeId) { nodeHighlights.filter { it.nodeId == node.nodeId } }
                val nodeAn = remember(nodeAnnotations, node.nodeId) { nodeAnnotations.filter { it.nodeId == node.nodeId } }
                DocumentNodeRenderer(
                    node = node,
                    highlights = nodeHl,
                    annotations = nodeAn,
                    pureReadingMode = pureReadingMode,
                    fontSize = fontSize,
                    activeNodeSelection = activeNodeSelection,
                    selectedColor = selectedColor,
                    onSelectionChanged = onSelectionChanged,
                    onHighlight = onHighlight,
                    onAnnotate = onAnnotate,
                    onBookmark = onBookmark,
                    onErase = onErase
                )
            }
        }
    }
}

@Composable
private fun DocumentNodeRenderer(
    node: DocumentNodeEntity,
    highlights: List<HighlightEntity>,
    annotations: List<AnnotationEntity>,
    pureReadingMode: Boolean,
    fontSize: Float = 16f,
    activeNodeSelection: NodeSelection?,
    selectedColor: Long,
    onSelectionChanged: (NodeSelection?) -> Unit,
    onHighlight: (nodeId: String, text: String, startIndex: Int, endIndex: Int, color: Long) -> Unit,
    onAnnotate: (nodeId: String, text: String, startIndex: Int, endIndex: Int) -> Unit,
    onBookmark: (nodeId: String, text: String) -> Unit,
    onErase: (nodeId: String, startIndex: Int, endIndex: Int) -> Unit
) {
    val bodySize = fontSize.sp
    val lineHeight = bodySize * 1.8

    when (node.type) {
        "heading" -> HeadingNode(node, bodySize)
        "paragraph" -> ParagraphNode(
            node = node,
            highlights = highlights,
            annotations = annotations,
            pureReadingMode = pureReadingMode,
            bodySize = bodySize,
            lineHeight = lineHeight,
            activeNodeSelection = activeNodeSelection,
            selectedColor = selectedColor,
            onSelectionChanged = onSelectionChanged,
            onHighlight = onHighlight,
            onAnnotate = onAnnotate,
            onBookmark = onBookmark,
            onErase = onErase
        )
        "table" -> TableNode(node, bodySize)
        "image" -> ImageNode(node)
        "quote" -> QuoteNode(node, bodySize)
        "list_item" -> ListItemNode(node, bodySize)
    }
}

@Composable
private fun HeadingNode(node: DocumentNodeEntity, bodySize: TextUnit) {
    val headingSize = when (node.level) {
        1 -> bodySize * 1.8f
        2 -> bodySize * 1.5f
        3 -> bodySize * 1.25f
        else -> bodySize * 1.1f
    }
    Text(
        text = node.content,
        style = TextStyle(fontSize = headingSize, fontWeight = FontWeight.Bold, lineHeight = headingSize * 1.4),
        modifier = Modifier.padding(vertical = 12.dp)
    )
}

@Composable
private fun ParagraphNode(
    node: DocumentNodeEntity,
    highlights: List<HighlightEntity>,
    annotations: List<AnnotationEntity>,
    pureReadingMode: Boolean,
    bodySize: TextUnit,
    lineHeight: TextUnit,
    activeNodeSelection: NodeSelection?,
    selectedColor: Long,
    onSelectionChanged: (NodeSelection?) -> Unit,
    onHighlight: (nodeId: String, text: String, startIndex: Int, endIndex: Int, color: Long) -> Unit,
    onAnnotate: (nodeId: String, text: String, startIndex: Int, endIndex: Int) -> Unit,
    onBookmark: (nodeId: String, text: String) -> Unit,
    onErase: (nodeId: String, startIndex: Int, endIndex: Int) -> Unit
) {
    val content = node.content
    val textColor = MaterialTheme.colorScheme.onSurface
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val selectionColor = if (isDark) Color.White.copy(alpha = 0.25f) else Color(0xFF9E9E9E).copy(alpha = 0.3f)
    val baseStyle = TextStyle(fontSize = bodySize, lineHeight = lineHeight, color = textColor)

    // Selection state (local to this paragraph)
    var committedSelection by remember(node.nodeId) { mutableStateOf<Pair<Int, Int>?>(null) }
    var isDragging by remember(node.nodeId) { mutableStateOf(false) }
    var dragRange by remember(node.nodeId) { mutableStateOf<Pair<Int, Int>?>(null) }
    var textLayoutResult by remember(node.nodeId) { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }

    // Sync with global active selection
    LaunchedEffect(activeNodeSelection) {
        if (activeNodeSelection == null || activeNodeSelection.nodeId != node.nodeId) {
            committedSelection = null
            isDragging = false
            dragRange = null
        }
    }

    // The range to highlight: live drag takes priority
    val displayRange = if (isDragging) dragRange else committedSelection

    // Build AnnotatedString with inline formatting + highlights + annotations + selection
    val annotatedString = remember(content, highlights, annotations, textColor, displayRange, selectionColor) {
        buildAnnotatedString {
            append(parseInlineFormatting(content))
            addStyle(SpanStyle(color = textColor), 0, content.length)

            highlights.forEach { hl ->
                val start = hl.startIndex.coerceIn(0, content.length)
                val end = hl.endIndex.coerceIn(0, content.length)
                if (start < end) {
                    addStyle(
                        style = SpanStyle(background = Color(hl.color).copy(alpha = 0.3f)),
                        start = start, end = end
                    )
                }
            }

            annotations.forEach { ann ->
                val start = ann.startIndex.coerceIn(0, content.length)
                val end = ann.endIndex.coerceIn(0, content.length)
                if (start < end) {
                    addStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = Color(ann.color)
                        ),
                        start = start, end = end
                    )
                }
            }

            // Selection highlight (real-time during drag, or after commit)
            if (displayRange != null) {
                val s = minOf(displayRange.first, displayRange.second).coerceIn(0, content.length)
                val e = maxOf(displayRange.first, displayRange.second).coerceIn(0, content.length)
                if (s < e) {
                    addStyle(
                        style = SpanStyle(background = selectionColor),
                        start = s, end = e
                    )
                }
            }
        }
    }

    Column {
        BasicText(
            text = annotatedString,
            style = baseStyle,
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .then(
                    if (!pureReadingMode) Modifier.pointerInput(content) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                textLayoutResult?.let { layout ->
                                    val charOffset = layout.getOffsetForPosition(offset)
                                    isDragging = true
                                    dragRange = Pair(charOffset, charOffset)
                                    committedSelection = null
                                    onSelectionChanged(null)
                                }
                            },
                            onDrag = { change, _ ->
                                textLayoutResult?.let { layout ->
                                    val charOffset = layout.getOffsetForPosition(change.position)
                                    val current = dragRange
                                    if (current != null) {
                                        dragRange = Pair(current.first, charOffset)
                                    }
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                val range = dragRange
                                if (range != null) {
                                    val start = range.first.coerceIn(0, content.length)
                                    val end = range.second.coerceIn(0, content.length)
                                    val min = minOf(start, end)
                                    val max = maxOf(start, end)
                                    if (min < max) {
                                        val selectedText = content.substring(min, max)
                                        committedSelection = Pair(min, max)
                                        onSelectionChanged(
                                            NodeSelection(node.nodeId, selectedText, min, max)
                                        )
                                    } else {
                                        committedSelection = null
                                        dragRange = null
                                        onSelectionChanged(null)
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                committedSelection = null
                                dragRange = null
                            }
                        )
                    } else Modifier
                )
        )
    }
}

@Composable
private fun SelectionActionBar(
    selectedText: String,
    onHighlight: (color: Long) -> Unit,
    onAnnotate: () -> Unit,
    onBookmark: () -> Unit,
    onErase: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // 3 highlight color dots
            listOf(0xFFFFEB3B, 0xFF4CAF50, 0xFFE91E63).forEach { color ->
                IconButton(
                    onClick = { onHighlight(color) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(color))
                    )
                }
            }

            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            IconButton(onClick = onAnnotate, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Comment, contentDescription = LocalizedStrings.readerAnnotate, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onBookmark, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Bookmark, contentDescription = LocalizedStrings.readerBookmark, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onErase, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = LocalizedStrings.readerErase, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = LocalizedStrings.cancel, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun TableNode(node: DocumentNodeEntity, bodySize: TextUnit) {
    val rows = node.tableData?.let { parseTableJson(it) } ?: return
    if (rows.isEmpty()) return
    val tableSize = bodySize * 0.9f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        rows.forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (rowIdx == 0)
                            Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        else Modifier
                    )
            ) {
                row.forEach { cell ->
                    Text(
                        text = cell,
                        style = TextStyle(
                            fontSize = tableSize,
                            fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal,
                            lineHeight = tableSize * 1.4
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
            if (rowIdx < rows.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun ImageNode(node: DocumentNodeEntity) {
    val uri = node.imageUri ?: return
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(4.dp)),
        contentScale = ContentScale.FillWidth
    )
}

@Composable
private fun QuoteNode(node: DocumentNodeEntity, bodySize: TextUnit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(1.5.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = node.content,
                style = TextStyle(
                    fontSize = bodySize,
                    fontStyle = FontStyle.Italic,
                    lineHeight = bodySize * 1.6
                )
            )
        }
    }
}

@Composable
private fun ListItemNode(node: DocumentNodeEntity, bodySize: TextUnit) {
    Row(
        modifier = Modifier.padding(
            start = (node.level * 16).dp,
            top = 2.dp,
            bottom = 2.dp
        )
    ) {
        Text(
            text = "  •  ",
            style = TextStyle(fontSize = bodySize)
        )
        Text(
            text = node.content,
            style = TextStyle(fontSize = bodySize, lineHeight = bodySize * 1.6)
        )
    }
}

// ==================== Inline Formatting Parser ====================

private fun parseInlineFormatting(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            when {
                // Bold+Italic: ***text***
                i + 2 < text.length && text.startsWith("***", i) -> {
                    val end = text.indexOf("***", i + 3)
                    if (end > 0) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 3, end))
                        }
                        i = end + 3
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Bold: **text**
                i + 1 < text.length && text.startsWith("**", i) -> {
                    val end = text.indexOf("**", i + 2)
                    if (end > 0) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Italic: *text*
                text[i] == '*' -> {
                    val end = text.indexOf('*', i + 1)
                    if (end > 0) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(text.substring(i + 1, end))
                        }
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                // Strikethrough: ~~text~~
                i + 1 < text.length && text.startsWith("~~", i) -> {
                    val end = text.indexOf("~~", i + 2)
                    if (end > 0) {
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(text.substring(i + 2, end))
                        }
                        i = end + 2
                    } else {
                        append(text[i])
                        i++
                    }
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}

private fun parseTableJson(json: String): List<List<String>> {
    return try {
        val arr = org.json.JSONArray(json)
        (0 until arr.length()).map { r ->
            val row = arr.getJSONArray(r)
            (0 until row.length()).map { row.getString(it) }
        }
    } catch (e: Exception) {
        emptyList()
    }
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

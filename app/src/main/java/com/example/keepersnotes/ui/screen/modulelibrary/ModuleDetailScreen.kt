package com.example.keepersnotes.ui.screen.modulelibrary

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.keepersnotes.data.local.entity.AnnotationEntity
import com.example.keepersnotes.data.local.entity.BookmarkEntity
import com.example.keepersnotes.data.local.entity.HighlightEntity
import com.example.keepersnotes.data.local.entity.ImageEntity
import com.example.keepersnotes.data.local.entity.KpMemoEntity
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.component.EditModuleDialog
import com.example.keepersnotes.util.Chapter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleDetailScreen(
    moduleId: String,
    onBack: () -> Unit,
    onStartReading: () -> Unit,
    onNavigateToChapter: (String) -> Unit = {},
    onImageClick: (Int) -> Unit = {},
    onNavigateToRelationship: () -> Unit = {},
    onNavigateToEntityList: (String) -> Unit = {},
    viewModel: ModuleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var imageToDelete by remember { mutableStateOf<ImageEntity?>(null) }
    var selectedImageTab by rememberSaveable { mutableIntStateOf(0) } // 0=信息, 1=图片
    var viewingImage by remember { mutableStateOf<ImageEntity?>(null) }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importImage(it) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = uiState.module?.title ?: "模组详情",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (uiState.module?.isFavorite == true) Icons.Default.Favorite
                            else Icons.Default.FavoriteBorder,
                            contentDescription = if (uiState.module?.isFavorite == true) "取消收藏" else "收藏",
                            tint = if (uiState.module?.isFavorite == true) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            onClick = {
                                showMenu = false
                                showEditDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedImageTab == 1) {
                FloatingActionButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Add, contentDescription = "添加图片")
                }
            } else {
                ExtendedFloatingActionButton(
                    onClick = onStartReading,
                    icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                    text = { Text("开始阅读") }
                )
            }
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

        val module = uiState.module
        if (module == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("模组不存在")
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Tab row
            PrimaryTabRow(selectedTabIndex = selectedImageTab) {
                Tab(
                    selected = selectedImageTab == 0,
                    onClick = { selectedImageTab = 0 },
                    text = { Text("详情") }
                )
                Tab(
                    selected = selectedImageTab == 1,
                    onClick = { selectedImageTab = 1 },
                    text = { Text("图片(${uiState.images.size})") }
                )
                Tab(
                    selected = selectedImageTab == 2,
                    onClick = { selectedImageTab = 2 },
                    text = { Text("笔记(${uiState.highlights.size + uiState.annotations.size + uiState.bookmarks.size + uiState.memos.size})") }
                )
                Tab(
                    selected = selectedImageTab == 3,
                    onClick = { selectedImageTab = 3 },
                    text = { Text("模组设置") }
                )
            }

            when (selectedImageTab) {
                0 -> ModuleDetailContent(
                    module = module,
                    uiState = uiState,
                    onStartReading = onStartReading,
                    onNavigateToChapter = onNavigateToChapter,
                    getChapterTitle = viewModel::getChapterTitle
                )
                1 -> ModuleImageGrid(
                    images = uiState.images,
                    onImageClick = { image -> viewingImage = image },
                    onDeleteImage = { imageToDelete = it }
                )
                2 -> ModuleNotesContent(
                    uiState = uiState,
                    onNavigateToChapter = onNavigateToChapter,
                    getChapterTitle = viewModel::getChapterTitle
                )
                3 -> ModuleSettingsContent(
                    uiState = uiState,
                    onNavigateToEntityList = onNavigateToEntityList,
                    onNavigateToRelationship = { onNavigateToRelationship() }
                )
            }
        }
    }

    // 编辑弹窗
    if (showEditDialog && uiState.module != null) {
        EditModuleDialog(
            module = uiState.module!!,
            onDismiss = { showEditDialog = false },
            onSave = { updatedModule ->
                viewModel.updateModule(updatedModule)
                showEditDialog = false
            }
        )
    }

    // 删除模组确认弹窗
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除模组") },
            text = { Text("确定要删除「${uiState.module?.title ?: ""}」吗？此操作将同时删除该模组的所有高亮、批注、书签和图片，且不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteModule()
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

    // 删除图片确认弹窗
    imageToDelete?.let { image ->
        AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text("删除图片") },
            text = { Text("确定要删除「${image.title}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteImage(image.imageId)
                        imageToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { imageToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    // 图片查看器
    viewingImage?.let { image ->
        ImageViewerDialog(
            image = image,
            onDismiss = { viewingImage = null }
        )
    }
}

@Composable
private fun ModuleDetailContent(
    module: com.example.keepersnotes.data.local.entity.ModuleEntity,
    uiState: ModuleDetailUiState,
    onStartReading: () -> Unit,
    onNavigateToChapter: (String) -> Unit,
    getChapterTitle: (String) -> String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 基本信息卡片
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = module.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (module.author.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "作者: ${module.author}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 标签行
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (module.system.isNotBlank()) {
                            SuggestionChip(onClick = {}, label = { Text(module.system) })
                        }
                        if (module.difficulty.isNotBlank()) {
                            SuggestionChip(onClick = {}, label = { Text(module.difficulty) })
                        }
                        if (module.playerCount.isNotBlank()) {
                            SuggestionChip(onClick = {}, label = { Text("${module.playerCount}人") })
                        }
                        if (module.duration.isNotBlank()) {
                            SuggestionChip(onClick = {}, label = { Text(module.duration) })
                        }
                    }
                }
            }
        }

        // 简介
        if (module.synopsis.isNotBlank()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("简介", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(module.synopsis, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // 阅读统计
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("阅读统计", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("${uiState.readingProgress?.totalReadTimeMinutes ?: 0}", "阅读分钟")
                        StatItem("${uiState.readingProgress?.readCount ?: 0}", "阅读次数")
                        StatItem("${uiState.highlights.size}", "高亮")
                        StatItem("${uiState.annotations.size}", "批注")
                        StatItem("${uiState.bookmarks.size}", "书签")
                    }
                }
            }
        }

        // 标签
        if (module.tags.isNotBlank()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("标签", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(module.tags, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // 阅读笔记
        val totalNotes = uiState.highlights.size + uiState.annotations.size + uiState.bookmarks.size + uiState.memos.size
        if (totalNotes > 0) {
            item {
                Text(
                    "阅读笔记 ($totalNotes)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // 高亮
            if (uiState.highlights.isNotEmpty()) {
                item {
                    NotesExpandableSection(
                        title = "高亮",
                        count = uiState.highlights.size,
                        icon = Icons.Default.Highlight,
                        iconTint = Color(0xFFFFEB3B)
                    ) {
                        uiState.highlights.forEach { highlight ->
                            NoteHighlightItem(
                                highlight = highlight,
                                chapterTitle = getChapterTitle(highlight.chapterId),
                                onClick = { onNavigateToChapter(highlight.chapterId) }
                            )
                        }
                    }
                }
            }

            // 批注
            if (uiState.annotations.isNotEmpty()) {
                item {
                    NotesExpandableSection(
                        title = "批注",
                        count = uiState.annotations.size,
                        icon = Icons.AutoMirrored.Filled.Comment,
                        iconTint = Color(0xFF4CAF50)
                    ) {
                        uiState.annotations.forEach { annotation ->
                            NoteAnnotationItem(
                                annotation = annotation,
                                chapterTitle = getChapterTitle(annotation.chapterId),
                                onClick = { onNavigateToChapter(annotation.chapterId) }
                            )
                        }
                    }
                }
            }

            // 书签
            if (uiState.bookmarks.isNotEmpty()) {
                item {
                    NotesExpandableSection(
                        title = "书签",
                        count = uiState.bookmarks.size,
                        icon = Icons.Default.Bookmark,
                        iconTint = Color(0xFFFF9800)
                    ) {
                        uiState.bookmarks.forEach { bookmark ->
                            NoteBookmarkItem(
                                bookmark = bookmark,
                                chapterTitle = bookmark.chapterTitle.ifBlank { getChapterTitle(bookmark.chapterId) },
                                onClick = { onNavigateToChapter(bookmark.chapterId) }
                            )
                        }
                    }
                }
            }

            // 笔记
            if (uiState.memos.isNotEmpty()) {
                item {
                    NotesExpandableSection(
                        title = "笔记",
                        count = uiState.memos.size,
                        icon = Icons.AutoMirrored.Filled.Note,
                        iconTint = MaterialTheme.colorScheme.primary
                    ) {
                        uiState.memos.forEach { memo ->
                            NoteMemoItem(
                                memo = memo,
                                chapterTitle = memo.chapterTitle.ifBlank { memo.chapterId?.let { getChapterTitle(it) } ?: "" },
                                onClick = { memo.chapterId?.let { onNavigateToChapter(it) } }
                            )
                        }
                    }
                }
            }
        }

        // 章节目录
        if (uiState.chapters.isNotEmpty()) {
            item {
                Text(
                    "章节目录 (${countChapters(uiState.chapters)} 章)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(uiState.chapters) { chapter ->
                ChapterListItem(
                    chapter = chapter,
                    depth = 0,
                    onClick = { onNavigateToChapter(chapter.id) }
                )
            }
        }
    }
}

@Composable
private fun ModuleImageGrid(
    images: List<ImageEntity>,
    onImageClick: (ImageEntity) -> Unit,
    onDeleteImage: (ImageEntity) -> Unit
) {
    if (images.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "暂无图片，点击右下角 + 添加",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(images.size) { index ->
                val image = images[index]
                ModuleImageCard(
                    image = image,
                    onClick = { onImageClick(image) },
                    onDelete = { onDeleteImage(image) }
                )
            }
        }
    }
}

@Composable
private fun ModuleImageCard(
    image: ImageEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.aspectRatio(1f).clickable(onClick = onClick)
    ) {
        Box {
            val file = File(image.filePath)
            if (file.exists()) {
                Image(
                    painter = rememberAsyncImagePainter(model = file),
                    contentDescription = image.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "图片丢失",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // 删除按钮
            IconButton(
                onClick = { showDeleteMenu = true },
                modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
            ) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "更多",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            DropdownMenu(
                expanded = showDeleteMenu,
                onDismissRequest = { showDeleteMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        showDeleteMenu = false
                        onDelete()
                    }
                )
            }

            // 标题
            if (image.title.isNotBlank()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = image.title,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NotesExpandableSection(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun NoteHighlightItem(
    highlight: HighlightEntity,
    chapterTitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(highlight.color))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = highlight.selectedText,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (chapterTitle.isNotBlank()) {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NoteAnnotationItem(
    annotation: AnnotationEntity,
    chapterTitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(annotation.color))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = annotation.selectedText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (annotation.note.isNotBlank()) {
                Text(
                    text = annotation.note,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (chapterTitle.isNotBlank()) {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NoteBookmarkItem(
    bookmark: BookmarkEntity,
    chapterTitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(bookmark.color))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val displayText = bookmark.selectedText.ifBlank { bookmark.note }
            if (displayText.isNotBlank()) {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (chapterTitle.isNotBlank()) {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun NoteMemoItem(
    memo: KpMemoEntity,
    chapterTitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            when (memo.type) {
                "clue" -> Icons.Default.Search
                "plot" -> Icons.Default.AutoStories
                "todo" -> Icons.Default.CheckBox
                "reminder" -> Icons.Default.Alarm
                "rule" -> Icons.Default.Gavel
                else -> Icons.AutoMirrored.Filled.Note
            },
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (memo.title.isNotBlank()) {
                Text(
                    text = memo.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (memo.content.isNotBlank()) {
                Text(
                    text = memo.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (chapterTitle.isNotBlank()) {
                Text(
                    text = chapterTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChapterListItem(
    chapter: Chapter,
    depth: Int,
    onClick: () -> Unit
) {
    val hasChildren = chapter.children.isNotEmpty()
    var expanded by remember { mutableStateOf(depth < 2) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (hasChildren) expanded = !expanded else onClick()
                }
                .padding(start = (depth * 16).dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasChildren) {
                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = chapter.title,
                style = if (depth == 0) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                else MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (!hasChildren && chapter.content.isNotBlank()) {
                Text(
                    "${chapter.content.length}字",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (hasChildren && expanded) {
            chapter.children.forEach { child ->
                ChapterListItem(chapter = child, depth = depth + 1, onClick = onClick)
            }
        }
    }
}

private fun countChapters(chapters: List<Chapter>): Int {
    var count = 0
    fun count(list: List<Chapter>) {
        list.forEach { chapter -> count++; count(chapter.children) }
    }
    count(chapters)
    return count
}

@Composable
private fun ModuleNotesContent(
    uiState: ModuleDetailUiState,
    onNavigateToChapter: (String) -> Unit,
    getChapterTitle: (String) -> String
) {
    val totalNotes = uiState.highlights.size + uiState.annotations.size + uiState.bookmarks.size + uiState.memos.size

    if (totalNotes == 0) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.AutoMirrored.Filled.Note,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "暂无阅读笔记",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (uiState.highlights.isNotEmpty()) {
            item {
                NotesExpandableSection(
                    title = "高亮",
                    count = uiState.highlights.size,
                    icon = Icons.Default.Highlight,
                    iconTint = Color(0xFFFFEB3B)
                ) {
                    uiState.highlights.forEach { highlight ->
                        NoteHighlightItem(
                            highlight = highlight,
                            chapterTitle = getChapterTitle(highlight.chapterId),
                            onClick = { onNavigateToChapter(highlight.chapterId) }
                        )
                    }
                }
            }
        }

        if (uiState.annotations.isNotEmpty()) {
            item {
                NotesExpandableSection(
                    title = "批注",
                    count = uiState.annotations.size,
                    icon = Icons.AutoMirrored.Filled.Comment,
                    iconTint = Color(0xFF4CAF50)
                ) {
                    uiState.annotations.forEach { annotation ->
                        NoteAnnotationItem(
                            annotation = annotation,
                            chapterTitle = getChapterTitle(annotation.chapterId),
                            onClick = { onNavigateToChapter(annotation.chapterId) }
                        )
                    }
                }
            }
        }

        if (uiState.bookmarks.isNotEmpty()) {
            item {
                NotesExpandableSection(
                    title = "书签",
                    count = uiState.bookmarks.size,
                    icon = Icons.Default.Bookmark,
                    iconTint = Color(0xFFFF9800)
                ) {
                    uiState.bookmarks.forEach { bookmark ->
                        NoteBookmarkItem(
                            bookmark = bookmark,
                            chapterTitle = bookmark.chapterTitle.ifBlank { getChapterTitle(bookmark.chapterId) },
                            onClick = { onNavigateToChapter(bookmark.chapterId) }
                        )
                    }
                }
            }
        }

        if (uiState.memos.isNotEmpty()) {
            item {
                NotesExpandableSection(
                    title = "笔记",
                    count = uiState.memos.size,
                    icon = Icons.AutoMirrored.Filled.Note,
                    iconTint = MaterialTheme.colorScheme.primary
                ) {
                    uiState.memos.forEach { memo ->
                        NoteMemoItem(
                            memo = memo,
                            chapterTitle = memo.chapterTitle.ifBlank { memo.chapterId?.let { getChapterTitle(it) } ?: "" },
                            onClick = { memo.chapterId?.let { onNavigateToChapter(it) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModuleSettingsContent(
    uiState: ModuleDetailUiState,
    onNavigateToEntityList: (String) -> Unit,
    onNavigateToRelationship: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 默认NPC
        item {
            SettingsNavItem(
                title = "默认NPC",
                count = uiState.defaultNpcs.size,
                icon = Icons.Default.People,
                iconTint = Color(0xFF4CAF50),
                onClick = { onNavigateToEntityList("npc") }
            )
        }

        // 地点
        item {
            SettingsNavItem(
                title = "地点",
                count = uiState.locations.size,
                icon = Icons.Default.LocationOn,
                iconTint = Color(0xFFFF9800),
                onClick = { onNavigateToEntityList("location") }
            )
        }

        // 组织
        item {
            SettingsNavItem(
                title = "组织",
                count = uiState.organizations.size,
                icon = Icons.Default.Business,
                iconTint = Color(0xFF9C27B0),
                onClick = { onNavigateToEntityList("organization") }
            )
        }

        // 线索
        item {
            SettingsNavItem(
                title = "线索",
                count = uiState.clues.size,
                icon = Icons.Default.Search,
                iconTint = Color(0xFFE91E63),
                onClick = { onNavigateToEntityList("clue") }
            )
        }

        // 人物关系网
        item {
            SettingsNavItem(
                title = "人物关系网",
                count = uiState.relationships.size,
                icon = Icons.Default.AccountTree,
                iconTint = Color(0xFF795548),
                onClick = onNavigateToRelationship
            )
        }
    }
}

@Composable
private fun SettingsNavItem(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageViewerDialog(
    image: ImageEntity,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val file = File(image.filePath)
            if (file.exists()) {
                Image(
                    painter = rememberAsyncImagePainter(model = file),
                    contentDescription = image.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        ),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    "图片丢失",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = Color.White
                )
            }
            // Title
            if (image.title.isNotBlank()) {
                Text(
                    text = image.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

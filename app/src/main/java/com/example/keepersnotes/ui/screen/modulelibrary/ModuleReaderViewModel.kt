package com.example.keepersnotes.ui.screen.modulelibrary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.AnnotationEntity
import com.example.keepersnotes.data.local.entity.BookmarkEntity
import com.example.keepersnotes.data.local.entity.DocumentNodeEntity
import com.example.keepersnotes.data.local.entity.HighlightEntity
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.local.entity.ReadingProgressEntity
import com.example.keepersnotes.data.repository.AnnotationRepository
import com.example.keepersnotes.data.repository.BookmarkRepository
import com.example.keepersnotes.data.repository.DocumentNodeRepository
import com.example.keepersnotes.data.repository.HighlightRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.data.repository.ReadingProgressRepository
import com.example.keepersnotes.util.Chapter
import com.example.keepersnotes.util.ModuleContentParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AnnotationTool {
    NONE, HIGHLIGHT, ANNOTATE, ERASER, BOOKMARK
}

data class NodeSelection(
    val nodeId: String,
    val text: String,
    val startIndex: Int,
    val endIndex: Int
)

data class ModuleReaderUiState(
    val module: ModuleEntity? = null,
    val chapters: List<Chapter> = emptyList(),
    val selectedChapter: Chapter? = null,
    val documentNodes: List<DocumentNodeEntity> = emptyList(),
    val useStructuredNodes: Boolean = false,
    val highlights: List<HighlightEntity> = emptyList(),
    val annotations: List<AnnotationEntity> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val readingProgress: ReadingProgressEntity? = null,
    val isLoading: Boolean = true,
    val activeTool: AnnotationTool = AnnotationTool.NONE,
    val selectedColor: Long = 0xFFFFEB3B,
    val eraserMode: EraserMode = EraserMode.ALL
)

enum class EraserMode {
    ALL, HIGHLIGHTS_ONLY, ANNOTATIONS_ONLY
}

@HiltViewModel
class ModuleReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val moduleRepository: ModuleRepository,
    private val documentNodeRepository: DocumentNodeRepository,
    private val highlightRepository: HighlightRepository,
    private val annotationRepository: AnnotationRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val moduleId: String = savedStateHandle.get<String>("moduleId") ?: ""
    val initialChapterId: String? = savedStateHandle.get<String>("chapterId")

    private val _uiState = MutableStateFlow(ModuleReaderUiState(isLoading = true))
    val uiState: StateFlow<ModuleReaderUiState> = _uiState.asStateFlow()

    // 恢复滚动位置用（由 UI 读取后消费）
    var initialScrollIndex: Int = 0
        private set
    var initialScrollOffset: Int = 0
        private set
    var initialFontSize: Float = 16f
        private set

    // 当前滚动位置（由 UI 持续更新）
    private var currentNodeId: String = ""
    private var currentScrollOffset: Int = 0
    private var currentFontSize: Float = 16f

    // 当前选中的文本（结构化节点模式）
    private val _activeNodeSelection = MutableStateFlow<NodeSelection?>(null)
    val activeNodeSelection: StateFlow<NodeSelection?> = _activeNodeSelection.asStateFlow()

    fun setActiveNodeSelection(selection: NodeSelection?) {
        _activeNodeSelection.value = selection
    }

    /**
     * 由 UI 调用，持续更新当前滚动位置。
     */
    private var lastSaveTime: Long = 0

    fun updateScrollPosition(nodeId: String, scrollOffset: Int) {
        currentNodeId = nodeId
        currentScrollOffset = scrollOffset
        // 节流：最多每 2 秒保存一次
        val now = System.currentTimeMillis()
        if (now - lastSaveTime > 2000) {
            lastSaveTime = now
            viewModelScope.launch {
                saveScrollPosition()
            }
        }
    }

    fun updateFontSize(fontSize: Float) {
        currentFontSize = fontSize
        // 保存字体大小
        viewModelScope.launch {
            saveScrollPosition()
        }
    }

    // 阅读时间追踪（生命周期感知）
    private var readStartTime: Long = 0
    private var accumulatedSeconds: Long = 0
    private var isForeground: Boolean = true

    fun onResume() {
        if (!isForeground) {
            isForeground = true
            readStartTime = System.currentTimeMillis()
        }
    }

    fun onPause() {
        if (isForeground && readStartTime > 0) {
            val elapsed = (System.currentTimeMillis() - readStartTime) / 1000
            if (elapsed > 0) accumulatedSeconds += elapsed
            readStartTime = 0
        }
        isForeground = false
        // 立即刷入数据库，防止进程被杀丢失时间
        flushAccumulatedTime()
    }

    private fun flushAccumulatedTime() {
        if (moduleId.isNotBlank()) {
            viewModelScope.launch {
                if (accumulatedSeconds > 0) {
                    val seconds = accumulatedSeconds
                    accumulatedSeconds = 0
                    readingProgressRepository.addReadTimeSeconds(moduleId, seconds)
                }
                // 保存滚动位置
                saveScrollPosition()
            }
        }
    }

    private suspend fun saveScrollPosition() {
        if (moduleId.isNotBlank() && currentNodeId.isNotEmpty()) {
            readingProgressRepository.updateLastNodePosition(moduleId, currentNodeId, currentScrollOffset, currentFontSize)
        }
    }

    init {
        if (moduleId.isNotBlank()) {
            moduleRepository.getModuleById(moduleId)
                .onEach { module ->
                    if (module != null) {
                        // Check if structured nodes exist for this module
                        val hasNodes = documentNodeRepository.hasNodes(moduleId)
                        android.util.Log.d("ModuleReader", "moduleId=$moduleId, hasNodes=$hasNodes")

                        if (hasNodes) {
                            // New flow: structured document nodes
                            // 确保 reading_progress 行存在，否则 addReadTimeSeconds 的 UPDATE 无效
                            readingProgressRepository.ensureProgressExists(moduleId)
                            // 读取已保存的滚动位置
                            val progress = readingProgressRepository.getProgressByModule(moduleId).firstOrNull()
                            android.util.Log.d("ModuleReader", "Loaded progress: lastNodeId=${progress?.lastNodeId}, lastScrollOffset=${progress?.lastScrollOffset}, lastFontSize=${progress?.lastFontSize}")
                            if (progress != null) {
                                initialScrollIndex = progress.lastNodeId.toIntOrNull()?.let { idx ->
                                    // lastNodeId 存储的是 index（兼容旧数据）
                                    idx
                                } ?: 0
                                initialScrollOffset = progress.lastScrollOffset
                                initialFontSize = progress.lastFontSize
                                currentFontSize = progress.lastFontSize
                            }

                            documentNodeRepository.getNodesByModule(moduleId)
                                .onEach { nodes ->
                                    android.util.Log.d("ModuleReader", "Loaded ${nodes.size} structured nodes")
                                    // 用 lastNodeId 匹配实际节点 index
                                    val savedNodeId = progress?.lastNodeId
                                    if (!savedNodeId.isNullOrEmpty()) {
                                        val matchIdx = nodes.indexOfFirst { it.nodeId == savedNodeId }
                                        if (matchIdx >= 0) {
                                            initialScrollIndex = matchIdx
                                        }
                                    }
                                    _uiState.update {
                                        it.copy(
                                            module = module,
                                            documentNodes = nodes,
                                            useStructuredNodes = true,
                                            readingProgress = progress,
                                            isLoading = false
                                        )
                                    }
                                }
                                .launchIn(viewModelScope)
                        } else {
                            // Old flow: chapters from contentJson
                            val chapters = ModuleContentParser.jsonToChapters(module.contentJson)
                            val firstChapter = findFirstLeaf(chapters)

                            val progress = readingProgressRepository.getProgressByModule(moduleId).firstOrNull()
                            val lastChapter = progress?.lastChapterId?.let { id ->
                                findChapterById(chapters, id)
                            }
                            val targetChapter = initialChapterId?.let { findChapterById(chapters, it) }
                                ?: lastChapter ?: firstChapter

                            _uiState.update {
                                it.copy(
                                    module = module,
                                    chapters = chapters,
                                    selectedChapter = it.selectedChapter ?: targetChapter,
                                    useStructuredNodes = false,
                                    readingProgress = progress,
                                    isLoading = false
                                )
                            }

                            if (progress == null && firstChapter != null) {
                                readingProgressRepository.initializeProgress(moduleId, firstChapter.id)
                            }
                        }

                        readStartTime = System.currentTimeMillis()
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                .launchIn(viewModelScope)

            // 加载高亮、批注、书签
            highlightRepository.getHighlightsByModule(moduleId)
                .onEach { highlights -> _uiState.update { it.copy(highlights = highlights) } }
                .launchIn(viewModelScope)

            annotationRepository.getAnnotationsByModule(moduleId)
                .onEach { annotations -> _uiState.update { it.copy(annotations = annotations) } }
                .launchIn(viewModelScope)

            bookmarkRepository.getBookmarksByModule(moduleId)
                .onEach { bookmarks -> _uiState.update { it.copy(bookmarks = bookmarks) } }
                .launchIn(viewModelScope)

            readingProgressRepository.getProgressByModule(moduleId)
                .onEach { progress -> _uiState.update { it.copy(readingProgress = progress) } }
                .launchIn(viewModelScope)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // onPause 可能已被调用（lifecycle observer），此时 accumulatedSeconds 可能已清零
        // 若未清零（lifecycle observer 未触发），手动计算剩余时间
        if (isForeground && readStartTime > 0) {
            val elapsed = (System.currentTimeMillis() - readStartTime) / 1000
            if (elapsed > 0) accumulatedSeconds += elapsed
            readStartTime = 0
            isForeground = false
        }
        // onCleared 时 viewModelScope 已取消，必须用 runBlocking 同步写入
        if (accumulatedSeconds > 0 && moduleId.isNotBlank()) {
            kotlinx.coroutines.runBlocking {
                readingProgressRepository.addReadTimeSeconds(moduleId, accumulatedSeconds)
            }
            accumulatedSeconds = 0
        }
    }

    fun selectChapter(chapter: Chapter) {
        _uiState.update { it.copy(selectedChapter = chapter) }
        // 更新阅读进度
        viewModelScope.launch {
            readingProgressRepository.updateLastChapter(moduleId, chapter.id)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            moduleRepository.toggleFavorite(moduleId)
        }
    }

    // 工具切换
    fun setActiveTool(tool: AnnotationTool) {
        _uiState.update {
            it.copy(activeTool = if (it.activeTool == tool) AnnotationTool.NONE else tool)
        }
    }

    fun setSelectedColor(color: Long) {
        _uiState.update { it.copy(selectedColor = color) }
    }

    fun setEraserMode(mode: EraserMode) {
        _uiState.update { it.copy(eraserMode = mode) }
    }

    // 书签操作
    fun addBookmark(chapterId: String, selectedText: String = "", note: String = "") {
        val chapter = _uiState.value.selectedChapter ?: return
        viewModelScope.launch {
            bookmarkRepository.addBookmark(
                moduleId = moduleId,
                chapterId = chapterId,
                chapterTitle = chapter.title,
                selectedText = selectedText,
                note = note,
                color = _uiState.value.selectedColor
            )
        }
    }

    fun addNodeBookmark(nodeId: String, selectedText: String = "", note: String = "") {
        viewModelScope.launch {
            bookmarkRepository.addNodeBookmark(
                moduleId = moduleId,
                nodeId = nodeId,
                selectedText = selectedText,
                note = note,
                color = _uiState.value.selectedColor
            )
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            bookmarkRepository.deleteBookmark(bookmark)
        }
    }

    fun deleteBookmarkById(bookmarkId: String) {
        viewModelScope.launch {
            bookmarkRepository.deleteBookmarkById(bookmarkId)
        }
    }

    fun navigateToBookmark(bookmark: BookmarkEntity) {
        val chapter = findChapterById(_uiState.value.chapters, bookmark.chapterId)
        if (chapter != null) {
            selectChapter(chapter)
        }
    }

    // 高亮操作
    fun addHighlight(chapterId: String, startIndex: Int, endIndex: Int, selectedText: String) {
        if (moduleId.isBlank() || selectedText.isBlank()) return
        viewModelScope.launch {
            highlightRepository.addHighlight(
                moduleId = moduleId,
                chapterId = chapterId,
                startIndex = startIndex,
                endIndex = endIndex,
                selectedText = selectedText,
                color = _uiState.value.selectedColor
            )
        }
    }

    fun addNodeHighlight(nodeId: String, startIndex: Int, endIndex: Int, selectedText: String, color: Long = _uiState.value.selectedColor) {
        if (moduleId.isBlank() || selectedText.isBlank()) return
        viewModelScope.launch {
            highlightRepository.addNodeHighlight(
                moduleId = moduleId,
                nodeId = nodeId,
                startIndex = startIndex,
                endIndex = endIndex,
                selectedText = selectedText,
                color = _uiState.value.selectedColor
            )
        }
    }

    fun deleteHighlight(highlight: HighlightEntity) {
        viewModelScope.launch { highlightRepository.deleteHighlight(highlight) }
    }

    fun deleteHighlightById(highlightId: String) {
        viewModelScope.launch { highlightRepository.deleteHighlightById(highlightId) }
    }

    // 批注操作
    fun addAnnotation(
        chapterId: String,
        startIndex: Int,
        endIndex: Int,
        selectedText: String,
        note: String
    ) {
        if (moduleId.isBlank() || selectedText.isBlank()) return
        viewModelScope.launch {
            annotationRepository.addAnnotation(
                moduleId = moduleId,
                chapterId = chapterId,
                startIndex = startIndex,
                endIndex = endIndex,
                selectedText = selectedText,
                note = note,
                color = _uiState.value.selectedColor
            )
        }
    }

    fun addNodeAnnotation(
        nodeId: String,
        startIndex: Int,
        endIndex: Int,
        selectedText: String,
        note: String
    ) {
        if (moduleId.isBlank() || selectedText.isBlank()) return
        viewModelScope.launch {
            annotationRepository.addNodeAnnotation(
                moduleId = moduleId,
                nodeId = nodeId,
                startIndex = startIndex,
                endIndex = endIndex,
                selectedText = selectedText,
                note = note,
                color = _uiState.value.selectedColor
            )
        }
    }

    fun updateAnnotation(annotation: AnnotationEntity) {
        viewModelScope.launch {
            annotationRepository.updateAnnotation(annotation.copy(updateTime = System.currentTimeMillis()))
        }
    }

    fun deleteAnnotation(annotation: AnnotationEntity) {
        viewModelScope.launch { annotationRepository.deleteAnnotation(annotation) }
    }

    fun deleteAnnotationById(annotationId: String) {
        viewModelScope.launch { annotationRepository.deleteAnnotationById(annotationId) }
    }

    // 橡皮擦操作
    fun eraseByHighlight(highlight: HighlightEntity) {
        viewModelScope.launch { highlightRepository.deleteHighlight(highlight) }
    }

    fun eraseByAnnotation(annotation: AnnotationEntity) {
        viewModelScope.launch { annotationRepository.deleteAnnotation(annotation) }
    }

    // 擦除选区内的高亮和批注
    fun eraseOverlapping(nodeId: String, startIndex: Int, endIndex: Int) {
        viewModelScope.launch {
            _uiState.value.highlights
                .filter { it.nodeId == nodeId && it.startIndex < endIndex && it.endIndex > startIndex }
                .forEach { highlightRepository.deleteHighlight(it) }
            _uiState.value.annotations
                .filter { it.nodeId == nodeId && it.startIndex < endIndex && it.endIndex > startIndex }
                .forEach { annotationRepository.deleteAnnotation(it) }
        }
    }

    // 批量清除
    fun clearHighlightsByChapter(chapterId: String) {
        viewModelScope.launch { highlightRepository.deleteHighlightsByChapter(moduleId, chapterId) }
    }

    fun clearAnnotationsByChapter(chapterId: String) {
        viewModelScope.launch { annotationRepository.deleteAnnotationsByChapter(moduleId, chapterId) }
    }

    fun clearAllHighlights() {
        viewModelScope.launch { highlightRepository.deleteHighlightsByModule(moduleId) }
    }

    fun clearAllAnnotations() {
        viewModelScope.launch { annotationRepository.deleteAnnotationsByModule(moduleId) }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch { bookmarkRepository.deleteBookmarksByModule(moduleId) }
    }

    fun clearAll() {
        viewModelScope.launch {
            highlightRepository.deleteHighlightsByModule(moduleId)
            annotationRepository.deleteAnnotationsByModule(moduleId)
            bookmarkRepository.deleteBookmarksByModule(moduleId)
        }
    }

    fun updateModule(module: ModuleEntity) {
        viewModelScope.launch { moduleRepository.updateModule(module) }
    }

    fun deleteModule() {
        viewModelScope.launch {
            moduleRepository.deleteModule(moduleId)
        }
    }

    fun updateChapterContent(chapterId: String, newContent: String) {
        val currentModule = _uiState.value.module ?: return
        val updatedChapters = ModuleContentParser.updateChapterContent(
            _uiState.value.chapters, chapterId, newContent
        )
        val updatedJson = ModuleContentParser.chaptersToJson(updatedChapters)
        viewModelScope.launch {
            moduleRepository.updateModule(currentModule.copy(contentJson = updatedJson))
        }
    }

    private fun findFirstLeaf(chapters: List<Chapter>): Chapter? {
        for (chapter in chapters) {
            if (chapter.children.isEmpty()) return chapter
            findFirstLeaf(chapter.children)?.let { return it }
        }
        return chapters.firstOrNull()
    }

    private fun findChapterById(chapters: List<Chapter>, id: String): Chapter? {
        for (chapter in chapters) {
            if (chapter.id == id) return chapter
            findChapterById(chapter.children, id)?.let { return it }
        }
        return null
    }
}

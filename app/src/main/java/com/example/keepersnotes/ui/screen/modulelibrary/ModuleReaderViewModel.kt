package com.example.keepersnotes.ui.screen.modulelibrary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.AnnotationEntity
import com.example.keepersnotes.data.local.entity.BookmarkEntity
import com.example.keepersnotes.data.local.entity.HighlightEntity
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.local.entity.ReadingProgressEntity
import com.example.keepersnotes.data.repository.AnnotationRepository
import com.example.keepersnotes.data.repository.BookmarkRepository
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

data class ModuleReaderUiState(
    val module: ModuleEntity? = null,
    val chapters: List<Chapter> = emptyList(),
    val selectedChapter: Chapter? = null,
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
    private val highlightRepository: HighlightRepository,
    private val annotationRepository: AnnotationRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val moduleId: String = savedStateHandle.get<String>("moduleId") ?: ""
    private val initialChapterId: String? = savedStateHandle.get<String>("chapterId")

    private val _uiState = MutableStateFlow(ModuleReaderUiState(isLoading = true))
    val uiState: StateFlow<ModuleReaderUiState> = _uiState.asStateFlow()

    // 阅读时间追踪
    private var readStartTime: Long = 0

    init {
        if (moduleId.isNotBlank()) {
            moduleRepository.getModuleById(moduleId)
                .onEach { module ->
                    if (module != null) {
                        val chapters = ModuleContentParser.jsonToChapters(module.contentJson)
                        val firstChapter = findFirstLeaf(chapters)

                        // 加载阅读进度
                        val progress = readingProgressRepository.getProgressByModule(moduleId).firstOrNull()
                        val lastChapter = progress?.lastChapterId?.let { id ->
                            findChapterById(chapters, id)
                        }
                        // 优先使用导航传入的章节，其次上次阅读章节，最后首章
                        val targetChapter = initialChapterId?.let { findChapterById(chapters, it) }
                            ?: lastChapter ?: firstChapter

                        _uiState.update {
                            it.copy(
                                module = module,
                                chapters = chapters,
                                selectedChapter = it.selectedChapter ?: targetChapter,
                                readingProgress = progress,
                                isLoading = false
                            )
                        }

                        // 初始化阅读进度
                        if (progress == null && firstChapter != null) {
                            readingProgressRepository.initializeProgress(moduleId, firstChapter.id)
                        }

                        // 记录阅读开始时间
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
        // 保存阅读时间
        if (readStartTime > 0) {
            val readMinutes = ((System.currentTimeMillis() - readStartTime) / 60000).toInt()
            if (readMinutes > 0) {
                viewModelScope.launch {
                    readingProgressRepository.addReadTime(moduleId, readMinutes)
                }
            }
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

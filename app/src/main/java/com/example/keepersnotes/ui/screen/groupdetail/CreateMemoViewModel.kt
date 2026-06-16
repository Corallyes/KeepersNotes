package com.example.keepersnotes.ui.screen.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.repository.KpMemoRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.util.Chapter
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.ModuleContentParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateMemoUiState(
    val title: String = "",
    val content: String = "",
    val type: String = Constants.MEMO_TYPE_GENERAL,
    val isHidden: Boolean = false,
    val priority: Int = 0,
    val titleError: String? = null,
    val isSubmitting: Boolean = false,
    val createdMemoId: String? = null,
    val modules: List<ModuleEntity> = emptyList(),
    val selectedModuleId: String? = null,
    val chapters: List<Chapter> = emptyList(),
    val selectedChapterId: String? = null,
    val selectedChapterTitle: String = ""
)

@HiltViewModel
class CreateMemoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val kpMemoRepository: KpMemoRepository,
    private val moduleRepository: ModuleRepository
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(CreateMemoUiState())
    val uiState: StateFlow<CreateMemoUiState> = _uiState.asStateFlow()

    init {
        // 加载可用的模组
        moduleRepository.getAllModules()
            .onEach { modules ->
                _uiState.update { it.copy(modules = modules) }
            }
            .launchIn(viewModelScope)
    }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    fun updateType(type: String) {
        _uiState.update { it.copy(type = type) }
    }

    fun updateIsHidden(isHidden: Boolean) {
        _uiState.update { it.copy(isHidden = isHidden) }
    }

    fun updatePriority(priority: Int) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun selectModule(moduleId: String?) {
        _uiState.update {
            it.copy(
                selectedModuleId = moduleId,
                chapters = emptyList(),
                selectedChapterId = null,
                selectedChapterTitle = ""
            )
        }
        if (moduleId != null) {
            viewModelScope.launch {
                moduleRepository.getModuleById(moduleId).firstOrNull()?.let { module ->
                    val chapters = ModuleContentParser.jsonToChapters(module.contentJson)
                    _uiState.update { it.copy(chapters = chapters) }
                }
            }
        }
    }

    fun selectChapter(chapterId: String, chapterTitle: String) {
        _uiState.update {
            it.copy(
                selectedChapterId = chapterId,
                selectedChapterTitle = chapterTitle
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.update { it.copy(titleError = "请输入标题或内容") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val memoId = kpMemoRepository.createMemo(
                groupId = groupId,
                type = state.type,
                title = state.title.trim(),
                content = state.content.trim(),
                isHidden = state.isHidden
            )
            // Update priority and module/chapter association
            kpMemoRepository.getMemoById(memoId).firstOrNull()?.let { memo ->
                kpMemoRepository.updateMemo(
                    memo.copy(
                        priority = state.priority,
                        moduleId = state.selectedModuleId,
                        chapterId = state.selectedChapterId,
                        chapterTitle = state.selectedChapterTitle
                    )
                )
            }
            _uiState.update { it.copy(isSubmitting = false, createdMemoId = memoId) }
        }
    }
}

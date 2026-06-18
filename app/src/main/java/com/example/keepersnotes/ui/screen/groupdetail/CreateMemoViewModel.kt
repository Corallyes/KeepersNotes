package com.example.keepersnotes.ui.screen.groupdetail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import com.example.keepersnotes.data.repository.CalendarEventRepository
import com.example.keepersnotes.data.repository.GroupRepository
import com.example.keepersnotes.data.repository.KpMemoRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.util.Chapter
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.LocalizedStrings
import com.example.keepersnotes.util.ModuleContentParser
import com.example.keepersnotes.notification.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateMemoUiState(
    val title: String = "",
    val content: String = "",
    val type: String = Constants.MEMO_TYPE_TODO,
    val priority: Int = 0,
    val titleError: String? = null,
    val isSubmitting: Boolean = false,
    val createdMemoId: String? = null,
    val modules: List<ModuleEntity> = emptyList(),
    val selectedModuleId: String? = null,
    val chapters: List<Chapter> = emptyList(),
    val selectedChapterId: String? = null,
    val selectedChapterTitle: String = "",
    // 提醒通知相关
    val isNotificationEnabled: Boolean = false,
    val notificationDate: Long? = null, // 选中的日期（零点时间戳）
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0
)

@HiltViewModel
class CreateMemoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val kpMemoRepository: KpMemoRepository,
    private val moduleRepository: ModuleRepository,
    private val groupRepository: GroupRepository,
    private val calendarEventRepository: CalendarEventRepository
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(CreateMemoUiState())
    val uiState: StateFlow<CreateMemoUiState> = _uiState.asStateFlow()

    init {
        // 从团获取关联的模组
        if (groupId.isNotBlank()) {
            viewModelScope.launch {
                groupRepository.getGroupById(groupId)
                    .firstOrNull()?.let { group ->
                        group.moduleId?.let { moduleId ->
                            selectModule(moduleId)
                        }
                    }
            }
        }
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

    fun updateNotificationEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isNotificationEnabled = enabled) }
    }

    fun updateNotificationDate(date: Long?) {
        _uiState.update { it.copy(notificationDate = date) }
    }

    fun updateNotificationTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(notificationHour = hour, notificationMinute = minute) }
    }

    fun submit(context: Context? = null) {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) {
            _uiState.update { it.copy(titleError = LocalizedStrings.memoTitleOrContentRequired) }
            return
        }
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            // 计算通知时间戳
            val notificationTime: Long? = if (state.type == Constants.MEMO_TYPE_REMINDER && state.isNotificationEnabled && state.notificationDate != null) {
                state.notificationDate!! + state.notificationHour * 3600_000L + state.notificationMinute * 60_000L
            } else null

            val memoId = kpMemoRepository.createMemo(
                groupId = groupId,
                type = state.type,
                title = state.title.trim(),
                content = state.content.trim(),
                isHidden = false
            )
            // Update priority, module/chapter association, and notification
            kpMemoRepository.getMemoById(memoId).firstOrNull()?.let { memo ->
                val notificationId = memoId.hashCode() and 0x7FFFFFFF
                kpMemoRepository.updateMemo(
                    memo.copy(
                        priority = state.priority,
                        chapterId = state.selectedChapterId,
                        chapterTitle = state.selectedChapterTitle,
                        isNotificationEnabled = state.type == Constants.MEMO_TYPE_REMINDER && state.isNotificationEnabled,
                        notificationTime = notificationTime,
                        notificationId = notificationId
                    )
                )
                // 调度通知 & 创建日历日程
                if (state.type == Constants.MEMO_TYPE_REMINDER && state.isNotificationEnabled && notificationTime != null) {
                    if (context != null && notificationTime > System.currentTimeMillis()) {
                        NotificationHelper.scheduleMemoNotification(
                            context = context,
                            notificationId = notificationId.toLong(),
                            title = state.title.trim().ifBlank { LocalizedStrings.memoReminderTitle },
                            content = state.content.trim().take(100),
                            triggerTime = notificationTime
                        )
                    }
                    // 同步到日历
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = notificationTime }
                    val dateOnly = java.util.Calendar.getInstance().apply {
                        set(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH), cal.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0)
                        set(java.util.Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    val timeStr = "${cal.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${cal.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')}"
                    calendarEventRepository.create(
                        groupId = groupId,
                        title = "⏰ ${state.title.trim().ifBlank { LocalizedStrings.memoReminderTitle }}",
                        date = dateOnly,
                        time = timeStr,
                        type = "memo_reminder"
                    )
                }
            }
            _uiState.update { it.copy(isSubmitting = false, createdMemoId = memoId) }
        }
    }
}

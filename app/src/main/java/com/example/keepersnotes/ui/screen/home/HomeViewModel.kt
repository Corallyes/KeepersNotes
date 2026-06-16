package com.example.keepersnotes.ui.screen.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import com.example.keepersnotes.data.local.entity.GroupEntity
import com.example.keepersnotes.data.repository.CalendarEventRepository
import com.example.keepersnotes.data.repository.GroupRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.data.repository.PlayerCharacterRepository
import com.example.keepersnotes.data.repository.SessionRepository
import com.example.keepersnotes.notification.ReminderScheduler
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.FileReaderUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val activeGroupCount: Int = 0,
    val totalPcCount: Int = 0,
    val weeklySessionCount: Int = 0,
    val upcomingGroups: List<GroupEntity> = emptyList(),
    val activeGroups: List<GroupEntity> = emptyList(),
    val calendarEvents: List<CalendarEventEntity> = emptyList(),
    val selectedDateEvents: List<CalendarEventEntity> = emptyList(),
    val selectedDate: Long? = null,
    val importResult: ImportResult? = null
)

sealed class ImportResult {
    data class Success(val moduleTitle: String) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val groupRepository: GroupRepository,
    private val pcRepository: PlayerCharacterRepository,
    private val moduleRepository: ModuleRepository,
    private val sessionRepository: SessionRepository,
    private val calendarEventRepository: CalendarEventRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            groupRepository.getActiveGroups(),
            pcRepository.getTotalPcCount(),
            groupRepository.getActiveGroupCount()
        ) { groups, totalPc, activeCount ->
            val now = System.currentTimeMillis()
            val upcoming = groups
                .filter { it.nextPlayTime != null && it.nextPlayTime > now }
                .sortedBy { it.nextPlayTime }
            _uiState.value.copy(
                activeGroupCount = activeCount,
                totalPcCount = totalPc,
                upcomingGroups = upcoming,
                activeGroups = groups
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)

        sessionRepository.getWeeklySessionCount()
            .onEach { count ->
                _uiState.update { it.copy(weeklySessionCount = count) }
            }
            .launchIn(viewModelScope)

        // 加载日历事件（当月前后3个月）
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.MONTH, -3)
        val startDate = cal.timeInMillis
        cal.add(java.util.Calendar.MONTH, 6)
        val endDate = cal.timeInMillis

        calendarEventRepository.getEventsBetween(startDate, endDate)
            .onEach { events ->
                _uiState.update { it.copy(calendarEvents = events) }
                // Schedule reminders for upcoming events
                val context = getApplication<Application>()
                ReminderScheduler.scheduleReminders(context, events)
            }
            .launchIn(viewModelScope)
    }

    fun selectDate(date: Long?) {
        _uiState.update { it.copy(selectedDate = date) }
        if (date != null) {
            calendarEventRepository.getEventsByDate(date)
                .onEach { events ->
                    _uiState.update { it.copy(selectedDateEvents = events) }
                }
                .launchIn(viewModelScope)
        } else {
            _uiState.update { it.copy(selectedDateEvents = emptyList()) }
        }
    }

    fun importModuleFromUri(uri: Uri, title: String, author: String, system: String) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val contentResult = FileReaderUtil.readFileContent(context, uri)
            contentResult.fold(
                onSuccess = { content ->
                    moduleRepository.importModule(
                        title = title.ifBlank { "未命名模组" },
                        author = author,
                        system = system,
                        content = content
                    )
                    _uiState.update {
                        it.copy(importResult = ImportResult.Success(title.ifBlank { "未命名模组" }))
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(importResult = ImportResult.Error(error.message ?: "导入失败"))
                    }
                }
            )
        }
    }

    fun clearImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }
}

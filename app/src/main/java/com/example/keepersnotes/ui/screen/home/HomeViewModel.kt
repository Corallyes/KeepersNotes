package com.example.keepersnotes.ui.screen.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.importer.ZipImportManager
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import com.example.keepersnotes.data.local.entity.GroupEntity
import com.example.keepersnotes.data.repository.CalendarEventRepository
import com.example.keepersnotes.data.repository.GroupRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.data.repository.SessionRepository
import com.example.keepersnotes.notification.ReminderScheduler
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.FileReaderUtil
import com.example.keepersnotes.util.LocalizedStrings
import com.example.keepersnotes.util.ModuleContentParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HomeUiState(
    val activeGroupCount: Int = 0,
    val completedGroupCount: Int = 0,
    val weeklySessionCount: Int = 0,
    val todayEvents: List<CalendarEventEntity> = emptyList(),
    val activeGroups: List<GroupEntity> = emptyList(),
    val calendarEvents: List<CalendarEventEntity> = emptyList(),
    val selectedDateEvents: List<CalendarEventEntity> = emptyList(),
    val selectedDate: Long? = null,
    val importResult: ImportResult? = null,
    val isImporting: Boolean = false
)

sealed class ImportResult {
    data class Success(val moduleTitle: String) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val groupRepository: GroupRepository,
    private val moduleRepository: ModuleRepository,
    private val sessionRepository: SessionRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val zipImportManager: ZipImportManager
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow<Long?>(null)

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            groupRepository.getActiveGroups(),
            groupRepository.getActiveGroupCount(),
            groupRepository.getCompletedGroupCount()
        ) { groups, activeCount, completedCount ->
            _uiState.value.copy(
                activeGroupCount = activeCount,
                completedGroupCount = completedCount,
                activeGroups = groups
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)

        // 今日日程
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + 86_400_000 - 1
        calendarEventRepository.getEventsBetween(todayStart, todayEnd)
            .onEach { events ->
                _uiState.update { it.copy(todayEvents = events) }
            }
            .launchIn(viewModelScope)

        // 选中日期的日程
        _selectedDate.flatMapLatest { date ->
            if (date != null) {
                calendarEventRepository.getEventsByDate(date)
            } else {
                flowOf(emptyList())
            }
        }.onEach { events ->
            _uiState.update { it.copy(selectedDateEvents = events) }
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
        _selectedDate.value = date
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun updateEvent(event: CalendarEventEntity) {
        viewModelScope.launch { calendarEventRepository.update(event) }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch { calendarEventRepository.deleteById(eventId) }
    }

    fun importModuleFromUri(uri: Uri, title: String, author: String, system: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                val context = getApplication<Application>()
                withContext(Dispatchers.IO) {
                    val fileName = getFileName(context, uri)
                    val ext = fileName.substringAfterLast('.', "").lowercase()

                    // Use structured parser for TXT files
                    if (ext == "txt") {
                        try {
                            val nodes = FileReaderUtil.readTxtStructured(context, uri)
                            if (nodes.isSuccess) {
                                val txtNodes = nodes.getOrNull() ?: emptyList()
                                // Convert to markdown for compatibility
                                val rawContent = txtNodes.joinToString("\n\n") { node ->
                                    when (node.type) {
                                        "heading" -> "#".repeat(node.level) + " " + node.content
                                        "paragraph" -> node.content
                                        "quote" -> "> " + node.content
                                        "list_item" -> "- " + node.content
                                        else -> node.content
                                    }
                                }
                                val cleanTitle = title.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F\\uFFFD]"), "").trim()
                                    .ifBlank { LocalizedStrings.unnamedModule }
                                val chapters = ModuleContentParser.parseTextToChapters(rawContent)
                                val contentJson = ModuleContentParser.chaptersToJson(chapters)
                                val moduleId = moduleRepository.importModule(
                                    title = cleanTitle,
                                    author = author,
                                    system = system,
                                    content = contentJson
                                )
                                _uiState.update {
                                    it.copy(isImporting = false, importResult = ImportResult.Success(title.ifBlank { LocalizedStrings.unnamedModule }))
                                }
                                return@withContext
                            }
                        } catch (e: Exception) {
                            // Fall through to default handling
                        }
                    }

                    // Default handling for other file types or fallback
                    val contentResult = FileReaderUtil.readFileContent(context, uri)
                    contentResult.fold(
                        onSuccess = { rawContent ->
                            val cleanTitle = title.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F\\uFFFD]"), "").trim()
                                .ifBlank { LocalizedStrings.unnamedModule }
                            val chapters = ModuleContentParser.parseTextToChapters(rawContent)
                            val contentJson = ModuleContentParser.chaptersToJson(chapters)
                            val moduleId = moduleRepository.importModule(
                                title = cleanTitle,
                                author = author,
                                system = system,
                                content = contentJson
                            )
                            _uiState.update {
                                it.copy(isImporting = false, importResult = ImportResult.Success(title.ifBlank { LocalizedStrings.unnamedModule }))
                            }
                        },
                        onFailure = { error ->
                            _uiState.update {
                                it.copy(isImporting = false, importResult = ImportResult.Error(error.message ?: LocalizedStrings.homeImportFail))
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isImporting = false, importResult = ImportResult.Error(e.message ?: LocalizedStrings.homeImportFail))
                }
            }
        }
    }

    fun importZipFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            try {
                val context = getApplication<Application>()
                withContext(Dispatchers.IO) {
                    val collectionId = java.util.UUID.randomUUID().toString()
                    val result = zipImportManager.importArchive(context, uri, collectionId)
                    val chapters = ModuleContentParser.parseTextToChapters(result.combinedContent)
                    val contentJson = ModuleContentParser.chaptersToJson(chapters)
                    val moduleId = moduleRepository.importModule(
                        title = result.collectionTitle,
                        author = "",
                        system = "",
                        content = contentJson,
                        isCollection = false,
                        moduleId = collectionId
                    )
                    zipImportManager.insertImportResult(result)
                    _uiState.update {
                        it.copy(isImporting = false, importResult = ImportResult.Success(result.collectionTitle))
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isImporting = false, importResult = ImportResult.Error(e.message ?: LocalizedStrings.homeImportFail))
                }
            }
        }
    }

    fun clearImportResult() {
        _uiState.update { it.copy(importResult = null) }
    }

    private fun getFileName(context: android.content.Context, uri: Uri): String {
        var fileName = ""
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex) ?: ""
            }
        }
        return fileName
    }

}

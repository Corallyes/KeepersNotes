package com.example.keepersnotes.ui.screen.grouplist

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.repository.CalendarEventRepository
import com.example.keepersnotes.data.repository.GroupRepository
import com.example.keepersnotes.data.repository.ModuleDefaultPcRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.data.repository.PlayerCharacterRepository
import com.example.keepersnotes.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateGroupUiState(
    val groupName: String = "",
    val moduleName: String = "",
    val system: String = Constants.SYSTEM_COC7,
    val selectedModuleId: String? = null,
    val coverImageUri: String? = null,
    val modules: List<ModuleEntity> = emptyList(),
    val groupNameError: String? = null,
    val isSubmitting: Boolean = false,
    val createdGroupId: String? = null,
    val gameFormat: String = "",
    val scale: String = "",
    val startTime: Long? = null,
    val expectedEndTime: Long? = null,
    val defaultSessionTime: String = ""
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    application: Application,
    private val groupRepository: GroupRepository,
    private val moduleRepository: ModuleRepository,
    private val defaultPcRepository: ModuleDefaultPcRepository,
    private val pcRepository: PlayerCharacterRepository,
    private val calendarEventRepository: CalendarEventRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    init {
        moduleRepository.getAllModules()
            .onEach { modules -> _uiState.update { it.copy(modules = modules) } }
            .launchIn(viewModelScope)
    }

    fun updateGroupName(name: String) {
        _uiState.update { it.copy(groupName = name, groupNameError = null) }
    }

    fun updateModuleName(name: String) {
        _uiState.update { it.copy(moduleName = name, selectedModuleId = null) }
    }

    fun updateSystem(system: String) {
        _uiState.update { it.copy(system = system) }
    }

    fun selectModule(module: ModuleEntity) {
        _uiState.update {
            it.copy(
                moduleName = module.title,
                selectedModuleId = module.moduleId,
                system = module.system.ifBlank { it.system }
            )
        }
    }

    fun updateCoverImage(uri: Uri?) {
        _uiState.update { it.copy(coverImageUri = uri?.toString()) }
    }

    fun updateGameFormat(format: String) {
        _uiState.update { it.copy(gameFormat = format) }
    }

    fun updateScale(scale: String) {
        _uiState.update { it.copy(scale = scale) }
    }

    fun updateStartTime(time: Long?) {
        _uiState.update { it.copy(startTime = time) }
    }

    fun updateExpectedEndTime(time: Long?) {
        _uiState.update { it.copy(expectedEndTime = time) }
    }

    fun updateDefaultSessionTime(time: String) {
        _uiState.update { it.copy(defaultSessionTime = time) }
    }

    private suspend fun createCalendarEvents(groupId: String, state: CreateGroupUiState) {
        val events = mutableListOf<CalendarEventEntity>()
        val time = state.defaultSessionTime.ifBlank { null }

        state.startTime?.let { date ->
            events.add(
                CalendarEventEntity(
                    eventId = java.util.UUID.randomUUID().toString(),
                    groupId = groupId,
                    title = "${state.groupName.trim()} 开团日",
                    date = date,
                    time = time,
                    type = "session_start"
                )
            )
        }

        state.expectedEndTime?.let { date ->
            events.add(
                CalendarEventEntity(
                    eventId = java.util.UUID.randomUUID().toString(),
                    groupId = groupId,
                    title = "${state.groupName.trim()} 预计结束",
                    date = date,
                    time = time,
                    type = "session_end"
                )
            )
        }

        if (events.isNotEmpty()) {
            calendarEventRepository.insertAll(events)
        }
    }

    private suspend fun inheritDefaultPcs(moduleId: String, groupId: String, system: String) {
        val defaultPcs = defaultPcRepository.getByModuleId(moduleId).first()
        defaultPcs.forEach { defaultPc ->
            pcRepository.createPc(
                groupId = groupId,
                playerName = defaultPc.playerName,
                characterName = defaultPc.name,
                system = system.ifBlank { defaultPc.system }
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.groupName.isBlank()) {
            _uiState.update { it.copy(groupNameError = "请输入团名称") }
            return
        }
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val groupId = groupRepository.createGroup(
                groupName = state.groupName.trim(),
                moduleName = state.moduleName.trim(),
                system = state.system,
                moduleId = state.selectedModuleId,
                coverImageUri = state.coverImageUri,
                gameFormat = state.gameFormat,
                scale = state.scale.trim(),
                startTime = state.startTime,
                expectedEndTime = state.expectedEndTime,
                defaultSessionTime = state.defaultSessionTime.trim()
            )
            // 继承模组的推荐PC
            state.selectedModuleId?.let { moduleId ->
                inheritDefaultPcs(moduleId, groupId, state.system)
            }
            // 自动创建日历事件
            createCalendarEvents(groupId, state)
            _uiState.update { it.copy(isSubmitting = false, createdGroupId = groupId) }
        }
    }
}

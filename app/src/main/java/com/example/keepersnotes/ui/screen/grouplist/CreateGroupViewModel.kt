package com.example.keepersnotes.ui.screen.grouplist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.repository.CalendarEventRepository
import com.example.keepersnotes.data.repository.GroupRelationshipRepository
import com.example.keepersnotes.data.repository.GroupRepository
import com.example.keepersnotes.data.repository.KpMemoRepository
import com.example.keepersnotes.data.repository.ModuleClueRepository
import com.example.keepersnotes.data.repository.ModuleDefaultNpcRepository
import com.example.keepersnotes.data.repository.ModuleDefaultPcRepository
import com.example.keepersnotes.data.repository.ModuleRelationshipRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.data.repository.NpcRepository
import com.example.keepersnotes.data.repository.PlayerCharacterRepository
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.LocalizedStrings
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
    val modules: List<ModuleEntity> = emptyList(),
    val groupNameError: String? = null,
    val isSubmitting: Boolean = false,
    val createdGroupId: String? = null,
    val gameFormat: String = "",
    val scale: String = "",
    val startTime: Long? = null,
    val expectedEndTime: Long? = null,
    val defaultSessionTime: String = "14:00"
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    application: Application,
    private val groupRepository: GroupRepository,
    private val moduleRepository: ModuleRepository,
    private val defaultPcRepository: ModuleDefaultPcRepository,
    private val defaultNpcRepository: ModuleDefaultNpcRepository,
    private val moduleRelationshipRepository: ModuleRelationshipRepository,
    private val pcRepository: PlayerCharacterRepository,
    private val npcRepository: NpcRepository,
    private val groupRelationshipRepository: GroupRelationshipRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val moduleClueRepository: ModuleClueRepository,
    private val kpMemoRepository: KpMemoRepository
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
        val name = state.groupName.trim()
        val start = state.startTime
        val end = state.expectedEndTime

        if (start != null && end != null && start <= end) {
            // 开始到结束之间的每一天都创建日程
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = start }
            while (cal.timeInMillis <= end) {
                val date = cal.timeInMillis
                val isStart = date == start
                val isEnd = date == end
                events.add(
                    CalendarEventEntity(
                        eventId = java.util.UUID.randomUUID().toString(),
                        groupId = groupId,
                        title = when {
                            isStart && isEnd -> LocalizedStrings.calendarSessionStart(name)
                            isStart -> LocalizedStrings.calendarSessionStartDate(name)
                            isEnd -> LocalizedStrings.calendarSessionEndDate(name)
                            else -> LocalizedStrings.calendarSessionInProgress(name)
                        },
                        date = date,
                        time = time,
                        type = when {
                            isStart -> "session_start"
                            isEnd -> "session_end"
                            else -> "session"
                        }
                    )
                )
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        } else {
            // 只有一个时间的情况
            start?.let { date ->
                events.add(
                    CalendarEventEntity(
                        eventId = java.util.UUID.randomUUID().toString(),
                        groupId = groupId,
                        title = LocalizedStrings.calendarSessionStartDate(name),
                        date = date,
                        time = time,
                        type = "session_start"
                    )
                )
            }
            end?.let { date ->
                events.add(
                    CalendarEventEntity(
                        eventId = java.util.UUID.randomUUID().toString(),
                        groupId = groupId,
                        title = LocalizedStrings.calendarSessionEndDate(name),
                        date = date,
                        time = time,
                        type = "session_end"
                    )
                )
            }
        }

        if (events.isNotEmpty()) {
            calendarEventRepository.insertAll(events)
        }
    }

    private suspend fun inheritDefaultPcs(moduleId: String, groupId: String, system: String): Map<String, String> {
        val defaultPcs = defaultPcRepository.getByModuleId(moduleId).first()
        Log.d("CreateGroup", "inheritDefaultPcs: found ${defaultPcs.size} default PCs for module $moduleId")
        val idMapping = mutableMapOf<String, String>()
        defaultPcs.forEach { defaultPc ->
            val pcId = pcRepository.createPc(
                groupId = groupId,
                playerName = defaultPc.playerName,
                characterName = defaultPc.name,
                system = system.ifBlank { defaultPc.system }
            )
            idMapping[defaultPc.id] = pcId
            Log.d("CreateGroup", "  PC: ${defaultPc.id} -> $pcId (${defaultPc.name})")
        }
        return idMapping
    }

    private suspend fun inheritDefaultNpcs(moduleId: String, groupId: String): Map<String, String> {
        val defaultNpcs = defaultNpcRepository.getByModuleId(moduleId).first()
        Log.d("CreateGroup", "inheritDefaultNpcs: found ${defaultNpcs.size} default NPCs for module $moduleId")
        val idMapping = mutableMapOf<String, String>()
        defaultNpcs.forEach { defaultNpc ->
            val npcId = npcRepository.createNpc(
                groupId = groupId,
                name = defaultNpc.name,
                alias = defaultNpc.alias,
                occupation = defaultNpc.occupation,
                gender = defaultNpc.gender
            )
            npcRepository.getNpcByIdOnce(npcId)?.let { npc ->
                npcRepository.updateNpc(
                    npc.copy(
                        description = defaultNpc.description,
                        truePurpose = defaultNpc.truePurpose,
                        relationshipToPc = defaultNpc.relationshipToPc,
                        status = defaultNpc.status
                    )
                )
            }
            idMapping[defaultNpc.id] = npcId
            Log.d("CreateGroup", "  NPC: ${defaultNpc.id} -> $npcId (${defaultNpc.name})")
        }
        return idMapping
    }

    private suspend fun inheritRelationships(
        moduleId: String,
        groupId: String,
        pcIdMapping: Map<String, String>,
        npcIdMapping: Map<String, String>
    ) {
        val relationships = moduleRelationshipRepository.getByModuleId(moduleId).first()
        Log.d("CreateGroup", "inheritRelationships: found ${relationships.size} relationships for module $moduleId")
        Log.d("CreateGroup", "  pcIdMapping: $pcIdMapping")
        Log.d("CreateGroup", "  npcIdMapping: $npcIdMapping")
        relationships.forEach { rel ->
            val sourceGroupId = if (rel.sourceType == "pc") pcIdMapping[rel.sourceId] else npcIdMapping[rel.sourceId]
            val targetGroupId = if (rel.targetType == "pc") pcIdMapping[rel.targetId] else npcIdMapping[rel.targetId]
            Log.d("CreateGroup", "  rel: ${rel.sourceId}(${rel.sourceType}) -> ${rel.targetId}(${rel.targetType}) [${rel.relationType}]")
            Log.d("CreateGroup", "    mapped: $sourceGroupId -> $targetGroupId")
            if (sourceGroupId != null && targetGroupId != null) {
                groupRelationshipRepository.create(
                    groupId = groupId,
                    sourceId = sourceGroupId,
                    sourceType = rel.sourceType,
                    targetId = targetGroupId,
                    targetType = rel.targetType,
                    relationType = rel.relationType,
                    description = rel.description
                )
                Log.d("CreateGroup", "    -> CREATED")
            } else {
                Log.w("CreateGroup", "    -> SKIPPED (null mapping)")
            }
        }
    }

    private suspend fun inheritClues(moduleId: String, groupId: String) {
        Log.d("CreateGroup", "inheritClues: moduleId=$moduleId, groupId=$groupId")
        val clues = moduleClueRepository.getByModuleId(moduleId).first()
        Log.d("CreateGroup", "inheritClues: found ${clues.size} clues for module $moduleId")
        clues.forEach { clue ->
            val content = buildString {
                if (clue.description.isNotBlank()) append(clue.description)
                if (clue.source.isNotBlank()) {
                    if (isNotEmpty()) append("\n\n")
                    append("${LocalizedStrings.clueSource}：${clue.source}")
                }
            }
            Log.d("CreateGroup", "  Creating memo: title='${clue.name}', type=clue, content=${content.length} chars, isHidden=${clue.isHidden}")
            val memoId = kpMemoRepository.createMemo(
                groupId = groupId,
                type = Constants.MEMO_TYPE_CLUE,
                title = clue.name,
                content = content,
                isHidden = clue.isHidden
            )
            Log.d("CreateGroup", "  Created memo: id=$memoId")
        }
        Log.d("CreateGroup", "inheritClues: done, created ${clues.size} memos")
    }

    fun submit() {
        val state = _uiState.value
        if (state.groupName.isBlank()) {
            _uiState.update { it.copy(groupNameError = LocalizedStrings.groupNameRequired) }
            return
        }
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val groupId = groupRepository.createGroup(
                groupName = state.groupName.trim(),
                moduleName = state.moduleName.trim(),
                system = state.system,
                moduleId = state.selectedModuleId,
                gameFormat = state.gameFormat,
                scale = state.scale.trim(),
                startTime = state.startTime,
                expectedEndTime = state.expectedEndTime,
                defaultSessionTime = state.defaultSessionTime.trim()
            )
            // 继承模组的默认PC、NPC、人物关系和线索
            state.selectedModuleId?.let { moduleId ->
                val pcIdMapping = inheritDefaultPcs(moduleId, groupId, state.system)
                val npcIdMapping = inheritDefaultNpcs(moduleId, groupId)
                inheritRelationships(moduleId, groupId, pcIdMapping, npcIdMapping)
                inheritClues(moduleId, groupId)
            }
            // 自动创建日历事件
            createCalendarEvents(groupId, state)
            _uiState.update { it.copy(isSubmitting = false, createdGroupId = groupId) }
        }
    }
}

package com.example.keepersnotes.ui.screen.groupdetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.*
import com.example.keepersnotes.data.repository.*
import com.example.keepersnotes.util.LocalizedStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupDetailUiState(
    val group: GroupEntity? = null,
    val pcs: List<PlayerCharacterEntity> = emptyList(),
    val npcs: List<NpcEntity> = emptyList(),
    val sessions: List<SessionEntity> = emptyList(),
    val memos: List<KpMemoEntity> = emptyList(),
    val pendingTodos: List<KpMemoEntity> = emptyList(),
    val relationships: List<GroupRelationshipEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: GroupRepository,
    private val pcRepository: PlayerCharacterRepository,
    private val npcRepository: NpcRepository,
    private val sessionRepository: SessionRepository,
    private val kpMemoRepository: KpMemoRepository,
    private val groupRelationshipRepository: GroupRelationshipRepository,
    private val calendarEventRepository: CalendarEventRepository
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(GroupDetailUiState(isLoading = true))
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    var selectedTab = 0
    var characterSubTab = 0
    var memoFilterIndex = 0

    init {
        if (groupId.isNotBlank()) {
            observeGroupData()
        }
    }

    private fun observeGroupData() {
        combine(
            groupRepository.getGroupById(groupId),
            pcRepository.getPcsByGroupId(groupId),
            npcRepository.getNpcsByGroupId(groupId)
        ) { group, pcs, npcs ->
            Triple(group, pcs, npcs)
        }.combine(
            sessionRepository.getSessionsByGroupId(groupId)
        ) { (group, pcs, npcs), sessions ->
            Quadruple(group, pcs, npcs, sessions)
        }.combine(
            kpMemoRepository.getPendingTodos(groupId)
        ) { (group, pcs, npcs, sessions), todos ->
            Quintuple(group, pcs, npcs, sessions, todos)
        }.combine(
            kpMemoRepository.getMemosByGroupId(groupId)
        ) { (group, pcs, npcs, sessions, todos), memos ->
            Sextuple(group, pcs, npcs, sessions, todos, memos)
        }.combine(
            groupRelationshipRepository.getByGroupId(groupId)
        ) { (group, pcs, npcs, sessions, todos, memos), relationships ->
            GroupDetailUiState(
                group = group,
                pcs = pcs,
                npcs = npcs,
                sessions = sessions,
                memos = memos,
                pendingTodos = todos,
                relationships = relationships,
                isLoading = false
            )
        }.onEach { state ->
            Log.d("GroupDetail", "state updated: pcs=${state.pcs.size}, npcs=${state.npcs.size}, rels=${state.relationships.size}")
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    fun updateGroupStatus(status: String) {
        viewModelScope.launch {
            groupRepository.updateGroupStatus(groupId, status)
        }
    }

    fun updateGroup(updatedGroup: GroupEntity) {
        viewModelScope.launch {
            val oldGroup = _uiState.value.group
            groupRepository.updateGroup(updatedGroup)

            // 如果开始时间、预计结束、默认开团时间有变化，重置日历日程
            val timeChanged = oldGroup != null && (
                oldGroup.startTime != updatedGroup.startTime ||
                oldGroup.expectedEndTime != updatedGroup.expectedEndTime ||
                oldGroup.defaultSessionTime != updatedGroup.defaultSessionTime
            )
            if (timeChanged) {
                calendarEventRepository.deleteByGroupId(groupId)
                val events = mutableListOf<CalendarEventEntity>()
                val time = updatedGroup.defaultSessionTime.trim().ifBlank { null }
                val name = updatedGroup.groupName.trim()
                val start = updatedGroup.startTime
                val end = updatedGroup.expectedEndTime

                if (start != null && end != null && start <= end) {
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
        }
    }

    fun updateLastPlayTime() {
        viewModelScope.launch {
            groupRepository.updateLastPlayTime(groupId)
        }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
        }
    }

    fun toggleMemoCompleted(memoId: String) {
        viewModelScope.launch {
            kpMemoRepository.toggleCompleted(memoId)
        }
    }

    fun updatePc(pc: PlayerCharacterEntity) {
        viewModelScope.launch { pcRepository.updatePc(pc) }
    }

    fun deletePc(pcId: String) {
        viewModelScope.launch { pcRepository.deletePc(pcId) }
    }

    fun updateNpc(npc: NpcEntity) {
        viewModelScope.launch { npcRepository.updateNpc(npc) }
    }

    fun deleteNpc(npcId: String) {
        viewModelScope.launch { npcRepository.deleteNpc(npcId) }
    }

    fun updateMemo(memo: KpMemoEntity) {
        viewModelScope.launch { kpMemoRepository.updateMemo(memo) }
    }

    fun deleteMemo(memoId: String) {
        viewModelScope.launch { kpMemoRepository.deleteMemo(memoId) }
    }

    // 团关系网
    fun createRelationship(sourceId: String, sourceType: String, targetId: String, targetType: String, relationType: String, description: String) {
        viewModelScope.launch {
            groupRelationshipRepository.create(groupId, sourceId, sourceType, targetId, targetType, relationType, description)
        }
    }

    fun updateRelationship(entity: GroupRelationshipEntity) {
        viewModelScope.launch { groupRelationshipRepository.update(entity) }
    }

    fun deleteRelationship(id: String) {
        viewModelScope.launch { groupRelationshipRepository.deleteById(id) }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

private data class Sextuple<A, B, C, D, E, F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
)

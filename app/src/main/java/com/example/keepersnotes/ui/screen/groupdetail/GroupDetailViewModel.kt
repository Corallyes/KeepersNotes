package com.example.keepersnotes.ui.screen.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.*
import com.example.keepersnotes.data.repository.*
import kotlinx.coroutines.flow.flatMapLatest
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
    val module: ModuleEntity? = null,
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
    private val moduleRepository: ModuleRepository,
    private val groupRelationshipRepository: GroupRelationshipRepository
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(GroupDetailUiState(isLoading = true))
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

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
            GroupDetailUiState(
                group = group,
                pcs = pcs,
                npcs = npcs,
                sessions = sessions,
                pendingTodos = todos,
                isLoading = false
            )
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)

        // Observe all memos separately
        kpMemoRepository.getMemosByGroupId(groupId)
            .onEach { memos ->
                _uiState.update { it.copy(memos = memos) }
            }
            .launchIn(viewModelScope)

        // Observe module if group has one
        groupRepository.getGroupById(groupId)
            .filterNotNull()
            .flatMapLatest { group ->
                if (group.moduleId != null) {
                    moduleRepository.getModuleById(group.moduleId)
                } else {
                    flowOf(null)
                }
            }
            .onEach { module ->
                _uiState.update { it.copy(module = module) }
            }
            .launchIn(viewModelScope)

        // Observe group relationships
        groupRelationshipRepository.getByGroupId(groupId)
            .onEach { relationships ->
                _uiState.update { it.copy(relationships = relationships) }
            }
            .launchIn(viewModelScope)
    }

    fun updateGroupStatus(status: String) {
        viewModelScope.launch {
            groupRepository.updateGroupStatus(groupId, status)
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

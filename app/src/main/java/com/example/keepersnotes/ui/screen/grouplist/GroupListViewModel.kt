package com.example.keepersnotes.ui.screen.grouplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.GroupEntity
import com.example.keepersnotes.data.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GroupFilter(val label: String) {
    ALL("全部"), ACTIVE("进行中"), PAUSED("暂停"), COMPLETED("已完结")
}

data class GroupListUiState(
    val groups: List<GroupEntity> = emptyList(),
    val selectedFilter: GroupFilter = GroupFilter.ALL
)

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(GroupFilter.ALL)

    val uiState: StateFlow<GroupListUiState> = combine(
        groupRepository.getAllGroups(),
        _selectedFilter
    ) { groups, filter ->
        val filtered = when (filter) {
            GroupFilter.ALL -> groups
            GroupFilter.ACTIVE -> groups.filter { it.status == "active" }
            GroupFilter.PAUSED -> groups.filter { it.status == "paused" }
            GroupFilter.COMPLETED -> groups.filter { it.status == "completed" }
        }
        GroupListUiState(groups = filtered, selectedFilter = filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupListUiState())

    fun setFilter(filter: GroupFilter) {
        _selectedFilter.value = filter
    }

    fun deleteGroup(groupId: String) {
        viewModelScope.launch { groupRepository.deleteGroup(groupId) }
    }

    fun updateGroupStatus(groupId: String, status: String) {
        viewModelScope.launch { groupRepository.updateGroupStatus(groupId, status) }
    }
}

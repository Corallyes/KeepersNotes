package com.example.keepersnotes.ui.screen.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.NpcEntity
import com.example.keepersnotes.data.repository.NpcRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NpcDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val npcRepository: NpcRepository
) : ViewModel() {

    private val npcId: String = savedStateHandle.get<String>("npcId") ?: ""

    val npc: StateFlow<NpcEntity?> = npcRepository.getNpcById(npcId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateNpc(updatedNpc: NpcEntity) {
        viewModelScope.launch { npcRepository.updateNpc(updatedNpc) }
    }

    fun deleteNpc(onDeleted: () -> Unit) {
        viewModelScope.launch {
            npcRepository.deleteNpc(npcId)
            onDeleted()
        }
    }
}

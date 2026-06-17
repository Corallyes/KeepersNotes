package com.example.keepersnotes.ui.screen.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.PlayerCharacterEntity
import com.example.keepersnotes.data.repository.PlayerCharacterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PcDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pcRepository: PlayerCharacterRepository
) : ViewModel() {

    private val pcId: String = savedStateHandle.get<String>("pcId") ?: ""

    val pc: StateFlow<PlayerCharacterEntity?> = pcRepository.getPcById(pcId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updatePc(updatedPc: PlayerCharacterEntity) {
        viewModelScope.launch { pcRepository.updatePc(updatedPc) }
    }

    fun deletePc(onDeleted: () -> Unit) {
        viewModelScope.launch {
            pcRepository.deletePc(pcId)
            onDeleted()
        }
    }
}

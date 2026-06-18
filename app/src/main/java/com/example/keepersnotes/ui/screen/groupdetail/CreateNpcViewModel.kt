package com.example.keepersnotes.ui.screen.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.repository.NpcRepository
import com.example.keepersnotes.util.LocalizedStrings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateNpcUiState(
    val name: String = "",
    val alias: String = "",
    val occupation: String = "",
    val description: String = "",
    val truePurpose: String = "",
    val gender: String = "",
    val nameError: String? = null,
    val isSubmitting: Boolean = false,
    val createdNpcId: String? = null
)

@HiltViewModel
class CreateNpcViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val npcRepository: NpcRepository
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(CreateNpcUiState())
    val uiState: StateFlow<CreateNpcUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun updateAlias(alias: String) {
        _uiState.update { it.copy(alias = alias) }
    }

    fun updateOccupation(occupation: String) {
        _uiState.update { it.copy(occupation = occupation) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateTruePurpose(truePurpose: String) {
        _uiState.update { it.copy(truePurpose = truePurpose) }
    }

    fun updateGender(value: String) {
        _uiState.update { it.copy(gender = value) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = LocalizedStrings.npcNameRequired) }
            return
        }
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val npcId = npcRepository.createNpc(
                groupId = groupId,
                name = state.name.trim(),
                alias = state.alias.trim(),
                occupation = state.occupation.trim(),
                gender = state.gender
            )
            // Update additional fields
            npcRepository.getNpcById(npcId).collect { npc ->
                npc?.let {
                    npcRepository.updateNpc(
                        it.copy(
                            description = state.description.trim(),
                            truePurpose = state.truePurpose.trim()
                        )
                    )
                }
                return@collect
            }
            _uiState.update { it.copy(isSubmitting = false, createdNpcId = npcId) }
        }
    }
}

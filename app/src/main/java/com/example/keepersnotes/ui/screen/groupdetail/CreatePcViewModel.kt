package com.example.keepersnotes.ui.screen.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.repository.PlayerCharacterRepository
import com.example.keepersnotes.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreatePcUiState(
    val playerName: String = "",
    val characterName: String = "",
    val system: String = Constants.SYSTEM_COC7,
    val hpMax: String = "",
    val sanMax: String = "",
    val gender: String = "",
    val playerNameError: String? = null,
    val characterNameError: String? = null,
    val isSubmitting: Boolean = false,
    val createdPcId: String? = null
)

@HiltViewModel
class CreatePcViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pcRepository: PlayerCharacterRepository
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(CreatePcUiState())
    val uiState: StateFlow<CreatePcUiState> = _uiState.asStateFlow()

    fun updatePlayerName(name: String) {
        _uiState.update { it.copy(playerName = name, playerNameError = null) }
    }

    fun updateCharacterName(name: String) {
        _uiState.update { it.copy(characterName = name, characterNameError = null) }
    }

    fun updateSystem(system: String) {
        _uiState.update { it.copy(system = system) }
    }

    fun updateHpMax(value: String) {
        _uiState.update { it.copy(hpMax = value) }
    }

    fun updateSanMax(value: String) {
        _uiState.update { it.copy(sanMax = value) }
    }

    fun updateGender(value: String) {
        _uiState.update { it.copy(gender = value) }
    }

    fun submit() {
        val state = _uiState.value
        var hasError = false
        if (state.playerName.isBlank()) {
            _uiState.update { it.copy(playerNameError = "请输入玩家昵称") }
            hasError = true
        }
        if (state.characterName.isBlank()) {
            _uiState.update { it.copy(characterNameError = "请输入角色名称") }
            hasError = true
        }
        if (hasError) return

        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val pcId = pcRepository.createPc(
                groupId = groupId,
                playerName = state.playerName.trim(),
                characterName = state.characterName.trim(),
                system = state.system,
                gender = state.gender
            )
            // Update HP/SAN max values if provided
            val hpMax = state.hpMax.toIntOrNull()
            val sanMax = state.sanMax.toIntOrNull()
            if (hpMax != null || sanMax != null) {
                pcRepository.getPcByIdOnce(pcId)?.let { pc ->
                    pcRepository.updatePc(
                        pc.copy(
                            hpMax = hpMax ?: pc.hpMax,
                            hpCurrent = hpMax ?: pc.hpCurrent,
                            sanMax = sanMax ?: pc.sanMax,
                            sanCurrent = sanMax ?: pc.sanCurrent
                        )
                    )
                }
            }
            _uiState.update { it.copy(isSubmitting = false, createdPcId = pcId) }
        }
    }
}

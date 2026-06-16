package com.example.keepersnotes.ui.screen.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.PlayerCharacterEntity
import com.example.keepersnotes.data.repository.PlayerCharacterRepository
import com.example.keepersnotes.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

data class CreateSessionUiState(
    val durationMinutes: String = "",
    val summary: String = "",
    val nextSessionNotes: String = "",
    val pcs: List<PlayerCharacterEntity> = emptyList(),
    val selectedPcIds: Set<String> = emptySet(),
    val importantEvents: List<String> = emptyList(),
    val cluesFound: List<String> = emptyList(),
    val newEventText: String = "",
    val newClueText: String = "",
    val isSubmitting: Boolean = false,
    val createdSessionId: String? = null
)

@HiltViewModel
class CreateSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository,
    private val pcRepository: PlayerCharacterRepository
) : ViewModel() {

    private val groupId: String = savedStateHandle.get<String>("groupId") ?: ""

    private val _uiState = MutableStateFlow(CreateSessionUiState())
    val uiState: StateFlow<CreateSessionUiState> = _uiState.asStateFlow()

    init {
        pcRepository.getPcsByGroupId(groupId)
            .onEach { pcs -> _uiState.update { it.copy(pcs = pcs) } }
            .launchIn(viewModelScope)
    }

    fun updateDurationMinutes(value: String) {
        _uiState.update { it.copy(durationMinutes = value) }
    }

    fun updateSummary(summary: String) {
        _uiState.update { it.copy(summary = summary) }
    }

    fun updateNextSessionNotes(notes: String) {
        _uiState.update { it.copy(nextSessionNotes = notes) }
    }

    fun togglePcSelection(pcId: String) {
        _uiState.update { state ->
            val newSet = if (pcId in state.selectedPcIds) {
                state.selectedPcIds - pcId
            } else {
                state.selectedPcIds + pcId
            }
            state.copy(selectedPcIds = newSet)
        }
    }

    fun updateNewEventText(text: String) {
        _uiState.update { it.copy(newEventText = text) }
    }

    fun addEvent() {
        val text = _uiState.value.newEventText.trim()
        if (text.isNotBlank()) {
            _uiState.update {
                it.copy(
                    importantEvents = it.importantEvents + text,
                    newEventText = ""
                )
            }
        }
    }

    fun removeEvent(index: Int) {
        _uiState.update {
            it.copy(importantEvents = it.importantEvents.toMutableList().apply { removeAt(index) })
        }
    }

    fun updateNewClueText(text: String) {
        _uiState.update { it.copy(newClueText = text) }
    }

    fun addClue() {
        val text = _uiState.value.newClueText.trim()
        if (text.isNotBlank()) {
            _uiState.update {
                it.copy(
                    cluesFound = it.cluesFound + text,
                    newClueText = ""
                )
            }
        }
    }

    fun removeClue(index: Int) {
        _uiState.update {
            it.copy(cluesFound = it.cluesFound.toMutableList().apply { removeAt(index) })
        }
    }

    fun submit() {
        val state = _uiState.value
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            val participantPcIds = state.selectedPcIds.joinToString(",")
            val eventsJson = JSONArray(state.importantEvents).toString()
            val cluesJson = JSONArray(state.cluesFound).toString()

            val sessionId = sessionRepository.createSession(
                groupId = groupId,
                participantPcIds = participantPcIds
            )
            sessionRepository.getSessionById(sessionId).firstOrNull()?.let { session ->
                sessionRepository.updateSession(
                    session.copy(
                        durationMinutes = state.durationMinutes.toIntOrNull() ?: 0,
                        summary = state.summary.trim(),
                        nextSessionNotes = state.nextSessionNotes.trim(),
                        importantEventsJson = eventsJson,
                        cluesFoundJson = cluesJson
                    )
                )
            }
            _uiState.update { it.copy(isSubmitting = false, createdSessionId = sessionId) }
        }
    }
}

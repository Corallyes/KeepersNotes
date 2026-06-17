package com.example.keepersnotes.ui.screen.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.SessionEntity
import com.example.keepersnotes.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId") ?: ""

    val session: StateFlow<SessionEntity?> = sessionRepository.getSessionById(sessionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun deleteSession(onDeleted: () -> Unit) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
            onDeleted()
        }
    }
}

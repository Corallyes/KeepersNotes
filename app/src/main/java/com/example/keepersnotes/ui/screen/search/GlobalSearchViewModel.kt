package com.example.keepersnotes.ui.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.dao.SearchResult
import com.example.keepersnotes.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GlobalSearchUiState(
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchHistory: List<String> = emptyList()
)

@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GlobalSearchUiState())
    val uiState: StateFlow<GlobalSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private val _searchHistory = mutableListOf<String>()

    fun search(query: String, filter: String? = null) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }

        // 添加到搜索历史
        if (query.length >= 2) {
            _searchHistory.remove(query)
            _searchHistory.add(0, query)
            if (_searchHistory.size > 10) _searchHistory.removeAt(_searchHistory.size - 1)
            _uiState.update { it.copy(searchHistory = _searchHistory.toList()) }
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(300) // 防抖

            val flow = when (filter) {
                "module" -> searchRepository.searchModules(query)
                "memo" -> searchRepository.searchMemos(query)
                "highlight" -> searchRepository.searchHighlights(query)
                "annotation" -> searchRepository.searchAnnotations(query)
                "bookmark" -> searchRepository.searchBookmarks(query)
                else -> searchRepository.searchAll(query)
            }

            flow.collect { results ->
                _uiState.update {
                    it.copy(
                        results = results,
                        isSearching = false
                    )
                }
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update { it.copy(results = emptyList(), isSearching = false) }
    }
}

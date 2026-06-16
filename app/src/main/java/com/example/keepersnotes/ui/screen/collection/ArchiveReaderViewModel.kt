package com.example.keepersnotes.ui.screen.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.ArchiveEntity
import com.example.keepersnotes.data.repository.ArchiveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class TocEntry(
    val title: String,
    val level: Int,       // 1-6 对应 # ~ ######
    val lineIndex: Int    // 在原文行列表中的索引
)

data class SearchMatch(
    val lineIndex: Int,
    val lineText: String
)

data class ArchiveReaderUiState(
    val archive: ArchiveEntity? = null,
    val toc: List<TocEntry> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ArchiveReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val archiveRepository: ArchiveRepository
) : ViewModel() {

    private val archiveId: String = savedStateHandle["archiveId"] ?: ""

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<SearchMatch>>(emptyList())
    val searchResults: StateFlow<List<SearchMatch>> = _searchResults

    val uiState: StateFlow<ArchiveReaderUiState> = archiveRepository.getById(archiveId)
        .map { archive ->
            val toc = if (archive != null) extractToc(archive.contentMarkdown) else emptyList()
            ArchiveReaderUiState(
                archive = archive,
                toc = toc,
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArchiveReaderUiState())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        val markdown = uiState.value.archive?.contentMarkdown ?: ""
        _searchResults.value = if (query.isBlank()) emptyList() else searchInContent(markdown, query)
    }

    private fun extractToc(markdown: String): List<TocEntry> {
        val entries = mutableListOf<TocEntry>()
        val lines = markdown.lines()
        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()
            val match = Regex("^(#{1,6})\\s+(.+)").find(trimmed)
            if (match != null) {
                val level = match.groupValues[1].length
                val title = match.groupValues[2].trim()
                entries.add(TocEntry(title = title, level = level, lineIndex = index))
            }
        }
        return entries
    }

    private fun searchInContent(markdown: String, query: String): List<SearchMatch> {
        val results = mutableListOf<SearchMatch>()
        val lines = markdown.lines()
        val queryLower = query.lowercase()
        lines.forEachIndexed { index, line ->
            if (line.lowercase().contains(queryLower)) {
                results.add(SearchMatch(lineIndex = index, lineText = line.trim()))
            }
        }
        return results
    }
}

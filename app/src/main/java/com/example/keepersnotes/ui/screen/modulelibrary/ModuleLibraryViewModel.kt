package com.example.keepersnotes.ui.screen.modulelibrary

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.importer.ZipImportManager
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.util.FileReaderUtil
import com.example.keepersnotes.util.ModuleContentParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class ModuleTab(val label: String) {
    ALL("全部"), FAVORITES("我的收藏"), MINE("我的模组")
}

enum class ModuleSort(val label: String) {
    NAME_ASC("名称 A-Z"),
    NAME_DESC("名称 Z-A"),
    NEWEST("最新导入"),
    OLDEST("最早导入"),
    SYSTEM("按系统")
}

sealed class ImportResult {
    data class Success(val title: String) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

data class ModuleLibraryUiState(
    val modules: List<ModuleEntity> = emptyList(),
    val selectedTab: ModuleTab = ModuleTab.ALL,
    val searchQuery: String = "",
    val selectedSystem: String? = null,
    val sortOption: ModuleSort = ModuleSort.NEWEST,
    val importResult: ImportResult? = null
)

@HiltViewModel
class ModuleLibraryViewModel @Inject constructor(
    application: Application,
    private val moduleRepository: ModuleRepository,
    private val zipImportManager: ZipImportManager
) : AndroidViewModel(application) {

    private val _selectedTab = MutableStateFlow(ModuleTab.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedSystem = MutableStateFlow<String?>(null)
    private val _sortOption = MutableStateFlow(ModuleSort.NEWEST)
    private val _importResult = MutableStateFlow<ImportResult?>(null)

    val uiState: StateFlow<ModuleLibraryUiState> = combine(
        moduleRepository.getAllModules(),
        _selectedTab,
        _searchQuery,
        _selectedSystem
    ) { allModules, tab, query, system ->
        val baseList = when (tab) {
            ModuleTab.ALL -> allModules
            ModuleTab.FAVORITES -> allModules.filter { it.isFavorite }
            ModuleTab.MINE -> allModules.filter { it.isUserCreated }
        }
        baseList
            .let { list -> if (system != null) list.filter { it.system == system } else list }
            .let { list ->
                if (query.isBlank()) list
                else list.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.author.contains(query, ignoreCase = true) ||
                            it.tags.contains(query, ignoreCase = true)
                }
            }
    }.combine(_sortOption) { filtered, sort ->
        Pair(filtered, sort)
    }.combine(_importResult) { (filtered, sort), importResult ->
        val sorted = when (sort) {
            ModuleSort.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
            ModuleSort.NAME_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            ModuleSort.NEWEST -> filtered.sortedByDescending { it.createTime }
            ModuleSort.OLDEST -> filtered.sortedBy { it.createTime }
            ModuleSort.SYSTEM -> filtered.sortedBy { it.system }
        }
        ModuleLibraryUiState(
            modules = sorted,
            selectedTab = _selectedTab.value,
            searchQuery = _searchQuery.value,
            selectedSystem = _selectedSystem.value,
            sortOption = sort,
            importResult = importResult
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ModuleLibraryUiState())

    fun setTab(tab: ModuleTab) { _selectedTab.value = tab }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSystemFilter(system: String?) { _selectedSystem.value = system }
    fun setSortOption(sort: ModuleSort) { _sortOption.value = sort }

    fun toggleFavorite(moduleId: String) {
        viewModelScope.launch { moduleRepository.toggleFavorite(moduleId) }
    }

    fun importModuleFromUri(uri: Uri, title: String, author: String, system: String) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val readResult = FileReaderUtil.readFileContent(context, uri)
            readResult.fold(
                onSuccess = { rawContent ->
                    val chapters = ModuleContentParser.parseTextToChapters(rawContent)
                    val contentJson = ModuleContentParser.chaptersToJson(chapters)
                    moduleRepository.importModule(
                        title = title,
                        author = author,
                        system = system,
                        content = contentJson
                    )
                    _importResult.value = ImportResult.Success(title)
                },
                onFailure = { error ->
                    _importResult.value = ImportResult.Error(error.message ?: "导入失败")
                }
            )
        }
    }

    fun importZipFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val collectionId = UUID.randomUUID().toString()
                val result = zipImportManager.importZip(context, uri, collectionId)

                // Create a ModuleEntity with the same ID so archives/images link correctly
                moduleRepository.importModule(
                    title = result.collectionTitle,
                    author = "",
                    system = "",
                    content = "",
                    isCollection = true,
                    moduleId = collectionId
                )
                _importResult.value = ImportResult.Success(result.collectionTitle)
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "ZIP 导入失败")
            }
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    fun updateModule(module: ModuleEntity) {
        viewModelScope.launch { moduleRepository.updateModule(module) }
    }

    fun deleteModule(moduleId: String) {
        viewModelScope.launch { moduleRepository.deleteModule(moduleId) }
    }
}

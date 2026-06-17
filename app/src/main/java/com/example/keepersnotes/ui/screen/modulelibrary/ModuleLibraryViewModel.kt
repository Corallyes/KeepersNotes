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
import com.example.keepersnotes.util.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

enum class ModuleTab(private val zhLabel: String, private val enLabel: String) {
    ALL("全部", "All"), FAVORITES("我的收藏", "Favorites"), MINE("我的模组", "My Modules");

    val label: String
        get() {
            val isEn = ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_ENGLISH ||
                    (ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_SYSTEM &&
                        java.util.Locale.getDefault().language == "en")
            return if (isEn) enLabel else zhLabel
        }
}

enum class ModuleSort(private val zhLabel: String, private val enLabel: String) {
    NAME_ASC("名称 A-Z", "Name A-Z"),
    NAME_DESC("名称 Z-A", "Name Z-A"),
    NEWEST("最新导入", "Newest"),
    OLDEST("最早导入", "Oldest"),
    SYSTEM("按系统", "By System");

    val label: String
        get() {
            val isEn = ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_ENGLISH ||
                    (ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_SYSTEM &&
                        java.util.Locale.getDefault().language == "en")
            return if (isEn) enLabel else zhLabel
        }
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
    val importResult: ImportResult? = null,
    val isImporting: Boolean = false
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
    private val _isImporting = MutableStateFlow(false)

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
        Triple(filtered, sort, importResult)
    }.combine(_isImporting) { (filtered, sort, importResult), isImporting ->
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
            importResult = importResult,
            isImporting = isImporting
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
            _isImporting.value = true
            try {
                val context = getApplication<Application>()
                val result = withContext(Dispatchers.IO) {
                    val readResult = FileReaderUtil.readFileContent(context, uri)
                    readResult.fold(
                        onSuccess = { rawContent ->
                            val cleanTitle = title.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F\\uFFFD]"), "").trim()
                            android.util.Log.d("ModuleImport", "title=$cleanTitle, rawContent=${rawContent.length} chars, first100=${rawContent.take(100)}")
                            val chapters = ModuleContentParser.parseTextToChapters(rawContent)
                            android.util.Log.d("ModuleImport", "chapters=${chapters.size}, firstTitle=${chapters.firstOrNull()?.title}")
                            val contentJson = ModuleContentParser.chaptersToJson(chapters)
                            val moduleId = moduleRepository.importModule(
                                title = cleanTitle,
                                author = author,
                                system = system,
                                content = contentJson
                            )
                            ImportResult.Success(title)
                        },
                        onFailure = { error ->
                            ImportResult.Error(error.message ?: "导入失败")
                        }
                    )
                }
                _importResult.value = result
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "导入失败")
            } finally {
                _isImporting.value = false
            }
        }
    }

    fun importZipFromUri(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val context = getApplication<Application>()
                val collectionId = UUID.randomUUID().toString()
                val resultTitle = withContext(Dispatchers.IO) {
                    val result = zipImportManager.importArchive(context, uri, collectionId)
                    // Parse combined content into chapters, same as single file import
                    val chapters = ModuleContentParser.parseTextToChapters(result.combinedContent)
                    val contentJson = ModuleContentParser.chaptersToJson(chapters)
                    moduleRepository.importModule(
                        title = result.collectionTitle,
                        author = "",
                        system = "",
                        content = contentJson,
                        isCollection = false,
                        moduleId = collectionId
                    )
                    zipImportManager.insertImportResult(result)
                    result.collectionTitle
                }
                _importResult.value = ImportResult.Success(resultTitle)
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: "ZIP 导入失败")
            } finally {
                _isImporting.value = false
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

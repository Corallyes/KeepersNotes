package com.example.keepersnotes.ui.screen.modulelibrary

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.importer.ZipImportManager
import com.example.keepersnotes.data.local.entity.DocumentNodeEntity
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.repository.DocumentNodeRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.util.FileReaderUtil
import com.example.keepersnotes.util.LocalizedStrings
import com.example.keepersnotes.util.ModuleContentParser
import com.example.keepersnotes.util.StructuredDocxParser
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
    private val documentNodeRepository: DocumentNodeRepository,
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
                val cleanTitle = title.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F\\uFFFD]"), "").trim()
                val fileName = getFileName(context, uri)
                val mimeType = context.contentResolver.getType(uri) ?: ""
                val isDocx = fileName.endsWith(".docx") || mimeType.contains("wordprocessingml")
                val isTxt = fileName.endsWith(".txt") || mimeType == "text/plain"
                android.util.Log.d("ModuleImport", "fileName=$fileName, mimeType=$mimeType, isDocx=$isDocx, isTxt=$isTxt, uri=$uri")

                val result = withContext(Dispatchers.IO) {
                    if (isDocx) {
                        // New flow: structured parsing → document_nodes table
                        android.util.Log.d("ModuleImport", "Using structured docx parser")
                        val nodesResult = FileReaderUtil.readDocxStructured(context, uri)
                        nodesResult.fold(
                            onSuccess = { nodes ->
                                android.util.Log.d("ModuleImport", "Parsed ${nodes.size} nodes")
                                val moduleId = moduleRepository.importModule(
                                    title = cleanTitle,
                                    author = author,
                                    system = system,
                                    content = "{}" // contentJson not used for docx
                                )
                                // Save structured nodes
                                val entities = nodes.map { node ->
                                    DocumentNodeEntity(
                                        nodeId = "${moduleId}_${node.order}",
                                        moduleId = moduleId,
                                        type = node.type,
                                        level = node.level,
                                        content = node.content,
                                        tableData = node.tableData?.let { tableToJson(it) },
                                        imageUri = node.imageUri,
                                        order = node.order
                                    )
                                }
                                android.util.Log.d("ModuleImport", "Inserting ${entities.size} nodes for moduleId=$moduleId")
                                try {
                                    documentNodeRepository.insertNodes(entities)
                                    val count = documentNodeRepository.getNodeCount(moduleId)
                                    android.util.Log.d("ModuleImport", "Insert done, nodeCount=$count")
                                } catch (e: Exception) {
                                    android.util.Log.e("ModuleImport", "Insert failed: ${e.message}", e)
                                }
                                ImportResult.Success(title)
                            },
                            onFailure = { error ->
                                android.util.Log.e("ModuleImport", "DOCX parse failed: ${error.message}", error)
                                ImportResult.Error(error.message ?: LocalizedStrings.docxParseFailed)
                            }
                        )
                    } else if (isTxt) {
                        // New flow: structured parsing for TXT files using Python
                        android.util.Log.d("ModuleImport", "Using structured txt parser")
                        try {
                            val nodes = FileReaderUtil.readTxtStructured(context, uri)
                            if (nodes.isSuccess) {
                                val txtNodes = nodes.getOrNull() ?: emptyList()
                                android.util.Log.d("ModuleImport", "Parsed ${txtNodes.size} nodes from TXT")
                                val moduleId = moduleRepository.importModule(
                                    title = cleanTitle,
                                    author = author,
                                    system = system,
                                    content = "{}" // contentJson not used for structured parsing
                                )
                                // Save structured nodes
                                val entities = txtNodes.map { node ->
                                    DocumentNodeEntity(
                                        nodeId = "${moduleId}_${node.order}",
                                        moduleId = moduleId,
                                        type = node.type,
                                        level = node.level,
                                        content = node.content,
                                        tableData = null,
                                        imageUri = null,
                                        order = node.order
                                    )
                                }
                                android.util.Log.d("ModuleImport", "Inserting ${entities.size} nodes for moduleId=$moduleId")
                                try {
                                    documentNodeRepository.insertNodes(entities)
                                    val count = documentNodeRepository.getNodeCount(moduleId)
                                    android.util.Log.d("ModuleImport", "Insert done, nodeCount=$count")
                                } catch (e: Exception) {
                                    android.util.Log.e("ModuleImport", "Insert failed: ${e.message}", e)
                                }
                                ImportResult.Success(title)
                            } else {
                                // Fallback to old flow if Python parsing fails
                                android.util.Log.d("ModuleImport", "TXT structured parsing failed, falling back to old flow")
                                val readResult = FileReaderUtil.readFileContent(context, uri)
                                readResult.fold(
                                    onSuccess = { rawContent ->
                                        val chapters = ModuleContentParser.parseTextToChapters(rawContent)
                                        val contentJson = ModuleContentParser.chaptersToJson(chapters)
                                        moduleRepository.importModule(
                                            title = cleanTitle,
                                            author = author,
                                            system = system,
                                            content = contentJson
                                        )
                                        ImportResult.Success(title)
                                    },
                                    onFailure = { error ->
                                        ImportResult.Error(error.message ?: LocalizedStrings.homeImportFail)
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("ModuleImport", "TXT structured parsing exception: ${e.message}", e)
                            // Fallback to old flow
                            val readResult = FileReaderUtil.readFileContent(context, uri)
                            readResult.fold(
                                onSuccess = { rawContent ->
                                    val chapters = ModuleContentParser.parseTextToChapters(rawContent)
                                    val contentJson = ModuleContentParser.chaptersToJson(chapters)
                                    moduleRepository.importModule(
                                        title = cleanTitle,
                                        author = author,
                                        system = system,
                                        content = contentJson
                                    )
                                    ImportResult.Success(title)
                                },
                                onFailure = { error ->
                                    ImportResult.Error(error.message ?: LocalizedStrings.homeImportFail)
                                }
                            )
                        }
                    } else {
                        // Old flow: other file types → text → chapters → contentJson
                        val readResult = FileReaderUtil.readFileContent(context, uri)
                        readResult.fold(
                            onSuccess = { rawContent ->
                                android.util.Log.d("ModuleImport", "title=$cleanTitle, rawContent=${rawContent.length} chars")
                                val chapters = ModuleContentParser.parseTextToChapters(rawContent)
                                val contentJson = ModuleContentParser.chaptersToJson(chapters)
                                moduleRepository.importModule(
                                    title = cleanTitle,
                                    author = author,
                                    system = system,
                                    content = contentJson
                                )
                                ImportResult.Success(title)
                            },
                            onFailure = { error ->
                                ImportResult.Error(error.message ?: LocalizedStrings.homeImportFail)
                            }
                        )
                    }
                }
                _importResult.value = result
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: LocalizedStrings.homeImportFail)
            } finally {
                _isImporting.value = false
            }
        }
    }

    private fun tableToJson(table: List<List<String>>): String {
        val arr = org.json.JSONArray()
        for (row in table) {
            val rowArr = org.json.JSONArray()
            for (cell in row) {
                rowArr.put(cell)
            }
            arr.put(rowArr)
        }
        return arr.toString()
    }

    private fun getFileName(context: android.content.Context, uri: Uri): String {
        var fileName = ""
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex) ?: ""
            }
        }
        return fileName
    }

    fun importZipFromUri(uri: Uri) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val context = getApplication<Application>()
                val collectionId = UUID.randomUUID().toString()
                val resultTitle = withContext(Dispatchers.IO) {
                    val result = zipImportManager.importArchive(context, uri, collectionId)

                    if (result.structuredNodes.isNotEmpty()) {
                        // New flow: save structured nodes
                        moduleRepository.importModule(
                            title = result.collectionTitle,
                            author = "",
                            system = "",
                            content = "{}",
                            isCollection = false,
                            moduleId = collectionId
                        )
                        val entities = result.structuredNodes.map { node ->
                            DocumentNodeEntity(
                                nodeId = "${collectionId}_${node.order}",
                                moduleId = collectionId,
                                type = node.type,
                                level = node.level,
                                content = node.content,
                                tableData = node.tableData?.let { tableToJson(it) },
                                imageUri = node.imageUri,
                                order = node.order
                            )
                        }
                        documentNodeRepository.insertNodes(entities)
                    } else {
                        // Old flow: parse combined markdown into chapters
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
                    }
                    zipImportManager.insertImportResult(result)
                    result.collectionTitle
                }
                _importResult.value = ImportResult.Success(resultTitle)
            } catch (e: Exception) {
                _importResult.value = ImportResult.Error(e.message ?: LocalizedStrings.zipImportFailed)
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

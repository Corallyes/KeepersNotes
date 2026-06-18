package com.example.keepersnotes.ui.screen.modulelibrary

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.AnnotationEntity
import com.example.keepersnotes.data.local.entity.BookmarkEntity
import com.example.keepersnotes.data.local.entity.HighlightEntity
import com.example.keepersnotes.data.local.entity.ImageEntity
import com.example.keepersnotes.data.local.entity.KpMemoEntity
import com.example.keepersnotes.data.local.entity.ModuleDefaultNpcEntity
import com.example.keepersnotes.data.local.entity.ModuleDefaultPcEntity
import com.example.keepersnotes.data.local.entity.ModuleLocationEntity
import com.example.keepersnotes.data.local.entity.ModuleOrganizationEntity
import com.example.keepersnotes.data.local.entity.ModuleRelationshipEntity
import com.example.keepersnotes.data.local.entity.ModuleClueEntity
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.local.entity.DocumentNodeEntity
import com.example.keepersnotes.data.local.entity.ReadingProgressEntity
import com.example.keepersnotes.data.repository.AnnotationRepository
import com.example.keepersnotes.data.repository.BookmarkRepository
import com.example.keepersnotes.data.repository.HighlightRepository
import com.example.keepersnotes.data.repository.ImageRepository
import com.example.keepersnotes.data.repository.ModuleDefaultNpcRepository
import com.example.keepersnotes.data.repository.ModuleDefaultPcRepository
import com.example.keepersnotes.data.repository.ModuleLocationRepository
import com.example.keepersnotes.data.repository.ModuleOrganizationRepository
import com.example.keepersnotes.data.repository.ModuleRelationshipRepository
import com.example.keepersnotes.data.repository.ModuleClueRepository
import com.example.keepersnotes.data.repository.DocumentNodeRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import com.example.keepersnotes.data.repository.KpMemoRepository
import com.example.keepersnotes.data.repository.ReadingProgressRepository
import com.example.keepersnotes.util.Chapter
import com.example.keepersnotes.util.ModuleContentParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class ModuleDetailUiState(
    val module: ModuleEntity? = null,
    val chapters: List<Chapter> = emptyList(),
    val documentHeadings: List<DocumentNodeEntity> = emptyList(),
    val highlights: List<HighlightEntity> = emptyList(),
    val annotations: List<AnnotationEntity> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val memos: List<KpMemoEntity> = emptyList(),
    val defaultPcs: List<ModuleDefaultPcEntity> = emptyList(),
    val defaultNpcs: List<ModuleDefaultNpcEntity> = emptyList(),
    val locations: List<ModuleLocationEntity> = emptyList(),
    val organizations: List<ModuleOrganizationEntity> = emptyList(),
    val relationships: List<ModuleRelationshipEntity> = emptyList(),
    val clues: List<ModuleClueEntity> = emptyList(),
    val readingProgress: ReadingProgressEntity? = null,
    val images: List<ImageEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class ModuleDetailViewModel @Inject constructor(
    application: Application,
    savedStateHandle: SavedStateHandle,
    private val moduleRepository: ModuleRepository,
    private val documentNodeRepository: DocumentNodeRepository,
    private val highlightRepository: HighlightRepository,
    private val annotationRepository: AnnotationRepository,
    private val bookmarkRepository: BookmarkRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val imageRepository: ImageRepository,
    private val kpMemoRepository: KpMemoRepository,
    private val defaultPcRepository: ModuleDefaultPcRepository,
    private val defaultNpcRepository: ModuleDefaultNpcRepository,
    private val locationRepository: ModuleLocationRepository,
    private val organizationRepository: ModuleOrganizationRepository,
    private val relationshipRepository: ModuleRelationshipRepository,
    private val clueRepository: ModuleClueRepository
) : AndroidViewModel(application) {

    private val moduleId: String = savedStateHandle.get<String>("moduleId") ?: ""

    private val _uiState = MutableStateFlow(ModuleDetailUiState(isLoading = true))
    val uiState: StateFlow<ModuleDetailUiState> = _uiState.asStateFlow()

    init {
        if (moduleId.isNotBlank()) {
            moduleRepository.getModuleById(moduleId)
                .onEach { module ->
                    if (module != null) {
                        val chapters = ModuleContentParser.jsonToChapters(module.contentJson)
                        _uiState.update {
                            it.copy(
                                module = module,
                                chapters = chapters,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
                .launchIn(viewModelScope)

            documentNodeRepository.getHeadingsByModule(moduleId)
                .onEach { headings -> _uiState.update { it.copy(documentHeadings = headings) } }
                .launchIn(viewModelScope)

            highlightRepository.getHighlightsByModule(moduleId)
                .onEach { highlights -> _uiState.update { it.copy(highlights = highlights) } }
                .launchIn(viewModelScope)

            annotationRepository.getAnnotationsByModule(moduleId)
                .onEach { annotations -> _uiState.update { it.copy(annotations = annotations) } }
                .launchIn(viewModelScope)

            bookmarkRepository.getBookmarksByModule(moduleId)
                .onEach { bookmarks -> _uiState.update { it.copy(bookmarks = bookmarks) } }
                .launchIn(viewModelScope)

            readingProgressRepository.getProgressByModule(moduleId)
                .onEach { progress -> _uiState.update { it.copy(readingProgress = progress) } }
                .launchIn(viewModelScope)

            imageRepository.getByCollectionId(moduleId)
                .onEach { images -> _uiState.update { it.copy(images = images) } }
                .launchIn(viewModelScope)

            kpMemoRepository.getMemosByModuleId(moduleId)
                .onEach { memos -> _uiState.update { it.copy(memos = memos) } }
                .launchIn(viewModelScope)

            defaultPcRepository.getByModuleId(moduleId)
                .onEach { pcs -> _uiState.update { it.copy(defaultPcs = pcs) } }
                .launchIn(viewModelScope)

            defaultNpcRepository.getByModuleId(moduleId)
                .onEach { npcs -> _uiState.update { it.copy(defaultNpcs = npcs) } }
                .launchIn(viewModelScope)

            locationRepository.getByModuleId(moduleId)
                .onEach { locations -> _uiState.update { it.copy(locations = locations) } }
                .launchIn(viewModelScope)

            organizationRepository.getByModuleId(moduleId)
                .onEach { orgs -> _uiState.update { it.copy(organizations = orgs) } }
                .launchIn(viewModelScope)

            relationshipRepository.getByModuleId(moduleId)
                .onEach { rels -> _uiState.update { it.copy(relationships = rels) } }
                .launchIn(viewModelScope)

            clueRepository.getByModuleId(moduleId)
                .onEach { clues -> _uiState.update { it.copy(clues = clues) } }
                .launchIn(viewModelScope)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun updateModule(module: ModuleEntity) {
        viewModelScope.launch { moduleRepository.updateModule(module) }
    }

    fun deleteModule() {
        viewModelScope.launch {
            moduleRepository.deleteModule(moduleId)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            moduleRepository.toggleFavorite(moduleId)
        }
    }

    fun importImage(uri: Uri, title: String = "") {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val imageDir = File(context.filesDir, "images/$moduleId")
                imageDir.mkdirs()

                val fileName = getFileName(context, uri) ?: "image_${System.currentTimeMillis()}"
                val ext = fileName.substringAfterLast(".", "jpg")
                val destFile = File(imageDir, "${UUID.randomUUID()}.$ext")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                val imageEntity = ImageEntity(
                    imageId = UUID.randomUUID().toString(),
                    collectionId = moduleId,
                    title = title.ifBlank { fileName.substringBeforeLast(".") },
                    filePath = destFile.absolutePath,
                    originalFileName = fileName,
                    sortOrder = _uiState.value.images.size,
                    createTime = System.currentTimeMillis()
                )
                imageRepository.insert(imageEntity)
            } catch (_: Exception) {
            }
        }
    }

    fun deleteImage(imageId: String) {
        viewModelScope.launch {
            val image = imageRepository.getByIdOnce(imageId)
            if (image != null) {
                File(image.filePath).delete()
                imageRepository.deleteById(imageId)
            }
        }
    }

    private fun getFileName(context: android.content.Context, uri: Uri): String? {
        var fileName: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    // 推荐PC
    fun createDefaultPc(name: String, playerName: String = "", system: String = "", description: String = "") {
        viewModelScope.launch { defaultPcRepository.create(moduleId, name, playerName, system, description) }
    }

    fun updateDefaultPc(entity: ModuleDefaultPcEntity) {
        viewModelScope.launch { defaultPcRepository.update(entity) }
    }

    fun deleteDefaultPc(id: String) {
        viewModelScope.launch { defaultPcRepository.deleteById(id) }
    }

    // 默认NPC
    fun createDefaultNpc(name: String, alias: String = "", occupation: String = "", description: String = "", truePurpose: String = "", gender: String = "") {
        viewModelScope.launch { defaultNpcRepository.create(moduleId, name, alias, occupation, description, truePurpose, gender) }
    }

    fun updateDefaultNpc(entity: ModuleDefaultNpcEntity) {
        viewModelScope.launch { defaultNpcRepository.update(entity) }
    }

    fun deleteDefaultNpc(id: String) {
        viewModelScope.launch { defaultNpcRepository.deleteById(id) }
    }

    // 地点
    fun createLocation(name: String, type: String = "", description: String = "", clues: String = "", inhabitants: String = "") {
        viewModelScope.launch { locationRepository.create(moduleId, name, type, description, clues, inhabitants) }
    }

    fun updateLocation(entity: ModuleLocationEntity) {
        viewModelScope.launch { locationRepository.update(entity) }
    }

    fun deleteLocation(id: String) {
        viewModelScope.launch { locationRepository.deleteById(id) }
    }

    // 组织
    fun createOrganization(name: String, type: String = "", description: String = "", members: String = "", goals: String = "") {
        viewModelScope.launch { organizationRepository.create(moduleId, name, type, description, members, goals) }
    }

    fun updateOrganization(entity: ModuleOrganizationEntity) {
        viewModelScope.launch { organizationRepository.update(entity) }
    }

    fun deleteOrganization(id: String) {
        viewModelScope.launch { organizationRepository.deleteById(id) }
    }

    // 关系
    fun createRelationship(sourceId: String, sourceType: String, targetId: String, targetType: String, relationType: String = "", description: String = "") {
        viewModelScope.launch { relationshipRepository.create(moduleId, sourceId, sourceType, targetId, targetType, relationType, description) }
    }

    fun updateRelationship(entity: ModuleRelationshipEntity) {
        viewModelScope.launch { relationshipRepository.update(entity) }
    }

    fun deleteRelationship(id: String) {
        viewModelScope.launch { relationshipRepository.deleteById(id) }
    }

    // 线索
    fun createClue(name: String, type: String = "", description: String = "", source: String = "", isHidden: Boolean = false) {
        viewModelScope.launch { clueRepository.create(moduleId, name, type, description, source, isHidden) }
    }

    fun updateClue(entity: ModuleClueEntity) {
        viewModelScope.launch { clueRepository.update(entity) }
    }

    fun deleteClue(id: String) {
        viewModelScope.launch { clueRepository.deleteById(id) }
    }

    fun getChapterTitle(chapterId: String): String {
        return findChapterById(_uiState.value.chapters, chapterId)?.title ?: ""
    }

    private fun findChapterById(chapters: List<Chapter>, id: String): Chapter? {
        for (chapter in chapters) {
            if (chapter.id == id) return chapter
            findChapterById(chapter.children, id)?.let { return it }
        }
        return null
    }
}

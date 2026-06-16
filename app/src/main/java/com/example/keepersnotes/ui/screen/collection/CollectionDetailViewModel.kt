package com.example.keepersnotes.ui.screen.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.ArchiveEntity
import com.example.keepersnotes.data.local.entity.ImageEntity
import com.example.keepersnotes.data.local.entity.ImageGroupEntity
import com.example.keepersnotes.data.local.entity.ModuleEntity
import com.example.keepersnotes.data.repository.ArchiveRepository
import com.example.keepersnotes.data.repository.GroupWithCount
import com.example.keepersnotes.data.repository.ImageGroupRepository
import com.example.keepersnotes.data.repository.ImageRepository
import com.example.keepersnotes.data.repository.ModuleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionDetailUiState(
    val collection: ModuleEntity? = null,
    val archives: List<ArchiveEntity> = emptyList(),
    val images: List<ImageEntity> = emptyList(),
    val groups: List<GroupWithCount> = emptyList(),
    val selectedGroupId: String? = null, // null = show all
    val isLoading: Boolean = true
)

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val moduleRepository: ModuleRepository,
    private val archiveRepository: ArchiveRepository,
    private val imageRepository: ImageRepository,
    private val imageGroupRepository: ImageGroupRepository
) : ViewModel() {

    private val collectionId: String = savedStateHandle["collectionId"] ?: ""

    private val _selectedGroupId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CollectionDetailUiState> = combine(
        moduleRepository.getModuleById(collectionId),
        archiveRepository.getByCollectionId(collectionId),
        imageRepository.getByCollectionId(collectionId),
        imageGroupRepository.getGroupsWithImageCount(collectionId),
        _selectedGroupId
    ) { collection, archives, allImages, groups, selectedGroupId ->
        val filteredImages = if (selectedGroupId != null) {
            allImages.filter { it.imageGroupId == selectedGroupId }
        } else {
            allImages
        }
        CollectionDetailUiState(
            collection = collection,
            archives = archives,
            images = filteredImages,
            groups = groups,
            selectedGroupId = selectedGroupId,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CollectionDetailUiState())

    fun selectGroup(groupId: String?) {
        _selectedGroupId.value = groupId
    }

    fun createGroup(name: String, description: String = "") {
        viewModelScope.launch {
            imageGroupRepository.createGroup(collectionId, name, description)
        }
    }

    fun assignImageToGroup(imageId: String, groupId: String) {
        viewModelScope.launch {
            imageGroupRepository.assignImageToGroup(imageId, groupId)
        }
    }

    fun removeImageFromGroup(imageId: String) {
        viewModelScope.launch {
            imageGroupRepository.removeImageFromGroup(imageId)
        }
    }

    suspend fun getArchiveById(archiveId: String): ArchiveEntity? =
        archiveRepository.getByIdOnce(archiveId)
}

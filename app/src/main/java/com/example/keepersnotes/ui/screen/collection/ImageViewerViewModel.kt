package com.example.keepersnotes.ui.screen.collection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.keepersnotes.data.local.entity.ImageEntity
import com.example.keepersnotes.data.repository.GroupWithCount
import com.example.keepersnotes.data.repository.ImageGroupRepository
import com.example.keepersnotes.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImageViewerUiState(
    val images: List<ImageEntity> = emptyList(),
    val groups: List<GroupWithCount> = emptyList()
)

@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val imageRepository: ImageRepository,
    private val imageGroupRepository: ImageGroupRepository
) : ViewModel() {

    private val collectionId: String = savedStateHandle["collectionId"] ?: ""

    val uiState: StateFlow<ImageViewerUiState> = combine(
        imageRepository.getByCollectionId(collectionId),
        imageGroupRepository.getGroupsWithImageCount(collectionId)
    ) { images, groups ->
        ImageViewerUiState(
            images = images,
            groups = groups
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ImageViewerUiState())

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

    fun createGroup(name: String, description: String = "") {
        viewModelScope.launch {
            imageGroupRepository.createGroup(collectionId, name, description)
        }
    }
}

package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ImageDao
import com.example.keepersnotes.data.local.dao.ImageGroupDao
import com.example.keepersnotes.data.local.entity.ImageEntity
import com.example.keepersnotes.data.local.entity.ImageGroupEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class GroupWithCount(
    val group: ImageGroupEntity,
    val imageCount: Int
)

@Singleton
class ImageGroupRepository @Inject constructor(
    private val imageGroupDao: ImageGroupDao,
    private val imageDao: ImageDao
) {
    fun getByCollectionId(collectionId: String): Flow<List<ImageGroupEntity>> =
        imageGroupDao.getByCollectionId(collectionId)

    fun getGroupsWithImageCount(collectionId: String): Flow<List<GroupWithCount>> {
        return combine(
            imageGroupDao.getByCollectionId(collectionId),
            imageDao.getByCollectionId(collectionId)
        ) { groups, images ->
            groups.map { group ->
                GroupWithCount(
                    group = group,
                    imageCount = images.count { it.imageGroupId == group.imageGroupId }
                )
            }
        }
    }

    fun getImagesByGroupId(groupId: String): Flow<List<ImageEntity>> =
        imageDao.getByGroupId(groupId)

    suspend fun createGroup(
        collectionId: String,
        name: String,
        description: String = ""
    ): String {
        val groupId = UUID.randomUUID().toString()
        imageGroupDao.insert(
            ImageGroupEntity(
                imageGroupId = groupId,
                collectionId = collectionId,
                name = name,
                description = description
            )
        )
        return groupId
    }

    suspend fun assignImageToGroup(imageId: String, groupId: String) {
        imageDao.updateGroupId(imageId, groupId)
    }

    suspend fun removeImageFromGroup(imageId: String) {
        imageDao.updateGroupId(imageId, null)
    }

    suspend fun deleteGroup(groupId: String) {
        imageGroupDao.deleteById(groupId)
    }
}

package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ImageDao
import com.example.keepersnotes.data.local.entity.ImageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    private val imageDao: ImageDao
) {
    fun getByCollectionId(collectionId: String): Flow<List<ImageEntity>> =
        imageDao.getByCollectionId(collectionId)

    fun getById(imageId: String): Flow<ImageEntity?> =
        imageDao.getById(imageId)

    suspend fun getByIdOnce(imageId: String): ImageEntity? =
        imageDao.getByIdOnce(imageId)

    fun getCountByCollectionId(collectionId: String): Flow<Int> =
        imageDao.getCountByCollectionId(collectionId)

    suspend fun insert(image: ImageEntity) = imageDao.insert(image)

    suspend fun insertAll(images: List<ImageEntity>) = imageDao.insertAll(images)

    suspend fun update(image: ImageEntity) = imageDao.update(image)

    suspend fun delete(image: ImageEntity) = imageDao.delete(image)

    suspend fun deleteById(imageId: String) = imageDao.deleteById(imageId)

    suspend fun deleteByCollectionId(collectionId: String) =
        imageDao.deleteByCollectionId(collectionId)
}

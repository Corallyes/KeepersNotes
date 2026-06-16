package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ArchiveDao
import com.example.keepersnotes.data.local.entity.ArchiveEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArchiveRepository @Inject constructor(
    private val archiveDao: ArchiveDao
) {
    fun getByCollectionId(collectionId: String): Flow<List<ArchiveEntity>> =
        archiveDao.getByCollectionId(collectionId)

    fun getById(archiveId: String): Flow<ArchiveEntity?> =
        archiveDao.getById(archiveId)

    suspend fun getByIdOnce(archiveId: String): ArchiveEntity? =
        archiveDao.getByIdOnce(archiveId)

    fun getCountByCollectionId(collectionId: String): Flow<Int> =
        archiveDao.getCountByCollectionId(collectionId)

    suspend fun insert(archive: ArchiveEntity) = archiveDao.insert(archive)

    suspend fun insertAll(archives: List<ArchiveEntity>) = archiveDao.insertAll(archives)

    suspend fun update(archive: ArchiveEntity) = archiveDao.update(archive)

    suspend fun delete(archive: ArchiveEntity) = archiveDao.delete(archive)

    suspend fun deleteById(archiveId: String) = archiveDao.deleteById(archiveId)

    suspend fun deleteByCollectionId(collectionId: String) =
        archiveDao.deleteByCollectionId(collectionId)
}

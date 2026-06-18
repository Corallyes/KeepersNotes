package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.DocumentNodeDao
import com.example.keepersnotes.data.local.entity.DocumentNodeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentNodeRepository @Inject constructor(
    private val documentNodeDao: DocumentNodeDao
) {
    fun getNodesByModule(moduleId: String): Flow<List<DocumentNodeEntity>> =
        documentNodeDao.getNodesByModule(moduleId)

    fun getHeadingsByModule(moduleId: String): Flow<List<DocumentNodeEntity>> =
        documentNodeDao.getHeadingsByModule(moduleId)

    suspend fun getNodesByModuleOnce(moduleId: String): List<DocumentNodeEntity> =
        documentNodeDao.getNodesByModuleOnce(moduleId)

    suspend fun insertNodes(nodes: List<DocumentNodeEntity>) =
        documentNodeDao.insertAll(nodes)

    suspend fun deleteByModule(moduleId: String) =
        documentNodeDao.deleteByModule(moduleId)

    suspend fun hasNodes(moduleId: String): Boolean =
        documentNodeDao.getNodeCount(moduleId) > 0

    suspend fun getNodeCount(moduleId: String): Int =
        documentNodeDao.getNodeCount(moduleId)
}

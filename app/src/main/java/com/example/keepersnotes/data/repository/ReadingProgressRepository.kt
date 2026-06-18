package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.ReadingProgressDao
import com.example.keepersnotes.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReadingProgressRepository @Inject constructor(
    private val readingProgressDao: ReadingProgressDao
) {

    fun getProgressByModule(moduleId: String): Flow<ReadingProgressEntity?> =
        readingProgressDao.getProgressByModule(moduleId)

    fun getRecentReads(limit: Int = 10): Flow<List<ReadingProgressEntity>> =
        readingProgressDao.getRecentReads(limit)

    suspend fun updateLastChapter(moduleId: String, chapterId: String) {
        val existing = readingProgressDao.getProgressByModule(moduleId)
        // 直接更新，如果不存在则创建
        readingProgressDao.insertOrUpdateProgress(
            ReadingProgressEntity(
                moduleId = moduleId,
                lastChapterId = chapterId,
                lastReadTime = System.currentTimeMillis()
            )
        )
    }

    suspend fun addReadTime(moduleId: String, minutes: Int) {
        readingProgressDao.addReadTime(moduleId, minutes)
    }

    suspend fun addReadTimeSeconds(moduleId: String, seconds: Long) {
        readingProgressDao.addReadTimeSeconds(moduleId, seconds)
    }

    suspend fun ensureProgressExists(moduleId: String) {
        readingProgressDao.insertIfNotExists(
            ReadingProgressEntity(
                moduleId = moduleId,
                lastReadTime = System.currentTimeMillis()
            )
        )
    }

    suspend fun incrementReadCount(moduleId: String) {
        readingProgressDao.incrementReadCount(moduleId)
    }

    suspend fun initializeProgress(moduleId: String, chapterId: String) {
        readingProgressDao.insertOrUpdateProgress(
            ReadingProgressEntity(
                moduleId = moduleId,
                lastChapterId = chapterId,
                lastReadTime = System.currentTimeMillis(),
                readCount = 1
            )
        )
    }

    suspend fun deleteProgress(moduleId: String) {
        readingProgressDao.deleteProgressByModule(moduleId)
    }

    suspend fun updateLastNodePosition(moduleId: String, nodeId: String, scrollOffset: Int, fontSize: Float) {
        readingProgressDao.updateLastNodePosition(moduleId, nodeId, scrollOffset, fontSize)
    }
}

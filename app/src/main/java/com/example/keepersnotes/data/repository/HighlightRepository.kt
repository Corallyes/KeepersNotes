package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.HighlightDao
import com.example.keepersnotes.data.local.entity.HighlightEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HighlightRepository @Inject constructor(
    private val highlightDao: HighlightDao
) {

    fun getHighlightsByModule(moduleId: String): Flow<List<HighlightEntity>> =
        highlightDao.getHighlightsByModule(moduleId)

    fun getHighlightsByChapter(moduleId: String, chapterId: String): Flow<List<HighlightEntity>> =
        highlightDao.getHighlightsByChapter(moduleId, chapterId)

    suspend fun getHighlightById(highlightId: String): HighlightEntity? =
        highlightDao.getHighlightById(highlightId)

    suspend fun addHighlight(
        moduleId: String,
        chapterId: String,
        startIndex: Int,
        endIndex: Int,
        selectedText: String,
        color: Long = 0xFFFFEB3B
    ): HighlightEntity {
        val highlight = HighlightEntity(
            highlightId = UUID.randomUUID().toString(),
            moduleId = moduleId,
            chapterId = chapterId,
            startIndex = startIndex,
            endIndex = endIndex,
            selectedText = selectedText,
            color = color
        )
        highlightDao.insertHighlight(highlight)
        return highlight
    }

    suspend fun updateHighlight(highlight: HighlightEntity) =
        highlightDao.updateHighlight(highlight)

    suspend fun deleteHighlight(highlight: HighlightEntity) =
        highlightDao.deleteHighlight(highlight)

    suspend fun deleteHighlightById(highlightId: String) =
        highlightDao.deleteHighlightById(highlightId)

    suspend fun deleteHighlightsByChapter(moduleId: String, chapterId: String) =
        highlightDao.deleteHighlightsByChapter(moduleId, chapterId)

    suspend fun deleteHighlightsByModule(moduleId: String) =
        highlightDao.deleteHighlightsByModule(moduleId)

    suspend fun deleteHighlightsByColor(moduleId: String, color: Long) =
        highlightDao.deleteHighlightsByColor(moduleId, color)

    suspend fun getHighlightCount(moduleId: String): Int =
        highlightDao.getHighlightCount(moduleId)

    suspend fun getUsedColors(moduleId: String): List<Long> =
        highlightDao.getUsedColors(moduleId)
}

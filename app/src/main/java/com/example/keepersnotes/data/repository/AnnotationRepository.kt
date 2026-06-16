package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.AnnotationDao
import com.example.keepersnotes.data.local.entity.AnnotationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnnotationRepository @Inject constructor(
    private val annotationDao: AnnotationDao
) {

    fun getAnnotationsByModule(moduleId: String): Flow<List<AnnotationEntity>> =
        annotationDao.getAnnotationsByModule(moduleId)

    fun getAnnotationsByChapter(moduleId: String, chapterId: String): Flow<List<AnnotationEntity>> =
        annotationDao.getAnnotationsByChapter(moduleId, chapterId)

    suspend fun getAnnotationById(annotationId: String): AnnotationEntity? =
        annotationDao.getAnnotationById(annotationId)

    suspend fun addAnnotation(
        moduleId: String,
        chapterId: String,
        startIndex: Int,
        endIndex: Int,
        selectedText: String,
        note: String,
        color: Long = 0xFF4CAF50
    ): AnnotationEntity {
        val annotation = AnnotationEntity(
            annotationId = UUID.randomUUID().toString(),
            moduleId = moduleId,
            chapterId = chapterId,
            startIndex = startIndex,
            endIndex = endIndex,
            selectedText = selectedText,
            note = note,
            color = color
        )
        annotationDao.insertAnnotation(annotation)
        return annotation
    }

    suspend fun updateAnnotation(annotation: AnnotationEntity) =
        annotationDao.updateAnnotation(annotation)

    suspend fun deleteAnnotation(annotation: AnnotationEntity) =
        annotationDao.deleteAnnotation(annotation)

    suspend fun deleteAnnotationById(annotationId: String) =
        annotationDao.deleteAnnotationById(annotationId)

    suspend fun deleteAnnotationsByChapter(moduleId: String, chapterId: String) =
        annotationDao.deleteAnnotationsByChapter(moduleId, chapterId)

    suspend fun deleteAnnotationsByModule(moduleId: String) =
        annotationDao.deleteAnnotationsByModule(moduleId)

    suspend fun getAnnotationCount(moduleId: String): Int =
        annotationDao.getAnnotationCount(moduleId)

    fun searchAnnotations(moduleId: String, query: String): Flow<List<AnnotationEntity>> =
        annotationDao.searchAnnotations(moduleId, query)
}

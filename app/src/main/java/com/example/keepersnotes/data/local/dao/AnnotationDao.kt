package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.AnnotationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnotationDao {

    @Query("SELECT * FROM annotations WHERE moduleId = :moduleId ORDER BY createTime DESC")
    fun getAnnotationsByModule(moduleId: String): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations WHERE moduleId = :moduleId AND chapterId = :chapterId ORDER BY startIndex ASC")
    fun getAnnotationsByChapter(moduleId: String, chapterId: String): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations WHERE annotationId = :annotationId")
    suspend fun getAnnotationById(annotationId: String): AnnotationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity)

    @Update
    suspend fun updateAnnotation(annotation: AnnotationEntity)

    @Delete
    suspend fun deleteAnnotation(annotation: AnnotationEntity)

    @Query("DELETE FROM annotations WHERE annotationId = :annotationId")
    suspend fun deleteAnnotationById(annotationId: String)

    @Query("DELETE FROM annotations WHERE moduleId = :moduleId AND chapterId = :chapterId")
    suspend fun deleteAnnotationsByChapter(moduleId: String, chapterId: String)

    @Query("DELETE FROM annotations WHERE moduleId = :moduleId")
    suspend fun deleteAnnotationsByModule(moduleId: String)

    @Query("SELECT COUNT(*) FROM annotations WHERE moduleId = :moduleId")
    suspend fun getAnnotationCount(moduleId: String): Int

    @Query("SELECT * FROM annotations WHERE moduleId = :moduleId AND (selectedText LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%')")
    fun searchAnnotations(moduleId: String, query: String): Flow<List<AnnotationEntity>>
}

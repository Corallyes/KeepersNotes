package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.HighlightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HighlightDao {

    @Query("SELECT * FROM highlights")
    fun getAll(): Flow<List<HighlightEntity>>

    @Query("DELETE FROM highlights")
    suspend fun deleteAll()

    @Query("SELECT * FROM highlights WHERE moduleId = :moduleId ORDER BY createTime DESC")
    fun getHighlightsByModule(moduleId: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE moduleId = :moduleId AND chapterId = :chapterId ORDER BY startIndex ASC")
    fun getHighlightsByChapter(moduleId: String, chapterId: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE moduleId = :moduleId AND nodeId = :nodeId ORDER BY startIndex ASC")
    fun getHighlightsByNode(moduleId: String, nodeId: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE moduleId = :moduleId AND nodeId IS NOT NULL ORDER BY createTime DESC")
    fun getNodeHighlightsByModule(moduleId: String): Flow<List<HighlightEntity>>

    @Query("SELECT * FROM highlights WHERE highlightId = :highlightId")
    suspend fun getHighlightById(highlightId: String): HighlightEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHighlight(highlight: HighlightEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(highlights: List<HighlightEntity>)

    @Update
    suspend fun updateHighlight(highlight: HighlightEntity)

    @Delete
    suspend fun deleteHighlight(highlight: HighlightEntity)

    @Query("DELETE FROM highlights WHERE highlightId = :highlightId")
    suspend fun deleteHighlightById(highlightId: String)

    @Query("DELETE FROM highlights WHERE moduleId = :moduleId AND chapterId = :chapterId")
    suspend fun deleteHighlightsByChapter(moduleId: String, chapterId: String)

    @Query("DELETE FROM highlights WHERE moduleId = :moduleId")
    suspend fun deleteHighlightsByModule(moduleId: String)

    @Query("DELETE FROM highlights WHERE moduleId = :moduleId AND color = :color")
    suspend fun deleteHighlightsByColor(moduleId: String, color: Long)

    @Query("SELECT COUNT(*) FROM highlights WHERE moduleId = :moduleId")
    suspend fun getHighlightCount(moduleId: String): Int

    @Query("SELECT DISTINCT color FROM highlights WHERE moduleId = :moduleId")
    suspend fun getUsedColors(moduleId: String): List<Long>
}

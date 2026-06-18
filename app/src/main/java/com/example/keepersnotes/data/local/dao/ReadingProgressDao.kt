package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {

    @Query("SELECT * FROM reading_progress")
    fun getAll(): Flow<List<ReadingProgressEntity>>

    @Query("DELETE FROM reading_progress")
    suspend fun deleteAll()

    @Query("SELECT * FROM reading_progress WHERE moduleId = :moduleId")
    fun getProgressByModule(moduleId: String): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress ORDER BY lastReadTime DESC LIMIT :limit")
    fun getRecentReads(limit: Int = 10): Flow<List<ReadingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: ReadingProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<ReadingProgressEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotExists(progress: ReadingProgressEntity)

    @Query("UPDATE reading_progress SET lastChapterId = :chapterId, lastReadTime = :time WHERE moduleId = :moduleId")
    suspend fun updateLastChapter(moduleId: String, chapterId: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE reading_progress SET totalReadTimeMinutes = totalReadTimeMinutes + :minutes WHERE moduleId = :moduleId")
    suspend fun addReadTime(moduleId: String, minutes: Int)

    @Query("UPDATE reading_progress SET totalReadTimeSeconds = totalReadTimeSeconds + :seconds WHERE moduleId = :moduleId")
    suspend fun addReadTimeSeconds(moduleId: String, seconds: Long)

    @Query("UPDATE reading_progress SET readCount = readCount + 1 WHERE moduleId = :moduleId")
    suspend fun incrementReadCount(moduleId: String)

    @Query("UPDATE reading_progress SET lastNodeId = :nodeId, lastScrollOffset = :scrollOffset, lastFontSize = :fontSize, lastReadTime = :time WHERE moduleId = :moduleId")
    suspend fun updateLastNodePosition(moduleId: String, nodeId: String, scrollOffset: Int, fontSize: Float, time: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteProgress(progress: ReadingProgressEntity)

    @Query("DELETE FROM reading_progress WHERE moduleId = :moduleId")
    suspend fun deleteProgressByModule(moduleId: String)
}

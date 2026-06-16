package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.ReadingProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingProgressDao {

    @Query("SELECT * FROM reading_progress WHERE moduleId = :moduleId")
    fun getProgressByModule(moduleId: String): Flow<ReadingProgressEntity?>

    @Query("SELECT * FROM reading_progress ORDER BY lastReadTime DESC LIMIT :limit")
    fun getRecentReads(limit: Int = 10): Flow<List<ReadingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: ReadingProgressEntity)

    @Query("UPDATE reading_progress SET lastChapterId = :chapterId, lastReadTime = :time WHERE moduleId = :moduleId")
    suspend fun updateLastChapter(moduleId: String, chapterId: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE reading_progress SET totalReadTimeMinutes = totalReadTimeMinutes + :minutes WHERE moduleId = :moduleId")
    suspend fun addReadTime(moduleId: String, minutes: Int)

    @Query("UPDATE reading_progress SET readCount = readCount + 1 WHERE moduleId = :moduleId")
    suspend fun incrementReadCount(moduleId: String)

    @Delete
    suspend fun deleteProgress(progress: ReadingProgressEntity)

    @Query("DELETE FROM reading_progress WHERE moduleId = :moduleId")
    suspend fun deleteProgressByModule(moduleId: String)
}

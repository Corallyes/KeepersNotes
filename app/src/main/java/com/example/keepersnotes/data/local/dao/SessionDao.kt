package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE groupId = :groupId ORDER BY sessionNumber DESC")
    fun getSessionsByGroupId(groupId: String): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    fun getSessionById(sessionId: String): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE sessionId = :sessionId")
    suspend fun getSessionByIdOnce(sessionId: String): SessionEntity?

    @Query("SELECT MAX(sessionNumber) FROM sessions WHERE groupId = :groupId")
    suspend fun getLatestSessionNumber(groupId: String): Int?

    @Query("SELECT COUNT(*) FROM sessions WHERE date >= :startOfWeek")
    fun getSessionCountSince(startOfWeek: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE sessionId = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}

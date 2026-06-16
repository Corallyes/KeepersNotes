package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.SessionDao
import com.example.keepersnotes.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    fun getSessionsByGroupId(groupId: String): Flow<List<SessionEntity>> =
        sessionDao.getSessionsByGroupId(groupId)

    fun getSessionById(sessionId: String): Flow<SessionEntity?> =
        sessionDao.getSessionById(sessionId)

    suspend fun createSession(
        groupId: String,
        participantPcIds: String = ""
    ): String {
        val latestNumber = sessionDao.getLatestSessionNumber(groupId) ?: 0
        val sessionId = UUID.randomUUID().toString()
        sessionDao.insertSession(
            SessionEntity(
                sessionId = sessionId,
                groupId = groupId,
                sessionNumber = latestNumber + 1,
                participantPcIds = participantPcIds
            )
        )
        return sessionId
    }

    suspend fun updateSession(session: SessionEntity) = sessionDao.updateSession(session)

    suspend fun deleteSession(sessionId: String) = sessionDao.deleteSessionById(sessionId)

    fun getWeeklySessionCount(): Flow<Int> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return sessionDao.getSessionCountSince(calendar.timeInMillis)
    }
}

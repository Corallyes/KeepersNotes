package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.CalendarEventDao
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarEventRepository @Inject constructor(
    private val dao: CalendarEventDao
) {
    fun getEventsBetween(startDate: Long, endDate: Long): Flow<List<CalendarEventEntity>> =
        dao.getEventsBetween(startDate, endDate)

    fun getEventsByDate(date: Long): Flow<List<CalendarEventEntity>> {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = date
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val dayStart = cal.timeInMillis
        cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.add(java.util.Calendar.MILLISECOND, -1)
        val dayEnd = cal.timeInMillis
        return dao.getEventsByDate(dayStart, dayEnd)
    }

    fun getEventsByGroupId(groupId: String): Flow<List<CalendarEventEntity>> =
        dao.getEventsByGroupId(groupId)

    fun getUpcomingEvents(today: Long, limit: Int = 10): Flow<List<CalendarEventEntity>> =
        dao.getUpcomingEvents(today, limit)

    suspend fun create(
        groupId: String,
        title: String,
        date: Long,
        time: String? = null,
        type: String,
        sessionId: String? = null,
        isRemindEnabled: Boolean = true
    ): String {
        val eventId = UUID.randomUUID().toString()
        dao.insert(
            CalendarEventEntity(
                eventId = eventId,
                groupId = groupId,
                title = title,
                date = date,
                time = time,
                type = type,
                sessionId = sessionId,
                isRemindEnabled = isRemindEnabled
            )
        )
        return eventId
    }

    suspend fun insertAll(events: List<CalendarEventEntity>) = dao.insertAll(events)

    suspend fun update(entity: CalendarEventEntity) = dao.update(entity)

    suspend fun deleteById(eventId: String) = dao.deleteById(eventId)

    suspend fun deleteByGroupId(groupId: String) = dao.deleteByGroupId(groupId)
}

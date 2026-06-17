package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    @Query("SELECT * FROM calendar_events WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, time ASC")
    fun getEventsBetween(startDate: Long, endDate: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE date >= :dayStart AND date <= :dayEnd ORDER BY time ASC")
    fun getEventsByDate(dayStart: Long, dayEnd: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE groupId = :groupId ORDER BY date ASC")
    fun getEventsByGroupId(groupId: String): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE date >= :today ORDER BY date ASC, time ASC LIMIT :limit")
    fun getUpcomingEvents(today: Long, limit: Int = 10): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CalendarEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<CalendarEventEntity>)

    @Update
    suspend fun update(entity: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE eventId = :eventId")
    suspend fun deleteById(eventId: String)

    @Query("DELETE FROM calendar_events WHERE groupId = :groupId")
    suspend fun deleteByGroupId(groupId: String)
}

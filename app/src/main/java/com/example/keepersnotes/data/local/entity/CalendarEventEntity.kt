package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_events",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("date")]
)
data class CalendarEventEntity(
    @PrimaryKey
    val eventId: String,
    val groupId: String,
    val title: String,
    val date: Long, // 年月日零点时间戳
    val time: String? = null, // 时:分，如 "16:00"
    val type: String, // session_start, session_end, session, custom
    val sessionId: String? = null,
    val isRemindEnabled: Boolean = true,
    val createTime: Long = System.currentTimeMillis()
)

package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    val sessionId: String,
    val groupId: String,
    val sessionNumber: Int,
    val date: Long = System.currentTimeMillis(),
    val durationMinutes: Int = 0,
    val participantPcIds: String = "", // comma-separated PC IDs
    val summary: String = "", // rich text session summary
    val importantEventsJson: String = "[]", // JSON array of key events
    val cluesFoundJson: String = "[]", // JSON array of clues with follow-up status
    val diceRollsJson: String = "[]", // JSON array of notable dice rolls
    val nextSessionNotes: String = "", // prep notes for next time
    val createTime: Long = System.currentTimeMillis()
)

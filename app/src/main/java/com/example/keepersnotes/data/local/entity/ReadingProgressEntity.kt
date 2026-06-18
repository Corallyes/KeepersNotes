package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reading_progress",
    foreignKeys = [
        ForeignKey(
            entity = ModuleEntity::class,
            parentColumns = ["moduleId"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("moduleId")]
)
data class ReadingProgressEntity(
    @PrimaryKey
    val moduleId: String,
    val lastChapterId: String = "",
    val lastReadTime: Long = System.currentTimeMillis(),
    val totalReadTimeMinutes: Int = 0,
    val readCount: Int = 0,
    val totalReadTimeSeconds: Long = 0,
    val lastNodeId: String = "",
    val lastScrollOffset: Int = 0,
    val lastFontSize: Float = 16f
)

package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "highlights",
    foreignKeys = [
        ForeignKey(
            entity = ModuleEntity::class,
            parentColumns = ["moduleId"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("moduleId"), Index("chapterId")]
)
data class HighlightEntity(
    @PrimaryKey
    val highlightId: String,
    val moduleId: String,
    val chapterId: String,
    val startIndex: Int,
    val endIndex: Int,
    val selectedText: String,
    val color: Long = 0xFFFFEB3B, // 默认黄色
    val createTime: Long = System.currentTimeMillis()
)

package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
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
data class BookmarkEntity(
    @PrimaryKey
    val bookmarkId: String,
    val moduleId: String,
    val chapterId: String,
    val chapterTitle: String = "",
    val selectedText: String = "",
    val note: String = "",
    val color: Long = 0xFFFF9800, // 橙色
    val createTime: Long = System.currentTimeMillis()
)

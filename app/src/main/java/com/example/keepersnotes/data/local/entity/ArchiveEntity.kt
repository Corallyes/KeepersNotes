package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "archives",
    foreignKeys = [
        ForeignKey(
            entity = ModuleEntity::class,
            parentColumns = ["moduleId"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("collectionId")]
)
data class ArchiveEntity(
    @PrimaryKey
    val archiveId: String,
    val collectionId: String,
    val title: String,
    val contentMarkdown: String = "",
    val originalFileName: String = "",
    val fileType: String = "", // "docx", "txt", "md"
    val sortOrder: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

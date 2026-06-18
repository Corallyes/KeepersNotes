package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "annotations",
    foreignKeys = [
        ForeignKey(
            entity = ModuleEntity::class,
            parentColumns = ["moduleId"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("moduleId"), Index("chapterId"), Index("nodeId")]
)
data class AnnotationEntity(
    @PrimaryKey
    val annotationId: String,
    val moduleId: String,
    val chapterId: String = "",
    val nodeId: String? = null,
    val startIndex: Int,
    val endIndex: Int,
    val selectedText: String,
    val note: String,
    val color: Long = 0xFF4CAF50, // 默认绿色
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis()
)

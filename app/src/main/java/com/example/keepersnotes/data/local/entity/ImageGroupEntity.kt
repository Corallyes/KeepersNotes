package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "image_groups",
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
data class ImageGroupEntity(
    @PrimaryKey
    val imageGroupId: String,
    val collectionId: String,
    val name: String,
    val description: String = "",
    val sortOrder: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "images",
    foreignKeys = [
        ForeignKey(
            entity = ModuleEntity::class,
            parentColumns = ["moduleId"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ImageGroupEntity::class,
            parentColumns = ["imageGroupId"],
            childColumns = ["imageGroupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("collectionId"), Index("imageGroupId")]
)
data class ImageEntity(
    @PrimaryKey
    val imageId: String,
    val collectionId: String,
    val title: String = "",
    val filePath: String,
    val originalFileName: String = "",
    val imageGroupId: String? = null,
    val sortOrder: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

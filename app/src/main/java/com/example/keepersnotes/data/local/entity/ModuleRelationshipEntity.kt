package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "module_relationships",
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
data class ModuleRelationshipEntity(
    @PrimaryKey
    val id: String,
    val moduleId: String,
    val sourceId: String,
    val sourceType: String,
    val targetId: String,
    val targetType: String,
    val relationType: String = "",
    val description: String = "",
    val createTime: Long = System.currentTimeMillis()
)

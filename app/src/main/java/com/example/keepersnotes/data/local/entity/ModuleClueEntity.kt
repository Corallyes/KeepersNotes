package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "module_clues",
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
data class ModuleClueEntity(
    @PrimaryKey
    val id: String,
    val moduleId: String,
    val name: String,
    val type: String = "",
    val description: String = "",
    val source: String = "",
    val isHidden: Boolean = false,
    val sortOrder: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "module_default_npcs",
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
data class ModuleDefaultNpcEntity(
    @PrimaryKey
    val id: String,
    val moduleId: String,
    val name: String,
    val alias: String = "",
    val occupation: String = "",
    val description: String = "",
    val truePurpose: String = "",
    val relationshipToPc: String = "",
    val status: String = "alive",
    val portraitUri: String? = null,
    val sortOrder: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

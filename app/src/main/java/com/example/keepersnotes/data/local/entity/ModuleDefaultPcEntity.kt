package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "module_default_pcs",
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
data class ModuleDefaultPcEntity(
    @PrimaryKey
    val id: String,
    val moduleId: String,
    val name: String,
    val playerName: String = "",
    val system: String = "",
    val description: String = "",
    val attributesJson: String = "{}",
    val portraitUri: String? = null,
    val sortOrder: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

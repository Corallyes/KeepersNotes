package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "module_organizations",
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
data class ModuleOrganizationEntity(
    @PrimaryKey
    val id: String,
    val moduleId: String,
    val name: String,
    val type: String = "",
    val description: String = "",
    val members: String = "",
    val goals: String = "",
    val sortOrder: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

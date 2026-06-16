package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "modules")
data class ModuleEntity(
    @PrimaryKey
    val moduleId: String,
    val title: String,
    val author: String = "",
    val system: String = "", // COC7, DND5e, etc.
    val difficulty: String = "", // easy, medium, hard
    val playerCount: String = "", // e.g. "3-5"
    val duration: String = "", // e.g. "4-6h"
    val synopsis: String = "",
    val tags: String = "", // comma-separated
    val contentJson: String = "{}", // structured content as JSON
    val coverImageUri: String? = null,
    val isFavorite: Boolean = false,
    val isUserCreated: Boolean = false,
    val isCollection: Boolean = false, // true = ZIP导入的卷宗，false = 单文件导入
    val createTime: Long = System.currentTimeMillis()
)

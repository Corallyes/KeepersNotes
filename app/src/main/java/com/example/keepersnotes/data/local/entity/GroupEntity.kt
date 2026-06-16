package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val groupId: String,
    val groupName: String,
    val moduleName: String,
    val system: String = "", // COC7, DND5e, etc.
    val status: String, // active, paused, completed
    val currentSession: Int = 0,
    val createTime: Long = System.currentTimeMillis(),
    val lastPlayTime: Long? = null,
    val nextPlayTime: Long? = null,
    val moduleId: String? = null,
    val coverImageUri: String? = null,
    val notes: String = "",
    val gameFormat: String = "", // 线上/线下/线上线下
    val scale: String = "", // 规模，如 "3-5人"
    val startTime: Long? = null, // 开团时间
    val expectedEndTime: Long? = null, // 预计结束时间
    val defaultSessionTime: String = "" // 默认开团时间，如 "16:00"
)

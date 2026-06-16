package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "group_relationships",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId")]
)
data class GroupRelationshipEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    val sourceId: String,
    val sourceType: String, // pc, npc
    val targetId: String,
    val targetType: String, // pc, npc
    val relationType: String = "",
    val description: String = "",
    val createTime: Long = System.currentTimeMillis()
)

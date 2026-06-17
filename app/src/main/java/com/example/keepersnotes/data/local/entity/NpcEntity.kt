package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "npcs")
data class NpcEntity(
    @PrimaryKey
    val npcId: String,
    val groupId: String,
    val name: String,
    val alias: String = "",
    val occupation: String = "",
    val description: String = "",
    val truePurpose: String = "", // KP-only hidden info
    val relationshipToPc: String = "",
    val status: String = "alive", // alive, dead, missing, unknown
    val gender: String = "",  // male, female, alien, other
    val firstAppearance: String = "",
    val kpNotes: String = "",
    val portraitUri: String? = null,
    val createTime: Long = System.currentTimeMillis()
)

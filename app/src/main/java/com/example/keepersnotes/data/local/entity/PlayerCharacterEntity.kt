package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_characters")
data class PlayerCharacterEntity(
    @PrimaryKey
    val pcId: String,
    val groupId: String,
    val playerName: String,
    val characterName: String,
    val system: String, // COC7, DND5e, etc.
    val hpCurrent: Int = 0,
    val hpMax: Int = 0,
    val sanCurrent: Int = 0,
    val sanMax: Int = 0,
    val luck: Int = 0,
    val attributesJson: String = "{}", // JSON blob for full attributes
    val skillsJson: String = "{}",      // JSON blob for skills
    val background: String = "",
    val inventoryJson: String = "[]",   // JSON blob for inventory
    val kpNotes: String = "",
    val status: String = "normal", // normal, wounded, insane, dead
    val gender: String = "",  // male, female, alien, other
    val portraitUri: String? = null,
    val createTime: Long = System.currentTimeMillis()
)

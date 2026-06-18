package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "document_nodes",
    foreignKeys = [
        ForeignKey(
            entity = ModuleEntity::class,
            parentColumns = ["moduleId"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("moduleId"),
        Index("moduleId", "order")
    ]
)
data class DocumentNodeEntity(
    @PrimaryKey
    val nodeId: String,
    val moduleId: String,
    val type: String,           // heading / paragraph / table / image / quote / list_item
    val level: Int = 0,         // heading level (1-6) or list nesting depth
    val content: String = "",   // text content
    val tableData: String? = null, // JSON 2D array: [[row1col1, row1col2], ...]
    val imageUri: String? = null,  // image file path
    val order: Int = 0          // document order
)

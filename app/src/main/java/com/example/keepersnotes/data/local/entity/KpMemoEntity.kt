package com.example.keepersnotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "kp_memos")
data class KpMemoEntity(
    @PrimaryKey
    val memoId: String,
    val groupId: String,
    val type: String, // "clue", "plot", "todo", "reminder", "rule", "general"
    val title: String = "",
    val content: String = "",
    val isHidden: Boolean = false, // hidden notes (dark clues, true purposes)
    val isCompleted: Boolean = false, // for checklist items
    val priority: Int = 0, // 0=normal, 1=important, 2=urgent
    val tags: String = "", // comma-separated
    val moduleId: String? = null, // 关联的模组ID
    val chapterId: String? = null, // 关联的章节ID
    val chapterTitle: String = "", // 关联的章节标题
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis()
)

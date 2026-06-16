package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.*
import kotlinx.coroutines.flow.Flow

data class SearchResult(
    val type: String, // "module", "memo", "highlight", "annotation", "bookmark"
    val id: String,
    val title: String,
    val content: String,
    val matchedText: String,
    val moduleId: String? = null,
    val chapterId: String? = null,
    val timestamp: Long = 0
)

@Dao
interface SearchDao {

    @Query("""
        SELECT 'module' as type, moduleId as id, title, synopsis as content, title as matchedText,
               moduleId, '' as chapterId, createTime as timestamp
        FROM modules
        WHERE title LIKE '%' || :query || '%'
           OR synopsis LIKE '%' || :query || '%'
           OR author LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
    """)
    fun searchModules(query: String): Flow<List<SearchResult>>

    @Query("""
        SELECT 'memo' as type, memoId as id, title, content, title as matchedText,
               '' as moduleId, '' as chapterId, createTime as timestamp
        FROM kp_memos
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
    """)
    fun searchMemos(query: String): Flow<List<SearchResult>>

    @Query("""
        SELECT 'highlight' as type, highlightId as id,
               selectedText as title, selectedText as content, selectedText as matchedText,
               moduleId, chapterId, createTime as timestamp
        FROM highlights
        WHERE selectedText LIKE '%' || :query || '%'
    """)
    fun searchHighlights(query: String): Flow<List<SearchResult>>

    @Query("""
        SELECT 'annotation' as type, annotationId as id,
               selectedText as title, note as content,
               CASE
                   WHEN selectedText LIKE '%' || :query || '%' THEN selectedText
                   ELSE note
               END as matchedText,
               moduleId, chapterId, createTime as timestamp
        FROM annotations
        WHERE selectedText LIKE '%' || :query || '%'
           OR note LIKE '%' || :query || '%'
    """)
    fun searchAnnotations(query: String): Flow<List<SearchResult>>

    @Query("""
        SELECT 'bookmark' as type, bookmarkId as id,
               chapterTitle as title, note as content,
               CASE
                   WHEN chapterTitle LIKE '%' || :query || '%' THEN chapterTitle
                   WHEN selectedText LIKE '%' || :query || '%' THEN selectedText
                   ELSE note
               END as matchedText,
               moduleId, chapterId, createTime as timestamp
        FROM bookmarks
        WHERE chapterTitle LIKE '%' || :query || '%'
           OR selectedText LIKE '%' || :query || '%'
           OR note LIKE '%' || :query || '%'
    """)
    fun searchBookmarks(query: String): Flow<List<SearchResult>>
}

package com.example.keepersnotes.data.local.dao

import androidx.room.*
import com.example.keepersnotes.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Query("SELECT * FROM bookmarks WHERE moduleId = :moduleId ORDER BY createTime DESC")
    fun getBookmarksByModule(moduleId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE moduleId = :moduleId AND chapterId = :chapterId ORDER BY createTime DESC")
    fun getBookmarksByChapter(moduleId: String, chapterId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookmarkId = :bookmarkId")
    suspend fun getBookmarkById(bookmarkId: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE bookmarkId = :bookmarkId")
    suspend fun deleteBookmarkById(bookmarkId: String)

    @Query("DELETE FROM bookmarks WHERE moduleId = :moduleId")
    suspend fun deleteBookmarksByModule(moduleId: String)

    @Query("SELECT COUNT(*) FROM bookmarks WHERE moduleId = :moduleId")
    suspend fun getBookmarkCount(moduleId: String): Int
}

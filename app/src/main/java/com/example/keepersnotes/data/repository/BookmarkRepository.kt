package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.BookmarkDao
import com.example.keepersnotes.data.local.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {

    fun getBookmarksByModule(moduleId: String): Flow<List<BookmarkEntity>> =
        bookmarkDao.getBookmarksByModule(moduleId)

    fun getBookmarksByChapter(moduleId: String, chapterId: String): Flow<List<BookmarkEntity>> =
        bookmarkDao.getBookmarksByChapter(moduleId, chapterId)

    suspend fun getBookmarkById(bookmarkId: String): BookmarkEntity? =
        bookmarkDao.getBookmarkById(bookmarkId)

    suspend fun addBookmark(
        moduleId: String,
        chapterId: String,
        chapterTitle: String,
        selectedText: String = "",
        note: String = "",
        color: Long = 0xFFFF9800
    ): BookmarkEntity {
        val bookmark = BookmarkEntity(
            bookmarkId = UUID.randomUUID().toString(),
            moduleId = moduleId,
            chapterId = chapterId,
            chapterTitle = chapterTitle,
            selectedText = selectedText,
            note = note,
            color = color
        )
        bookmarkDao.insertBookmark(bookmark)
        return bookmark
    }

    suspend fun updateBookmark(bookmark: BookmarkEntity) =
        bookmarkDao.updateBookmark(bookmark)

    suspend fun deleteBookmark(bookmark: BookmarkEntity) =
        bookmarkDao.deleteBookmark(bookmark)

    suspend fun deleteBookmarkById(bookmarkId: String) =
        bookmarkDao.deleteBookmarkById(bookmarkId)

    suspend fun deleteBookmarksByModule(moduleId: String) =
        bookmarkDao.deleteBookmarksByModule(moduleId)

    suspend fun getBookmarkCount(moduleId: String): Int =
        bookmarkDao.getBookmarkCount(moduleId)
}

package com.example.keepersnotes.data.repository

import com.example.keepersnotes.data.local.dao.SearchDao
import com.example.keepersnotes.data.local.dao.SearchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val searchDao: SearchDao
) {

    fun searchAll(query: String): Flow<List<SearchResult>> {
        if (query.isBlank()) return kotlinx.coroutines.flow.flowOf(emptyList())

        return combine(
            searchDao.searchModules(query),
            searchDao.searchMemos(query),
            searchDao.searchHighlights(query),
            searchDao.searchAnnotations(query),
            searchDao.searchBookmarks(query)
        ) { results ->
            results.flatMap { it }
                .sortedByDescending { it.timestamp }
        }
    }

    fun searchModules(query: String): Flow<List<SearchResult>> =
        searchDao.searchModules(query)

    fun searchMemos(query: String): Flow<List<SearchResult>> =
        searchDao.searchMemos(query)

    fun searchHighlights(query: String): Flow<List<SearchResult>> =
        searchDao.searchHighlights(query)

    fun searchAnnotations(query: String): Flow<List<SearchResult>> =
        searchDao.searchAnnotations(query)

    fun searchBookmarks(query: String): Flow<List<SearchResult>> =
        searchDao.searchBookmarks(query)
}

package com.example.cross_platformfilemanager.data.repository.sqldelight

import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.mapper.toModel
import com.example.cross_platformfilemanager.data.model.TaggoRecentSearch
import com.example.cross_platformfilemanager.data.repository.SearchHistoryRepository

class SqlDelightSearchHistoryRepository(
    private val database: TaggoDatabase,
) : SearchHistoryRepository {
    private val queries = database.taggoDatabaseQueries

    override suspend fun getRecentSearches(): List<TaggoRecentSearch> =
        queries.selectRecentSearches().executeAsList().map { it.toModel() }

    override suspend fun upsertRecentSearch(search: TaggoRecentSearch) {
        queries.upsertRecentSearch(
            search.id,
            search.rawQuery,
            search.normalizedQuery,
            search.createdAtMs,
            search.lastUsedAtMs,
            search.useCount,
        )
    }

    override suspend fun deleteRecentSearch(id: String) {
        queries.deleteRecentSearch(id)
    }
}

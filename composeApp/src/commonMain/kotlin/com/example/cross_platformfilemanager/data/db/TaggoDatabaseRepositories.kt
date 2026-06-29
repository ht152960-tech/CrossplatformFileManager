package com.example.cross_platformfilemanager.data.db

import com.example.cross_platformfilemanager.data.repository.BehaviorRepository
import com.example.cross_platformfilemanager.data.repository.FileEntryRepository
import com.example.cross_platformfilemanager.data.repository.RecommendationRecordRepository
import com.example.cross_platformfilemanager.data.repository.RecommendationPolicyRepository
import com.example.cross_platformfilemanager.data.repository.SearchHistoryRepository
import com.example.cross_platformfilemanager.data.repository.TagRepository
import com.example.cross_platformfilemanager.data.repository.sqldelight.SqlDelightBehaviorRepository
import com.example.cross_platformfilemanager.data.repository.sqldelight.SqlDelightFileEntryRepository
import com.example.cross_platformfilemanager.data.repository.sqldelight.SqlDelightRecommendationRecordRepository
import com.example.cross_platformfilemanager.data.repository.sqldelight.SqlDelightRecommendationPolicyRepository
import com.example.cross_platformfilemanager.data.repository.sqldelight.SqlDelightSearchHistoryRepository
import com.example.cross_platformfilemanager.data.repository.sqldelight.SqlDelightTagRepository

data class TaggoDatabaseRepositories(
    val fileEntries: FileEntryRepository,
    val tags: TagRepository,
    val searchHistory: SearchHistoryRepository,
    val behavior: BehaviorRepository,
    val recommendations: RecommendationRecordRepository,
    val recommendationPolicy: RecommendationPolicyRepository,
)

fun createTaggoDatabaseRepositories(
    database: TaggoDatabase,
): TaggoDatabaseRepositories = TaggoDatabaseRepositories(
    fileEntries = SqlDelightFileEntryRepository(database),
    tags = SqlDelightTagRepository(database),
    searchHistory = SqlDelightSearchHistoryRepository(database),
    behavior = SqlDelightBehaviorRepository(database),
    recommendations = SqlDelightRecommendationRecordRepository(database),
    recommendationPolicy = SqlDelightRecommendationPolicyRepository(database),
)

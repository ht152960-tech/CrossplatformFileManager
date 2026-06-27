package com.example.cross_platformfilemanager.data.adapter

interface TaggoIdGenerator {
    fun nextFileEntryId(): String

    fun nextFileReferenceId(): String

    fun nextTagId(normalizedName: String): String

    fun nextRecentSearchId(normalizedQuery: String): String

    fun nextBehaviorSessionId(): String

    fun nextBehaviorEventId(): String

    fun nextExplicitNeedSignalId(): String

    fun nextRecommendationContextId(): String

    fun nextRecommendationSetId(): String

    fun nextRecommendationFeedbackId(): String
}

interface TaggoClock {
    fun nowMs(): Long
}

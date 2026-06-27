package com.example.cross_platformfilemanager.data.adapter

import kotlin.random.Random
import kotlin.time.Clock

object SystemTaggoClock : TaggoClock {
    override fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()
}

object DefaultTaggoIdGenerator : TaggoIdGenerator {
    override fun nextFileEntryId(): String = nextId("file")

    override fun nextFileReferenceId(): String = nextId("ref")

    override fun nextTagId(normalizedName: String): String = nextId("tag")

    override fun nextRecentSearchId(normalizedQuery: String): String = nextId("search")

    override fun nextBehaviorSessionId(): String = nextId("session")

    override fun nextBehaviorEventId(): String = nextId("event")

    override fun nextExplicitNeedSignalId(): String = nextId("signal")

    override fun nextRecommendationContextId(): String = nextId("context")

    override fun nextRecommendationSetId(): String = nextId("set")

    override fun nextRecommendationFeedbackId(): String = nextId("feedback")

    private fun nextId(prefix: String): String =
        "${prefix}_${Clock.System.now().toEpochMilliseconds()}_${Random.nextLong(0, Long.MAX_VALUE)}"
}

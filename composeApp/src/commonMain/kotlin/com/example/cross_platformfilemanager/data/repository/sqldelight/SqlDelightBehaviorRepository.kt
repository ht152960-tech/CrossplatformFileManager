package com.example.cross_platformfilemanager.data.repository.sqldelight

import com.example.cross_platformfilemanager.data.db.TaggoDatabase
import com.example.cross_platformfilemanager.data.mapper.toModel
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorEvent
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorSession
import com.example.cross_platformfilemanager.data.model.TaggoExplicitNeedSignal
import com.example.cross_platformfilemanager.data.repository.BehaviorRepository

class SqlDelightBehaviorRepository(
    private val database: TaggoDatabase,
) : BehaviorRepository {
    private val queries = database.taggoDatabaseQueries
    private val historyQueries = database.recommendationHistoryQueriesQueries

    override suspend fun startSession(session: TaggoBehaviorSession) {
        queries.insertBehaviorSession(
            session.id,
            session.startedAtMs,
            session.endedAtMs,
            session.platform,
            session.appVersion,
            session.databaseVersion,
            session.recommendationModelVersion,
        )
    }

    override suspend fun endSession(id: String, endedAtMs: Long) {
        queries.endBehaviorSession(endedAtMs, id)
    }

    override suspend fun getSessionById(id: String): TaggoBehaviorSession? =
        queries.selectBehaviorSessionById(id).executeAsOneOrNull()?.toModel()

    override suspend fun recordEvent(event: TaggoBehaviorEvent) {
        queries.insertBehaviorEvent(
            event.id,
            event.sessionId,
            event.occurredAtMs,
            event.eventType,
            event.screenName,
            event.entryPoint,
            event.fileId,
            event.fileReferenceId,
            event.searchQuery,
            event.recommendationSetId,
            event.recommendationRank,
            event.durationMs,
            event.extraJson,
        )
    }

    override suspend fun getEventsForSession(sessionId: String): List<TaggoBehaviorEvent> =
        queries.selectBehaviorEventsForSession(sessionId).executeAsList().map { it.toModel() }

    override suspend fun getEventsByTypeInRange(
        eventType: String,
        fromMs: Long,
        toMs: Long,
    ): List<TaggoBehaviorEvent> =
        historyQueries.selectBehaviorEventsByTypeInRange(eventType, fromMs, toMs)
            .executeAsList()
            .map { it.toModel() }

    override suspend fun recordExplicitNeedSignal(signal: TaggoExplicitNeedSignal) {
        queries.insertExplicitNeedSignal(
            signal.id,
            signal.sessionId,
            signal.behaviorEventId,
            signal.signalType,
            signal.signalValue,
            signal.createdAtMs,
            signal.consumedByRecommendationSetId,
        )
    }

    override suspend fun getExplicitNeedSignalsForSession(
        sessionId: String,
    ): List<TaggoExplicitNeedSignal> =
        queries.selectExplicitNeedSignalsForSession(sessionId).executeAsList().map { it.toModel() }
}

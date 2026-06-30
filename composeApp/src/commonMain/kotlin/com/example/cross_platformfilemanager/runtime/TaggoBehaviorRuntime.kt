package com.example.cross_platformfilemanager.runtime

import com.example.cross_platformfilemanager.data.adapter.TaggoClock
import com.example.cross_platformfilemanager.data.adapter.TaggoIdGenerator
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorEvent
import com.example.cross_platformfilemanager.data.model.TaggoBehaviorSession
import com.example.cross_platformfilemanager.data.repository.BehaviorRepository
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.coroutines.cancellation.CancellationException

class TaggoBehaviorRuntime(
    private val behaviorRepository: BehaviorRepository,
    private val idGenerator: TaggoIdGenerator,
    private val clock: TaggoClock,
    private val platform: String,
    private val appVersion: String?,
    private val databaseVersion: Long,
    private val recommendationModelVersion: Long,
) {
    private var currentSessionId: String? = null
    private var currentSessionStartedAtMs: Long? = null
    private var currentSessionEndedAtMs: Long? = null

    val sessionId: String?
        get() = currentSessionId

    suspend fun startSessionIfNeeded() {
        if (currentSessionId != null) return
        val startedAtMs = clock.nowMs()
        val sessionId = idGenerator.nextBehaviorSessionId()
        safeCall {
            behaviorRepository.startSession(
                TaggoBehaviorSession(
                    id = sessionId,
                    startedAtMs = startedAtMs,
                    endedAtMs = null,
                    platform = platform,
                    appVersion = appVersion,
                    databaseVersion = databaseVersion,
                    recommendationModelVersion = recommendationModelVersion,
                ),
            )
            currentSessionId = sessionId
            currentSessionStartedAtMs = startedAtMs
            currentSessionEndedAtMs = null
            recordEventInternal(
                eventType = EVENT_SESSION_START,
                screenName = "app",
                entryPoint = "app_launch",
            )
        }
    }

    suspend fun endSession() {
        val sessionId = currentSessionId ?: return
        if (currentSessionEndedAtMs != null) return
        val endedAtMs = clock.nowMs()
        safeCall {
            behaviorRepository.endSession(sessionId, endedAtMs)
            currentSessionEndedAtMs = endedAtMs
            recordEventInternal(
                eventType = EVENT_SESSION_END,
                screenName = "app",
                entryPoint = "app_close",
                durationMs = currentSessionStartedAtMs?.let { (endedAtMs - it).coerceAtLeast(0L) },
            )
        }
    }

    suspend fun recordEvent(
        eventType: String,
        screenName: String? = null,
        entryPoint: String? = null,
        fileId: String? = null,
        fileReferenceId: String? = null,
        searchQuery: String? = null,
        recommendationSetId: String? = null,
        recommendationRank: Long? = null,
        durationMs: Long? = null,
        extraJson: String? = null,
    ) {
        startSessionIfNeeded()
        safeCall {
            recordEventInternal(
                eventType = eventType,
                screenName = screenName,
                entryPoint = entryPoint,
                fileId = fileId,
                fileReferenceId = fileReferenceId,
                searchQuery = searchQuery,
                recommendationSetId = recommendationSetId,
                recommendationRank = recommendationRank,
                durationMs = durationMs,
                extraJson = extraJson,
            )
        }
    }

    suspend fun recordFileAdded(fileId: String, entryPoint: String? = null) {
        recordEvent(
            eventType = EVENT_FILE_ADD,
            fileId = fileId,
            entryPoint = entryPoint,
        )
    }

    suspend fun recordFileDeleted(fileId: String, entryPoint: String? = null) {
        recordEvent(
            eventType = EVENT_FILE_DELETE,
            fileId = fileId,
            entryPoint = entryPoint,
        )
    }

    suspend fun recordTagAdded(fileId: String, tagName: String) {
        recordEvent(
            eventType = EVENT_TAG_ADD,
            fileId = fileId,
            extraJson = buildExtraJson("tagName" to tagName),
        )
    }

    suspend fun recordTagRemoved(fileId: String, tagName: String) {
        recordEvent(
            eventType = EVENT_TAG_REMOVE,
            fileId = fileId,
            extraJson = buildExtraJson("tagName" to tagName),
        )
    }

    suspend fun recordSearchSubmit(rawQuery: String, screenName: String? = "search") {
        recordEvent(
            eventType = EVENT_SEARCH_SUBMIT,
            screenName = screenName,
            searchQuery = rawQuery,
            entryPoint = "search_bar",
        )
    }

    suspend fun recordViewDetail(fileId: String, entryPoint: String? = null, screenName: String? = "detail") {
        recordEvent(
            eventType = EVENT_VIEW_DETAIL,
            screenName = screenName,
            entryPoint = entryPoint,
            fileId = fileId,
        )
    }

    suspend fun recordOpenContent(
        fileId: String,
        fileReferenceId: String? = null,
        entryPoint: String? = null,
        screenName: String? = "detail",
    ) {
        recordEvent(
            eventType = EVENT_OPEN_CONTENT,
            screenName = screenName,
            entryPoint = entryPoint,
            fileId = fileId,
            fileReferenceId = fileReferenceId,
        )
    }

    suspend fun recordOpenFailed(
        fileId: String? = null,
        fileReferenceId: String? = null,
        errorMessage: String? = null,
        entryPoint: String? = null,
        screenName: String? = null,
    ) {
        recordEvent(
            eventType = EVENT_OPEN_FAILED,
            screenName = screenName,
            entryPoint = entryPoint,
            fileId = fileId,
            fileReferenceId = fileReferenceId,
            extraJson = errorMessage?.let { buildExtraJson("errorMessage" to it) },
        )
    }

    private suspend fun recordEventInternal(
        eventType: String,
        screenName: String?,
        entryPoint: String?,
        fileId: String? = null,
        fileReferenceId: String? = null,
        searchQuery: String? = null,
        recommendationSetId: String? = null,
        recommendationRank: Long? = null,
        durationMs: Long? = null,
        extraJson: String? = null,
    ) {
        val sessionId = currentSessionId
        val occurredAtMs = clock.nowMs()
        behaviorRepository.recordEvent(
            TaggoBehaviorEvent(
                id = idGenerator.nextBehaviorEventId(),
                sessionId = sessionId,
                occurredAtMs = occurredAtMs,
                eventType = eventType,
                screenName = screenName,
                entryPoint = entryPoint,
                fileId = fileId,
                fileReferenceId = fileReferenceId,
                searchQuery = searchQuery,
                recommendationSetId = recommendationSetId,
                recommendationRank = recommendationRank,
                durationMs = durationMs,
                extraJson = extraJson,
            ),
        )
    }

    private suspend inline fun safeCall(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Throwable) {
            // 行为日志是附属记录，失败时静默降级，不影响主流程。
        }
    }

    private fun buildExtraJson(vararg pairs: Pair<String, String?>): String {
        val jsonObject: JsonObject = buildJsonObject {
            pairs.forEach { (key, value) ->
                if (!value.isNullOrBlank()) {
                    put(key, value)
                }
            }
        }
        return jsonObject.toString()
    }

    private companion object {
        const val EVENT_SESSION_START = "session_start"
        const val EVENT_SESSION_END = "session_end"
        const val EVENT_FILE_ADD = "file_add"
        const val EVENT_FILE_DELETE = "file_delete"
        const val EVENT_TAG_ADD = "tag_add"
        const val EVENT_TAG_REMOVE = "tag_remove"
        const val EVENT_SEARCH_SUBMIT = "search_submit"
        const val EVENT_VIEW_DETAIL = "view_detail"
        const val EVENT_OPEN_CONTENT = "open_content"
        const val EVENT_OPEN_FAILED = "open_failed"
    }
}

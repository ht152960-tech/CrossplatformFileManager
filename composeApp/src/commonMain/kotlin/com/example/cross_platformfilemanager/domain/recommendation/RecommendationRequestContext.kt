package com.example.cross_platformfilemanager.domain.recommendation

class RecommendationRequestContext(
    private val pathConfig: AfterOpenPathConfig = AfterOpenPathConfig(),
) {
    private val recentOpens = ArrayDeque<RecentOpen>()

    fun recordOpenContent(
        fileId: String,
        occurredAtMs: Long = 0L,
        sessionId: String? = null,
    ) {
        val normalizedFileId = fileId.takeIf { it.isNotBlank() } ?: return
        val previous = recentOpens.lastOrNull()
        val sessionChanged = previous != null &&
            (previous.sessionId != sessionId) &&
            (previous.sessionId != null || sessionId != null)
        val gapExceeded = previous != null &&
            occurredAtMs >= previous.occurredAtMs &&
            occurredAtMs - previous.occurredAtMs > pathConfig.maxGapMs
        if (sessionChanged || gapExceeded) {
            recentOpens.clear()
        }
        recentOpens.addLast(RecentOpen(normalizedFileId, occurredAtMs, sessionId))
        while (recentOpens.size > pathConfig.maxContextSize) {
            recentOpens.removeFirst()
        }
    }

    fun createRequest(
        nowMs: Long,
        limit: Int,
        sessionId: String? = recentOpens.lastOrNull()?.sessionId,
    ): RecommendationRequest {
        val triggerFileId = recentOpens.lastOrNull()?.fileId
        return RecommendationRequest(
            mode = if (triggerFileId == null) {
                RecommendationMode.HOME_INITIAL
            } else {
                RecommendationMode.AFTER_OPEN
            },
            nowMs = nowMs,
            limit = limit,
            triggerFileId = triggerFileId,
            sessionId = sessionId,
            recentOpenFileIds = recentOpens.map { it.fileId },
        )
    }

    private data class RecentOpen(
        val fileId: String,
        val occurredAtMs: Long,
        val sessionId: String?,
    )
}

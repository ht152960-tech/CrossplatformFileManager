package com.example.cross_platformfilemanager.domain.recommendation

class RecommendationRequestContext {
    private var lastSuccessfulOpenFileId: String? = null

    fun recordOpenContent(fileId: String) {
        lastSuccessfulOpenFileId = fileId.takeIf { it.isNotBlank() }
    }

    fun createRequest(
        nowMs: Long,
        limit: Int,
        sessionId: String? = null,
    ): RecommendationRequest {
        val triggerFileId = lastSuccessfulOpenFileId
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
        )
    }
}

package com.example.cross_platformfilemanager

/**
 * 本地开发和 UI 验收专用配置。
 */
internal object TaggoDebugConfig {
    // 仅用于本地 UI 验收，发布前必须关闭。
    const val allowImmediateRecommendationsForUiTest = true
}

internal const val RECENT_UPLOAD_RECOMMENDATION_DELAY_HOURS = 24L
private const val MILLIS_PER_HOUR = 60L * 60L * 1000L
private const val RECENT_UPLOAD_RECOMMENDATION_DELAY_MILLIS =
    RECENT_UPLOAD_RECOMMENDATION_DELAY_HOURS * MILLIS_PER_HOUR

internal fun isEligibleForRecommendation(
    file: FileReference,
    nowMillis: Long,
    allowImmediateRecommendations: Boolean =
        TaggoDebugConfig.allowImmediateRecommendationsForUiTest,
): Boolean {
    if (allowImmediateRecommendations) {
        return true
    }

    return nowMillis - file.createdAtMillis >= RECENT_UPLOAD_RECOMMENDATION_DELAY_MILLIS
}

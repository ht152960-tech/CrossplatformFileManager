package com.example.cross_platformfilemanager

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 应用快照 JSON 编解码器。
 *
 * 推荐日志和推荐引擎状态也通过这里持久化，因此这是推荐学习结果跨会话保留的关键边界。
 */
internal object SnapshotCodec {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = true
    }

    fun encode(snapshot: AppSnapshot): String = json.encodeToString(snapshot)

    fun decode(payload: String): AppSnapshot? {
        if (payload.isBlank()) return null
        return runCatching {
            json.decodeFromString<AppSnapshot>(payload)
        }.getOrNull()
    }
}

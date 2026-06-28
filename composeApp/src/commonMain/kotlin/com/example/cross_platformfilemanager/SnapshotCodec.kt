package com.example.cross_platformfilemanager

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 应用快照 JSON 编解码器。
 *
 * 这里只负责 UI 临时状态快照的编码/解码。
 * 文件、标签、最近搜索和推荐学习状态不再通过 JSON snapshot 持久化；
 * 这些业务事实源现在由 SQLDelight DB 提供。
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

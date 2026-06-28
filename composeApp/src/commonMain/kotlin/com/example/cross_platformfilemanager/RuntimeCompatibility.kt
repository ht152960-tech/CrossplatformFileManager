package com.example.cross_platformfilemanager

import com.example.cross_platformfilemanager.runtime.TaggoRuntimeFile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Temporary source compatibility for the unchanged recommendation and platform APIs.
 * The aliased value is the clean database-backed runtime model.
 */
typealias FileReference = TaggoRuntimeFile

@Serializable
enum class FileSourceKind {
    @SerialName("ManualPath")
    ManualPath,
    @SerialName("BrowserHandle")
    BrowserHandle,
    @SerialName("Url")
    Url,
    @SerialName("RemoteReference")
    RemoteReference,
}

data class SearchResult(
    val reference: TaggoRuntimeFile,
    val score: Double,
    val reason: String,
    val matchedTagCount: Int = 0,
) {
    val scoreLabel: String get() = "Score ${(score * 100).toInt()}"
}

data class Suggestion(
    val label: String,
    val reason: String,
    val kind: SuggestionKind,
    val score: Double,
)

enum class SuggestionKind {
    Query,
    Tag,
    File,
}

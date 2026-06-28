package com.example.cross_platformfilemanager.runtime

import com.example.cross_platformfilemanager.FileSourceKind
import com.example.cross_platformfilemanager.ThumbnailStatus

data class TaggoRuntimeFile(
    val id: String,
    val displayName: String,
    val extension: String?,
    val mimeType: String?,
    val taggoFileCategory: String,
    val sizeBytes: Long?,
    val primaryReferenceId: String,
    val referenceType: String,
    val referenceValue: String,
    val referenceAvailable: Boolean,
    val platform: String?,
    val tags: List<String>,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val lastContentOpenedAtMs: Long?,
    val contentOpenCount: Long,
    val thumbnailState: String,
    val thumbnailReferenceValue: String?,
) {
    val title: String get() = displayName
    val source: String get() = referenceValue
    val sourceKind: FileSourceKind
        get() = when (referenceType.lowercase()) {
            "browser_handle", "browser-handle", "web_file_handle", "web_file_token" ->
                FileSourceKind.BrowserHandle
            "url", "remote_url" -> FileSourceKind.Url
            "remote_reference", "remote-reference", "external_uri" -> FileSourceKind.RemoteReference
            else -> FileSourceKind.ManualPath
        }
    val fileType: String get() = taggoFileCategory
    val fileSizeBytes: Long? get() = sizeBytes
    val thumbnailPath: String? get() = thumbnailReferenceValue
    val thumbnailStatus: ThumbnailStatus
        get() = when (thumbnailState.lowercase()) {
            "generating" -> ThumbnailStatus.GENERATING
            "ready" -> ThumbnailStatus.READY
            "failed" -> ThumbnailStatus.FAILED
            "unsupported" -> ThumbnailStatus.UNSUPPORTED
            else -> ThumbnailStatus.NONE
        }
    val createdAtMillis: Long get() = createdAtMs
    val modifiedAtMillis: Long get() = updatedAtMs
    val lastOpenedAtMillis: Long get() = lastContentOpenedAtMs ?: 0L
}

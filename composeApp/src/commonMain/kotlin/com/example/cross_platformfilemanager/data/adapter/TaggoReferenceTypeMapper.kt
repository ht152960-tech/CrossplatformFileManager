package com.example.cross_platformfilemanager.data.adapter

fun inferTaggoReferenceType(referenceValue: String): String = when {
    referenceValue.startsWith("content://", ignoreCase = true) -> "android_content_uri"
    referenceValue.startsWith("browser-handle:", ignoreCase = true) -> "web_file_handle"
    referenceValue.startsWith("browser-file:", ignoreCase = true) -> "web_file_token"
    referenceValue.startsWith("http://", ignoreCase = true) ||
        referenceValue.startsWith("https://", ignoreCase = true) -> "remote_url"
    referenceValue.isWindowsAbsolutePath() -> "local_path"
    referenceValue.startsWith("/") -> "local_path"
    "://" in referenceValue -> "external_uri"
    else -> "unknown_reference"
}

private fun String.isWindowsAbsolutePath(): Boolean =
    length >= 3 &&
        this[0].isLetter() &&
        this[1] == ':' &&
        (this[2] == '\\' || this[2] == '/')

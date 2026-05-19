package com.example.cross_platformfilemanager

data class FileOpenLog(
    val fileId: String,
    val openedAtMillis: Long,
    val previousFileId: String? = null,
)

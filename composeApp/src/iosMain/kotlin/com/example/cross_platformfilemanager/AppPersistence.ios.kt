package com.example.cross_platformfilemanager

// iOS 端当前未接入快照持久化、文件选择和引用解析能力。
actual fun createAppSnapshotStore(): AppSnapshotStore? = null

actual fun createBrowserReferencePicker(): BrowserReferencePicker? = null

actual fun createBrowserReferenceResolver(): BrowserReferenceResolver? = null

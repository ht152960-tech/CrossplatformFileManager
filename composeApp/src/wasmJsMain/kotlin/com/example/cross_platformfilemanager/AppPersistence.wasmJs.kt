package com.example.cross_platformfilemanager

import kotlinx.browser.window

private const val LOCAL_STORAGE_KEY = "file-atlas.snapshot"

actual fun createAppSnapshotStore(): AppSnapshotStore? = BrowserSnapshotStore()

private class BrowserSnapshotStore : AppSnapshotStore {
    override suspend fun load(): AppSnapshot? {
        val encoded = window.localStorage.getItem(LOCAL_STORAGE_KEY) ?: return null
        return SnapshotCodec.decode(encoded)
    }

    override suspend fun save(snapshot: AppSnapshot) {
        window.localStorage.setItem(LOCAL_STORAGE_KEY, SnapshotCodec.encode(snapshot))
    }
}


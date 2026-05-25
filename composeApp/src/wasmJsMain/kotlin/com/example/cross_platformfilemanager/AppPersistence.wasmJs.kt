@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.example.cross_platformfilemanager

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

private const val LOCAL_STORAGE_KEY = "file-atlas.snapshot"

actual fun createAppSnapshotStore(): AppSnapshotStore? = BrowserSnapshotStore()

private class BrowserSnapshotStore : AppSnapshotStore {
    override suspend fun load(): AppSnapshot? {
        val encoded = loadEncodedSnapshot() ?: return null
        return SnapshotCodec.decode(encoded)
    }

    override suspend fun save(snapshot: AppSnapshot) {
        val encoded = SnapshotCodec.encode(snapshot)
        saveEncodedSnapshot(encoded)
    }
}

private suspend fun loadEncodedSnapshot(): String? {
    val api = browserPersistenceApi() ?: return window.localStorage.getItem(LOCAL_STORAGE_KEY)
    return try {
        val loaded: JsAny? = loadSnapshotPromise(api).await()
        loaded as String?
    } catch (_: Throwable) {
        window.localStorage.getItem(LOCAL_STORAGE_KEY)
    }
}

private suspend fun saveEncodedSnapshot(encoded: String) {
    val api = browserPersistenceApi()
    if (api != null) {
        try {
            val ignored: JsAny? = saveSnapshotPromise(api, encoded).await()
            window.localStorage.setItem(LOCAL_STORAGE_KEY, encoded)
            return
        } catch (_: Throwable) {
        }
    }
    window.localStorage.setItem(LOCAL_STORAGE_KEY, encoded)
}

private fun browserPersistenceApi(): JsAny? = js("window.fileAtlasPersistence ?? null")

private fun loadSnapshotPromise(api: JsAny): Promise<JsAny?> = js("api.loadSnapshot()")

private fun saveSnapshotPromise(api: JsAny, encoded: String): Promise<JsAny?> = js("api.saveSnapshot(encoded)")

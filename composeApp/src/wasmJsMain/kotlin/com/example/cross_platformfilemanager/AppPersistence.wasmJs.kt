@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.example.cross_platformfilemanager

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

private const val LOCAL_STORAGE_KEY = "file-atlas.snapshot"

actual fun createAppSnapshotStore(): AppSnapshotStore? = BrowserSnapshotStore()

actual fun createLocalDataController(): LocalDataController? = BrowserLocalDataController()

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

private class BrowserLocalDataController : LocalDataController {
    override suspend fun exportSnapshot(): String? {
        val encoded = loadEncodedSnapshot() ?: return null
        return encoded
    }

    override suspend fun importSnapshot(): String? {
        val api = browserPersistenceApi() ?: return null
        return try {
            val imported: JsAny? = importSnapshotPromise(api).await()
            imported as String?
        } catch (_: Throwable) {
            null
        }
    }

    override suspend fun clearAllData() {
        val api = browserPersistenceApi()
        if (api != null) {
            try {
                val ignored: JsAny? = clearAllDataPromise(api).await()
                return
            } catch (_: Throwable) {
            }
        }
        window.localStorage.removeItem(LOCAL_STORAGE_KEY)
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

private fun importSnapshotPromise(api: JsAny): Promise<JsAny?> = js("api.importSnapshot()")

private fun clearAllDataPromise(api: JsAny): Promise<JsAny?> = js("api.clearAllData()")

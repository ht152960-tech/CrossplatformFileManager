package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.ExperimentalWasmJsInterop

actual fun createBrowserReferenceResolver(): BrowserReferenceResolver? = BrowserReferenceResolverJs()

private external interface BrowserResolveInterop {
    fun resolveReference(source: String): Promise<JsAny?>
}

private class BrowserReferenceResolverJs : BrowserReferenceResolver {
    override suspend fun resolveReference(reference: FileReference): BrowserReferenceDraft? {
        val encoded: JsAny? = browserInterop().resolveReference(reference.source).await()
        return BrowserReferenceFormat.decode(encoded as? String ?: return null)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun browserInterop(): BrowserResolveInterop = js("window.fileAtlasBrowser")

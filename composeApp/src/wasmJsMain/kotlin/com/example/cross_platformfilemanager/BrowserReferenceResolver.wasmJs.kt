@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.ExperimentalWasmJsInterop

// Wasm 端通过浏览器桥接对象把外部引用解析成可入库的文件草稿。
actual fun createBrowserReferenceResolver(): BrowserReferenceResolver? = BrowserReferenceResolverWasm()

private external interface BrowserResolveInteropWasm {
    fun resolveReference(source: String): Promise<JsAny?>
}

private class BrowserReferenceResolverWasm : BrowserReferenceResolver {
    override suspend fun resolveReference(reference: FileReference): BrowserReferenceDraft? {
        val bridge = browserInterop() ?: return null
        val encoded: JsAny? = bridge.resolveReference(reference.source).await()
        val encodedString = encoded as? String ?: return null
        return BrowserReferenceFormat.decode(encodedString)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun browserInterop(): BrowserResolveInteropWasm? =
    js("(globalThis.fileAtlasBrowser || null)")

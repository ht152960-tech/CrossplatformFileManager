@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

// Wasm 端通过浏览器桥接对象触发文件选择，再把编码结果解回共享层草稿。
actual fun createBrowserReferencePicker(): BrowserReferencePicker? = BrowserReferencePickerWasm()

private external interface BrowserPickInteropWasm {
    fun pickReference(): Promise<JsAny?>
}

private class BrowserReferencePickerWasm : BrowserReferencePicker {
    override suspend fun pickReference(): BrowserReferenceDraft? {
        val bridge = browserInterop() ?: return null
        val encoded: JsAny? = bridge.pickReference().await()
        return BrowserReferenceFormat.decode(encoded?.toString() ?: return null)
    }
}

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun browserInterop(): BrowserPickInteropWasm? =
    js("(globalThis.fileAtlasBrowser || null)")

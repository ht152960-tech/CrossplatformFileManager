@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

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

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
        val encoded: JsAny? = browserInterop().pickReference().await()
        return BrowserReferenceFormat.decode(encoded as? String ?: return null)
    }
}

private fun browserInterop(): BrowserPickInteropWasm = js("window.fileAtlasBrowser")

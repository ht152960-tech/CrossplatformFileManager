package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.ExperimentalWasmJsInterop

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

@OptIn(ExperimentalWasmJsInterop::class)
private fun browserInterop(): BrowserPickInteropWasm = js("window.fileAtlasBrowser")

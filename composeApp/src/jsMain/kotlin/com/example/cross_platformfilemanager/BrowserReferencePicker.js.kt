package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.ExperimentalWasmJsInterop

actual fun createBrowserReferencePicker(): BrowserReferencePicker? = BrowserReferencePickerJs()

private external interface BrowserPickInterop {
    fun pickReference(): Promise<JsAny?>
}

private class BrowserReferencePickerJs : BrowserReferencePicker {
    override suspend fun pickReference(): BrowserReferenceDraft? {
        val encoded: JsAny? = browserInterop().pickReference().await()
        return BrowserReferenceFormat.decode(encoded as? String ?: return null)
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun browserInterop(): BrowserPickInterop = js("window.fileAtlasBrowser")

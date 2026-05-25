@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.example.cross_platformfilemanager

import kotlinx.coroutines.await
import kotlin.js.JsAny
import kotlin.js.Promise

// JS 端通过浏览器桥接对象触发文件选择，再把编码结果解回共享层草稿。
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

private fun browserInterop(): BrowserPickInterop = js("window.fileAtlasBrowser")

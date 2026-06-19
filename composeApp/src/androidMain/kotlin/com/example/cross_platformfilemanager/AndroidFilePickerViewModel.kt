package com.example.cross_platformfilemanager

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

internal class AndroidFilePickerViewModel(
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private var runtimePickStarted = false

    val isPickPending: Boolean
        get() = savedStateHandle[KEY_PENDING] ?: false

    fun beginSingleFilePick(): String? {
        if (isPickPending) return null
        val requestId = "${nowMillis()}-${nextRequestSequence++}"
        savedStateHandle[KEY_PENDING] = true
        savedStateHandle[KEY_REQUEST_ID] = requestId
        savedStateHandle[KEY_REQUEST_TYPE] = REQUEST_TYPE_SINGLE_FILE
        runtimePickStarted = true
        return requestId
    }

    fun consumePickerResult(): Boolean {
        val hadPending = isPickPending
        clearPending()
        return hadPending
    }

    fun clearPending() {
        savedStateHandle[KEY_PENDING] = false
        savedStateHandle[KEY_REQUEST_ID] = null
        savedStateHandle[KEY_REQUEST_TYPE] = null
        runtimePickStarted = false
    }

    fun clearStalePendingAfterProcessRestore(): Boolean {
        if (isPickPending && !runtimePickStarted) {
            clearPending()
            return true
        }
        return false
    }

    companion object {
        private const val KEY_PENDING = "android_file_picker_pending"
        private const val KEY_REQUEST_ID = "android_file_picker_request_id"
        private const val KEY_REQUEST_TYPE = "android_file_picker_request_type"
        private const val REQUEST_TYPE_SINGLE_FILE = "single_file"

        private var nextRequestSequence = 0
    }
}

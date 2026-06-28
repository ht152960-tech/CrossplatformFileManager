package com.example.cross_platformfilemanager

internal object BrowserReferenceFormat {
    fun encode(
        title: String,
        source: String,
        fileType: String,
        fileSizeBytes: Long?,
    ): String = buildString {
        appendString(title)
        appendString(source)
        appendString(fileType)
        appendString(fileSizeBytes?.toString().orEmpty())
    }

    fun decode(payload: String): BrowserReferenceDraft? {
        if (payload.isBlank()) return null
        return runCatching {
            val cursor = Cursor(payload)
            val title = cursor.readString()
            val source = cursor.readString()
            val fileType = cursor.readString()
            val fileSizeBytes = cursor.readString().toLongOrNull()
            BrowserReferenceDraft(
                title = title.ifBlank { "Untitled file" },
                source = source.ifBlank { "browser-handle:unknown" },
                fileType = fileType.ifBlank { "FILE" },
                fileSizeBytes = fileSizeBytes,
            )
        }.getOrNull()
    }

    private class Cursor(private val text: String) {
        private var index = 0

        fun readString(): String {
            val lengthEnd = text.indexOf(':', startIndex = index)
            require(lengthEnd >= 0)
            val length = text.substring(index, lengthEnd).toInt()
            val start = lengthEnd + 1
            val end = start + length
            require(end <= text.length)
            index = end
            return text.substring(start, end)
        }
    }

    private fun StringBuilder.appendString(value: String) {
        append(value.length)
        append(':')
        append(value)
    }
}

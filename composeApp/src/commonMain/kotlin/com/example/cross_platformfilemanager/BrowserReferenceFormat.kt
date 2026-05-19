package com.example.cross_platformfilemanager

internal object BrowserReferenceFormat {
    fun encode(
        title: String,
        source: String,
        fileType: String,
        fileSizeBytes: Long?,
        notes: String,
    ): String = buildString {
        appendString(title)
        appendString(source)
        appendString(fileType)
        appendString(fileSizeBytes?.toString().orEmpty())
        appendString(notes)
    }

    fun decode(payload: String): BrowserReferenceDraft? {
        if (payload.isBlank()) return null
        val cursor = Cursor(payload)
        val title = cursor.readString()
        val source = cursor.readString()
        val fileType = cursor.readString()
        val sizeOrNotes = cursor.readString()
        val (fileSizeBytes, notes) = if (cursor.remaining() == 0) {
            null to sizeOrNotes
        } else {
            val noteText = cursor.readString()
            if (cursor.remaining() != 0) return null
            sizeOrNotes.toLongOrNull() to noteText
        }

        return BrowserReferenceDraft(
            title = title.ifBlank { "Untitled file" },
            source = source.ifBlank { "browser-handle:unknown" },
            fileType = fileType.ifBlank { "FILE" },
            fileSizeBytes = fileSizeBytes ?: guessFileSizeFromNotes(notes),
            notes = notes.ifBlank { "Selected from browser file picker." },
        )
    }

    private class Cursor(private val text: String) {
        private var index = 0

        fun readString(): String {
            var lengthEnd = index
            while (lengthEnd < text.length && text[lengthEnd] != ':') {
                lengthEnd++
            }
            require(lengthEnd < text.length) { "Invalid browser reference payload" }
            val length = text.substring(index, lengthEnd).toInt()
            val start = lengthEnd + 1
            val end = start + length
            require(end <= text.length) { "Invalid browser reference payload" }
            val value = text.substring(start, end)
            index = end
            return value
        }

        fun remaining(): Int = text.length - index
    }

    private fun StringBuilder.appendString(value: String) {
        append(value.length)
        append(':')
        append(value)
    }
}

package com.example.cross_platformfilemanager

/**
 * 负责把浏览器文件选择器返回的草稿信息编码成字符串，并在下次恢复时解码回来。
 *
 * 这里使用长度前缀格式，避免标题、备注里出现分隔符时把字段拆错。
 */
internal object BrowserReferenceFormat {
    /**
     * 按固定字段顺序写入字符串，供 Web/Wasm 端跨会话保存草稿信息。
     */
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

    /**
     * 从持久化字符串恢复文件草稿。
     *
     * 兼容旧格式：早期版本把 `fileSizeBytes` 和 `notes` 合并存放，因此这里要保留回退解析。
     */
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

    /**
     * 逐段读取长度前缀字符串，保证解码顺序与编码顺序一致。
     */
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

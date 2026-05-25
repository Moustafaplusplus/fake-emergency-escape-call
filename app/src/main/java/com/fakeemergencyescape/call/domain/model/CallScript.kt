package com.fakeemergencyescape.call.domain.model

import org.json.JSONArray
import org.json.JSONObject

data class CallScriptLine(
    val text: String,
    val pauseAfterMs: Long = DEFAULT_PAUSE_MS,
) {
    companion object {
        const val DEFAULT_PAUSE_MS = 3_500L
    }
}

data class CallScript(
    val lines: List<CallScriptLine>,
) {
    init {
        require(lines.isNotEmpty()) { "Script must have at least one line" }
    }

    val lineCount: Int get() = lines.size

    /** Full conversation text for UI preview (lines + pause markers). */
    fun displayText(): String = buildString {
        lines.forEachIndexed { index, line ->
            if (index > 0) append('\n')
            append(line.text)
            if (line.pauseAfterMs > 0 && index < lines.lastIndex) {
                append("\n⏸ ")
                append(formatPause(line.pauseAfterMs))
                append(" — respond here")
            }
        }
    }

    companion object {
        fun single(text: String): CallScript =
            CallScript(listOf(CallScriptLine(text, pauseAfterMs = 0)))

        fun formatPause(pauseAfterMs: Long): String = when {
            pauseAfterMs >= 1_000 && pauseAfterMs % 1_000 == 0L -> "${pauseAfterMs / 1_000}s"
            pauseAfterMs >= 1_000 -> "%.1fs".format(pauseAfterMs / 1_000f)
            else -> "${pauseAfterMs}ms"
        }
    }
}

object CallScriptCodec {
    private const val KEY_LINES = "lines"
    private const val KEY_TEXT = "text"
    private const val KEY_PAUSE = "pauseAfterMs"

    fun encode(script: CallScript): String {
        val root = JSONObject()
        val array = JSONArray()
        script.lines.forEach { line ->
            array.put(
                JSONObject()
                    .put(KEY_TEXT, line.text)
                    .put(KEY_PAUSE, line.pauseAfterMs),
            )
        }
        root.put(KEY_LINES, array)
        return root.toString()
    }

    fun decode(json: String?): CallScript? {
        if (json.isNullOrBlank()) return null
        return try {
            val root = JSONObject(json)
            val array = root.getJSONArray(KEY_LINES)
            val lines = buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        CallScriptLine(
                            text = obj.getString(KEY_TEXT),
                            pauseAfterMs = obj.optLong(KEY_PAUSE, CallScriptLine.DEFAULT_PAUSE_MS),
                        ),
                    )
                }
            }
            if (lines.isEmpty()) null else CallScript(lines)
        } catch (_: Exception) {
            null
        }
    }
}

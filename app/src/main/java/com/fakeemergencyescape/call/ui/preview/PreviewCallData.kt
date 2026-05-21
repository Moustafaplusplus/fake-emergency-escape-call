package com.fakeemergencyescape.call.ui.preview

data class PreviewCall(
    val id: String,
    val callerName: String,
    val message: String,
)

object PreviewCallData {
    const val PREVIEW_ID = "preview"

    val preview = PreviewCall(
        id = PREVIEW_ID,
        callerName = "Alex Morgan",
        message = "Hey, something came up. Can you step out for a minute?",
    )

    val homeSamples = listOf(
        PreviewCall("sample-1", "Alex Morgan", "Your next meeting is starting now."),
        PreviewCall("sample-2", "Jordan Lee", "Can you come back home? I need your help."),
    )

    fun findById(id: String): PreviewCall = when (id) {
        PREVIEW_ID -> preview
        else -> homeSamples.find { it.id == id }
            ?: preview.copy(id = id, callerName = "Caller")
    }

    fun initials(name: String): String =
        name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "?" }
}

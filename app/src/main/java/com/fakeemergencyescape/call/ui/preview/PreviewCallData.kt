package com.fakeemergencyescape.call.ui.preview

import com.fakeemergencyescape.call.domain.model.CallScript
import com.fakeemergencyescape.call.domain.model.CallScriptCodec
import com.fakeemergencyescape.call.domain.model.CallScriptLine
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.domain.model.MessageType

object PreviewCallData {
    const val INCOMING_ID = "preview_incoming"
    const val ACTIVE_ID = "preview_active"

    private val previewScriptJson = CallScriptCodec.encode(
        CallScript(
            listOf(
                CallScriptLine("Hey, you there?", 3_500),
                CallScriptLine("Where are you right now?", 4_000),
                CallScriptLine("The smoke alarm has been going off for ten minutes.", 3_000),
                CallScriptLine("Please come home right now.", 0),
            ),
        ),
    )

    fun isPreviewCall(callId: String): Boolean =
        callId == INCOMING_ID || callId == ACTIVE_ID

    fun findById(callId: String): FakeCall? = when (callId) {
        INCOMING_ID -> sampleCall(id = INCOMING_ID, status = CallStatus.SCHEDULED)
        ACTIVE_ID -> sampleCall(id = ACTIVE_ID, status = CallStatus.ANSWERED)
        else -> null
    }

    private fun sampleCall(id: String, status: CallStatus): FakeCall {
        val now = System.currentTimeMillis()
        return FakeCall(
            id = id,
            callerName = "Alex Morgan",
            callerPhotoUri = null,
            message = "Hey, you there? … smoke alarm … come home now.",
            scriptJson = previewScriptJson,
            messageType = MessageType.TEXT,
            voiceMessageUri = null,
            scheduledAtMillis = now,
            ringtoneUri = null,
            voiceLocale = "en-US",
            speechRate = 1f,
            pitch = 1f,
            vibrationEnabled = false,
            status = status,
            createdAtMillis = now,
            updatedAtMillis = now,
        )
    }
}

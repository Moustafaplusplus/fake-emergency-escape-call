package com.fakeemergencyescape.call.ui.active

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fakeemergencyescape.call.domain.model.ActiveCallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.ActiveCallControlId
import com.fakeemergencyescape.call.domain.model.DefaultActiveCallAppearance
import com.fakeemergencyescape.call.ui.theme.CallScreenTheme

@Composable
fun ActiveCallScreen(
    callerName: String,
    callDurationFormatted: String,
    speakerOn: Boolean,
    muted: Boolean,
    showNoEarpieceHint: Boolean,
    ttsError: String?,
    isSpeaking: Boolean,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onReplay: () -> Unit,
    onEndCall: () -> Unit,
    appearance: ActiveCallAppearanceSettings = DefaultActiveCallAppearance,
) {
    CallScreenTheme {
        ActiveCallContent(
            callerName = callerName,
            callDurationFormatted = callDurationFormatted,
            appearance = appearance,
            muted = muted,
            speakerOn = speakerOn,
            showNoEarpieceHint = showNoEarpieceHint,
            ttsError = ttsError,
            isSpeaking = isSpeaking,
            controlUi = { id -> activeCallControlUi(id, muted, speakerOn, replayEnabled = ttsError == null) },
            onControlClick = { id ->
                when (id) {
                    ActiveCallControlId.MUTE -> onToggleMute()
                    ActiveCallControlId.SPEAKER -> onToggleSpeaker()
                    ActiveCallControlId.REPLAY -> onReplay()
                    else -> Unit
                }
            },
            onEndCall = onEndCall,
            modifier = Modifier.fillMaxSize(),
            replayEnabled = ttsError == null,
        )
    }
}

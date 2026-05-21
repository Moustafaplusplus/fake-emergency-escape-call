package com.fakeemergencyescape.call.ui.incoming

import androidx.compose.runtime.Composable
import com.fakeemergencyescape.call.domain.model.CallAppearanceSettings
import com.fakeemergencyescape.call.ui.theme.CallScreenTheme

@Composable
fun IncomingCallScreen(
    callerName: String,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    appearance: CallAppearanceSettings = CallAppearanceSettings(),
) {
    CallScreenTheme {
        IncomingCallContent(
            callerName = callerName,
            appearance = appearance,
            onAnswer = onAnswer,
            onDecline = onDecline,
        )
    }
}

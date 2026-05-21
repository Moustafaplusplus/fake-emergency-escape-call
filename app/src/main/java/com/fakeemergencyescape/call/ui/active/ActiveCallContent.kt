package com.fakeemergencyescape.call.ui.active

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.ActiveCallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.ActiveCallControlId
import com.fakeemergencyescape.call.ui.components.CallAvatar
import com.fakeemergencyescape.call.ui.components.CustomizableCallBackground
import com.fakeemergencyescape.call.ui.components.PositionedActiveCallControls
import com.fakeemergencyescape.call.ui.util.CallDisplayUtils
import com.fakeemergencyescape.call.ui.theme.CallTextPrimary
import com.fakeemergencyescape.call.ui.theme.CallTextSecondary

@Composable
fun ActiveCallContent(
    callerName: String,
    callDurationFormatted: String,
    appearance: ActiveCallAppearanceSettings,
    muted: Boolean,
    speakerOn: Boolean,
    showNoEarpieceHint: Boolean,
    ttsError: String?,
    isSpeaking: Boolean,
    controlUi: @Composable (ActiveCallControlId) -> ActiveCallControlUi,
    onControlClick: (ActiveCallControlId) -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    replayEnabled: Boolean = true,
    onControlPositionChange: (ActiveCallControlId, Float, Float) -> Unit = { _, _, _ -> },
    onControlRemove: (ActiveCallControlId) -> Unit = {},
    onEndCallPositionChange: (Float, Float) -> Unit = { _, _ -> },
) {
    CustomizableCallBackground(
        appearance = appearance,
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(52.dp))
                CallAvatar(
                    initials = CallDisplayUtils.initials(callerName),
                    size = (104 * appearance.avatarScale).dp.coerceIn(80.dp, 140.dp),
                    showRing = true,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = callerName,
                    color = CallTextPrimary,
                    fontSize = (32f * appearance.callerNameScale).coerceIn(24f, 42f).sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = callDurationFormatted,
                    color = CallTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (isSpeaking && !muted) {
                    Text(
                        text = stringResource(R.string.active_speaking),
                        color = CallTextSecondary.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (showNoEarpieceHint && !speakerOn) {
                    Text(
                        text = stringResource(R.string.active_no_earpiece_hint),
                        color = CallTextSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
                    )
                }
                ttsError?.let { error ->
                    Text(
                        text = error,
                        color = CallTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }

            PositionedActiveCallControls(
                appearance = appearance,
                controlUi = controlUi,
                onControlClick = onControlClick,
                endCallLabel = stringResource(R.string.active_end_call),
                onEndCall = onEndCall,
                modifier = Modifier.fillMaxSize(),
                editable = editable,
                onControlPositionChange = onControlPositionChange,
                onControlRemove = onControlRemove,
                onEndCallPositionChange = onEndCallPositionChange,
            )
        }
    }
}

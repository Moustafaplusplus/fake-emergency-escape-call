package com.fakeemergencyescape.call.ui.incoming

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.CallAppearanceSettings
import com.fakeemergencyescape.call.ui.components.CallAvatar
import com.fakeemergencyescape.call.ui.components.CustomizableCallBackground
import com.fakeemergencyescape.call.ui.components.PositionedCallButtons
import com.fakeemergencyescape.call.ui.util.CallDisplayUtils
import com.fakeemergencyescape.call.ui.theme.CallTextPrimary
import com.fakeemergencyescape.call.ui.theme.CallTextSecondary

@Composable
fun IncomingCallContent(
    callerName: String,
    appearance: CallAppearanceSettings,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
    draggableButtons: Boolean = false,
    onDeclinePositionChange: (Float, Float) -> Unit = { _, _ -> },
    onAnswerPositionChange: (Float, Float) -> Unit = { _, _ -> },
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
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = stringResource(R.string.incoming_call_label),
                    color = CallTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Spacer(modifier = Modifier.weight(0.35f))
                CallAvatar(
                    initials = CallDisplayUtils.initials(callerName),
                    size = (156 * appearance.avatarScale).dp.coerceIn(96.dp, 200.dp),
                    showRing = true,
                )
                Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = callerName,
                    color = CallTextPrimary,
                    fontSize = (36f * appearance.callerNameScale).coerceIn(24f, 48f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp,
                )
                if (appearance.showMobileLabel) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.incoming_mobile_label),
                        color = CallTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Spacer(modifier = Modifier.weight(0.65f))
            }

            PositionedCallButtons(
                appearance = appearance,
                onDecline = onDecline,
                onAnswer = onAnswer,
                modifier = Modifier.fillMaxSize(),
                draggable = draggableButtons,
                onDeclinePositionChange = onDeclinePositionChange,
                onAnswerPositionChange = onAnswerPositionChange,
            )
        }
    }
}

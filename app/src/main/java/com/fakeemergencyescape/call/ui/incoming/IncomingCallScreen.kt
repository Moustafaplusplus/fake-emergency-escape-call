package com.fakeemergencyescape.call.ui.incoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.fakeemergencyescape.call.ui.components.CallAvatar
import com.fakeemergencyescape.call.ui.components.CallScreenBackground
import com.fakeemergencyescape.call.ui.components.IncomingCallActions
import com.fakeemergencyescape.call.ui.preview.PreviewCallData
import com.fakeemergencyescape.call.ui.theme.CallScreenTheme
import com.fakeemergencyescape.call.ui.theme.CallTextPrimary
import com.fakeemergencyescape.call.ui.theme.CallTextSecondary

@Composable
fun IncomingCallScreen(
    callerName: String,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
) {
    CallScreenTheme {
        CallScreenBackground {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
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
                        initials = PreviewCallData.initials(callerName),
                        size = 156.dp,
                        showRing = true,
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    Text(
                        text = callerName,
                        color = CallTextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 42.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.incoming_mobile_label),
                        color = CallTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(modifier = Modifier.weight(0.65f))
                    IncomingCallActions(
                        onDecline = onDecline,
                        onAnswer = onAnswer,
                        declineLabel = stringResource(R.string.incoming_decline),
                        answerLabel = stringResource(R.string.incoming_answer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                    )
                }
        }
    }
}

package com.fakeemergencyescape.call.ui.active

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.ui.components.CallAvatar
import com.fakeemergencyescape.call.ui.components.CallControlGridButton
import com.fakeemergencyescape.call.ui.components.CallScreenBackground
import com.fakeemergencyescape.call.ui.components.EndCallButton
import com.fakeemergencyescape.call.ui.preview.PreviewCallData
import com.fakeemergencyescape.call.ui.theme.CallScreenTheme
import com.fakeemergencyescape.call.ui.theme.CallTextPrimary
import com.fakeemergencyescape.call.ui.theme.CallTextSecondary

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
) {
    CallScreenTheme {
        CallScreenBackground {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(52.dp))
                    CallAvatar(
                        initials = PreviewCallData.initials(callerName),
                        size = 104.dp,
                        showRing = true,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = callerName,
                        color = CallTextPrimary,
                        fontSize = 32.sp,
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
                    Spacer(modifier = Modifier.weight(1f))
                    CallControlsRow(
                        controls = listOf(
                            CallControlItem(
                                icon = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
                                label = stringResource(R.string.active_mute),
                                active = muted,
                                onClick = onToggleMute,
                            ),
                            CallControlItem(
                                icon = if (speakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.Outlined.Phone,
                                label = stringResource(R.string.active_speaker_btn),
                                active = speakerOn,
                                onClick = onToggleSpeaker,
                            ),
                            CallControlItem(
                                icon = Icons.Default.Dialpad,
                                label = stringResource(R.string.active_keypad),
                                enabled = false,
                                onClick = {},
                            ),
                        ),
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    CallControlsRow(
                        controls = listOf(
                            CallControlItem(
                                icon = Icons.Default.Bluetooth,
                                label = stringResource(R.string.active_bluetooth),
                                enabled = false,
                                onClick = {},
                            ),
                            CallControlItem(
                                icon = Icons.Default.Pause,
                                label = stringResource(R.string.active_hold),
                                enabled = false,
                                onClick = {},
                            ),
                            CallControlItem(
                                icon = Icons.Default.Replay,
                                label = stringResource(R.string.active_replay),
                                enabled = ttsError == null,
                                onClick = onReplay,
                            ),
                            CallControlItem(
                                icon = Icons.Default.MoreVert,
                                label = stringResource(R.string.active_more),
                                enabled = false,
                                onClick = {},
                            ),
                        ),
                    )
                    Spacer(modifier = Modifier.height(40.dp))
                    EndCallButton(
                        label = stringResource(R.string.active_end_call),
                        onClick = onEndCall,
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                }
        }
    }
}

private data class CallControlItem(
    val icon: ImageVector,
    val label: String,
    val active: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
private fun CallControlsRow(
    controls: List<CallControlItem>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        controls.forEach { item ->
            CallControlGridButton(
                icon = item.icon,
                label = item.label,
                onClick = item.onClick,
                active = item.active,
                enabled = item.enabled,
            )
        }
    }
}

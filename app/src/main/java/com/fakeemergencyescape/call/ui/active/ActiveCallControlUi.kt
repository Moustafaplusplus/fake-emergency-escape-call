package com.fakeemergencyescape.call.ui.active

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.ActiveCallControlId

data class ActiveCallControlUi(
    val icon: ImageVector,
    val label: String,
    val active: Boolean = false,
    val enabled: Boolean = true,
)

@Composable
fun activeCallControlUi(
    id: ActiveCallControlId,
    muted: Boolean = false,
    speakerOn: Boolean = false,
    replayEnabled: Boolean = true,
): ActiveCallControlUi = when (id) {
    ActiveCallControlId.MUTE -> ActiveCallControlUi(
        icon = if (muted) Icons.Default.MicOff else Icons.Default.Mic,
        label = stringResource(R.string.active_mute),
        active = muted,
        enabled = true,
    )
    ActiveCallControlId.SPEAKER -> ActiveCallControlUi(
        icon = if (speakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.Outlined.Phone,
        label = stringResource(R.string.active_speaker_btn),
        active = speakerOn,
        enabled = true,
    )
    ActiveCallControlId.KEYPAD -> ActiveCallControlUi(
        icon = Icons.Default.Dialpad,
        label = stringResource(R.string.active_keypad),
        enabled = false,
    )
    ActiveCallControlId.BLUETOOTH -> ActiveCallControlUi(
        icon = Icons.Default.Bluetooth,
        label = stringResource(R.string.active_bluetooth),
        enabled = false,
    )
    ActiveCallControlId.HOLD -> ActiveCallControlUi(
        icon = Icons.Default.Pause,
        label = stringResource(R.string.active_hold),
        enabled = false,
    )
    ActiveCallControlId.REPLAY -> ActiveCallControlUi(
        icon = Icons.Default.Replay,
        label = stringResource(R.string.active_replay),
        enabled = replayEnabled,
    )
    ActiveCallControlId.MORE -> ActiveCallControlUi(
        icon = Icons.Default.MoreVert,
        label = stringResource(R.string.active_more),
        enabled = false,
    )
}

fun ActiveCallControlId.labelRes(): Int = when (this) {
    ActiveCallControlId.MUTE -> R.string.active_mute
    ActiveCallControlId.SPEAKER -> R.string.active_speaker_btn
    ActiveCallControlId.KEYPAD -> R.string.active_keypad
    ActiveCallControlId.BLUETOOTH -> R.string.active_bluetooth
    ActiveCallControlId.HOLD -> R.string.active_hold
    ActiveCallControlId.REPLAY -> R.string.active_replay
    ActiveCallControlId.MORE -> R.string.active_more
}

@Composable
fun activeCallControlLabel(id: ActiveCallControlId): String = stringResource(id.labelRes())

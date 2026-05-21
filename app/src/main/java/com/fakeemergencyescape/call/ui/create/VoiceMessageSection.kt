package com.fakeemergencyescape.call.ui.create

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.ui.components.IconButton3D
import com.fakeemergencyescape.call.ui.components.Primary3DButton
import com.fakeemergencyescape.call.ui.components.Secondary3DButton

@Composable
fun VoiceMessageSection(
    hasRecording: Boolean,
    isRecording: Boolean,
    recordingElapsedSec: Int,
    isPlayingPreview: Boolean,
    enabled: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onClearRecording: () -> Unit,
    onTogglePreview: () -> Unit,
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) onStartRecording()
    }

    fun requestRecord() {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.voice_message_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when {
            isRecording -> {
                Text(
                    text = stringResource(R.string.voice_recording_elapsed, formatElapsed(recordingElapsedSec)),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Primary3DButton(
                    text = stringResource(R.string.voice_stop_recording),
                    onClick = onStopRecording,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                )
            }
            hasRecording -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Secondary3DButton(
                        text = if (isPlayingPreview) {
                            stringResource(R.string.voice_stop_preview)
                        } else {
                            stringResource(R.string.voice_play_preview)
                        },
                        onClick = onTogglePreview,
                        enabled = enabled,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton3D(
                        icon = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.voice_delete_recording),
                        onClick = onClearRecording,
                        enabled = enabled,
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
                Secondary3DButton(
                    text = stringResource(R.string.voice_record_again),
                    onClick = { requestRecord() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                )
            }
            else -> {
                Primary3DButton(
                    text = stringResource(R.string.voice_start_recording),
                    onClick = { requestRecord() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                )
            }
        }
    }
}

private fun formatElapsed(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

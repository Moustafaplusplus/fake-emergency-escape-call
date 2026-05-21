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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.R

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

        if (isRecording) {
            Text(
                text = stringResource(R.string.voice_recording_elapsed, formatElapsed(recordingElapsedSec)),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Button(
                onClick = onStopRecording,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Text(
                    text = stringResource(R.string.voice_stop_recording),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        } else if (hasRecording) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = onTogglePreview,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                    Text(
                        text = if (isPlayingPreview) {
                            stringResource(R.string.voice_stop_preview)
                        } else {
                            stringResource(R.string.voice_play_preview)
                        },
                        modifier = Modifier.padding(start = 6.dp),
                    )
                }
                IconButton(onClick = onClearRecording, enabled = enabled) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.voice_delete_recording))
                }
            }
            OutlinedButton(
                onClick = { requestRecord() },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Text(
                    text = stringResource(R.string.voice_record_again),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        } else {
            Button(
                onClick = { requestRecord() },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
            ) {
                Icon(Icons.Default.Mic, contentDescription = null)
                Text(
                    text = stringResource(R.string.voice_start_recording),
                    modifier = Modifier.padding(start = 8.dp),
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

package com.fakeemergencyescape.call.ui.callappearance

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.fakeemergencyescape.call.R

@Composable
fun rememberAppearanceEditorBackHandler(
    hasUnsavedChanges: Boolean,
    onNavigateBack: () -> Unit,
): () -> Unit {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.unsaved_changes_title)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onNavigateBack()
                    },
                ) {
                    Text(stringResource(R.string.unsaved_changes_discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.unsaved_changes_stay))
                }
            },
        )
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showDialog = true
    }

    return remember(hasUnsavedChanges, onNavigateBack) {
        {
            if (hasUnsavedChanges) {
                showDialog = true
            } else {
                onNavigateBack()
            }
        }
    }
}

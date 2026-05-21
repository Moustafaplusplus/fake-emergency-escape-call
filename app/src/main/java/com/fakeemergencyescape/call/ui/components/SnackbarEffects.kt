package com.fakeemergencyescape.call.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ShowMessageSnackbar(
    message: String?,
    snackbarHostState: SnackbarHostState,
    onShown: () -> Unit = {},
) {
    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            onShown()
        }
    }
}

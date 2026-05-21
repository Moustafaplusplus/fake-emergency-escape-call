package com.fakeemergencyescape.call.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun OpenLegalUrlButton(
    label: String,
    url: String,
) {
    val context = LocalContext.current
    TextButton(
        onClick = {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)),
            )
        },
    ) {
        Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

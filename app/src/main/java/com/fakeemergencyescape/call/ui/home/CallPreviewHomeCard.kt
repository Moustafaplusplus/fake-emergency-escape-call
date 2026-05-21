package com.fakeemergencyescape.call.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.ui.components.ElevatedAppCard
import com.fakeemergencyescape.call.ui.components.Secondary3DButton

@Composable
fun CallPreviewHomeCard(
    onPreviewIncoming: () -> Unit,
    onPreviewActive: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedAppCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.home_call_preview_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.home_call_preview_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Secondary3DButton(
                text = stringResource(R.string.home_preview_incoming),
                onClick = onPreviewIncoming,
                modifier = Modifier.fillMaxWidth(),
            )
            Secondary3DButton(
                text = stringResource(R.string.home_preview_active),
                onClick = onPreviewActive,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

package com.fakeemergencyescape.call.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.ui.components.ElevatedAppCard
import com.fakeemergencyescape.call.ui.components.GradientOutlineButton
import com.fakeemergencyescape.call.ui.components.SectionLabel
import com.fakeemergencyescape.call.ui.theme.CallAnswerGreen
import com.fakeemergencyescape.call.ui.theme.SecondaryAccent
import com.fakeemergencyescape.call.ui.theme.TertiaryAccent

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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SectionLabel(text = stringResource(R.string.home_call_preview_title))
            Text(
                text = stringResource(R.string.home_call_preview_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                GradientOutlineButton(
                    text = stringResource(R.string.home_preview_incoming),
                    onClick = onPreviewIncoming,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Visibility,
                )
                GradientOutlineButton(
                    text = stringResource(R.string.home_preview_active),
                    onClick = onPreviewActive,
                    modifier = Modifier.weight(1f),
                    leadingIcon = Icons.Default.Visibility,
                )
            }
        }
    }
}

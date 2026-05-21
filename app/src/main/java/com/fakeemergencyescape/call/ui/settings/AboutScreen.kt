package com.fakeemergencyescape.call.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.BuildConfig
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.legal.LegalUrls
import com.fakeemergencyescape.call.ui.components.AppScaffold
import com.fakeemergencyescape.call.ui.components.ElevatedAppCard
import com.fakeemergencyescape.call.ui.components.OpenLegalUrlButton

@Composable
fun AboutScreen(onBack: () -> Unit) {
    AppScaffold(
        title = { Text(stringResource(R.string.about_title), fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedAppCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.about_disclaimer),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = stringResource(R.string.about_ui_notice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.about_voice_messages),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.about_no_telephony),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OpenLegalUrlButton(
                        label = stringResource(R.string.legal_view_about_online),
                        url = LegalUrls.ABOUT,
                    )
                    OpenLegalUrlButton(
                        label = stringResource(R.string.legal_view_terms_online),
                        url = LegalUrls.TERMS,
                    )
                }
            }
        }
    }
}

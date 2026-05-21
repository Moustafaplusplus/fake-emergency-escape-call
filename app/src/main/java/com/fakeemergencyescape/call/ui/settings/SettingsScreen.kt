package com.fakeemergencyescape.call.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.ui.components.AppScaffold
import com.fakeemergencyescape.call.ui.components.ElevatedAppCard
import com.fakeemergencyescape.call.ui.permissions.PermissionsDashboard
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onAbout: () -> Unit,
    onPrivacy: () -> Unit,
    onTerms: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLocaleDialog by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (showLocaleDialog) {
        AlertDialog(
            onDismissRequest = { showLocaleDialog = false },
            title = { Text(stringResource(R.string.settings_tts_locale)) },
            text = {
                Column {
                    SettingsViewModel.LOCALE_OPTIONS.forEach { (tag, label) ->
                        TextButton(
                            onClick = {
                                viewModel.onLocaleSelected(tag)
                                showLocaleDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLocaleDialog = false }) {
                    Text(stringResource(R.string.nav_back))
                }
            },
        )
    }

    AppScaffold(
        title = {
            Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedAppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SliderSetting(
                label = stringResource(R.string.settings_tts_rate),
                value = uiState.speechRate,
                valueRange = 0.5f..2f,
                onValueChange = viewModel::onSpeechRateChange,
                valueLabel = String.format("%.1f", uiState.speechRate),
            )
            SliderSetting(
                label = stringResource(R.string.settings_tts_pitch),
                value = uiState.speechPitch,
                valueRange = 0.5f..2f,
                onValueChange = viewModel::onSpeechPitchChange,
                valueLabel = String.format("%.1f", uiState.speechPitch),
            )
            SettingsLinkRow(
                label = stringResource(R.string.settings_tts_locale),
                subtitle = uiState.localeLabel,
                onClick = { showLocaleDialog = true },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.settings_default_vibration))
                Switch(
                    checked = uiState.defaultVibration,
                    onCheckedChange = viewModel::onDefaultVibrationChange,
                )
            }
            OutlinedButton(
                onClick = viewModel::previewVoice,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isPreviewing,
            ) {
                Text(stringResource(R.string.settings_preview_voice), fontWeight = FontWeight.SemiBold)
            }
            uiState.previewMessage?.let { msg ->
                Text(
                    text = msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
                }
            }
            ElevatedAppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
            PermissionsDashboard(
                status = uiState.permissions,
                showDegradedHints = true,
                onOpenNotificationSettings = viewModel::openNotificationSettings,
                onOpenExactAlarmSettings = viewModel::openExactAlarmSettings,
                onOpenFullScreenIntentSettings = viewModel::openFullScreenIntentSettings,
                onOpenBatterySettings = viewModel::openBatterySettings,
            )
                }
            }
            ElevatedAppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            SettingsLinkRow(label = stringResource(R.string.settings_about), onClick = onAbout)
            SettingsLinkRow(label = stringResource(R.string.settings_privacy), onClick = onPrivacy)
            SettingsLinkRow(label = stringResource(R.string.settings_terms), onClick = onTerms)
                }
            }
            Spacer(modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueLabel: String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(
                valueLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

@Composable
private fun SettingsLinkRow(
    label: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
    }
}

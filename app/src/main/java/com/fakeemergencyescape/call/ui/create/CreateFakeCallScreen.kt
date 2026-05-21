package com.fakeemergencyescape.call.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.MessageType
import com.fakeemergencyescape.call.ui.components.AppScaffold
import com.fakeemergencyescape.call.ui.components.Chip3D
import com.fakeemergencyescape.call.ui.components.ElevatedAppCard
import com.fakeemergencyescape.call.ui.components.LoadingOverlay
import com.fakeemergencyescape.call.ui.components.Primary3DButton
import com.fakeemergencyescape.call.ui.components.Secondary3DButton
import com.fakeemergencyescape.call.ui.components.ShowMessageSnackbar
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateFakeCallScreen(
    viewModel: CreateFakeCallViewModel,
    onBack: () -> Unit,
    onScheduled: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var showTemplateSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val todayStartUtc = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    ShowMessageSnackbar(
        message = uiState.snackbarMessage,
        snackbarHostState = snackbarHostState,
        onShown = viewModel::clearSnackbarMessage,
    )

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.clearSaveSuccess()
            onScheduled()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = uiState.customTimeMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= todayStartUtc
        },
    )
    val calendar = remember(uiState.customTimeMillis) {
        Calendar.getInstance().apply { timeInMillis = uiState.customTimeMillis }
    }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false,
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selected ->
                            val c = Calendar.getInstance().apply { timeInMillis = uiState.customTimeMillis }
                            val picked = Calendar.getInstance().apply { timeInMillis = selected }
                            c.set(Calendar.YEAR, picked.get(Calendar.YEAR))
                            c.set(Calendar.MONTH, picked.get(Calendar.MONTH))
                            c.set(Calendar.DAY_OF_MONTH, picked.get(Calendar.DAY_OF_MONTH))
                            viewModel.onCustomTimeChange(c.timeInMillis)
                        }
                        showDatePicker = false
                        showTimePicker = true
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        val c = Calendar.getInstance().apply { timeInMillis = uiState.customTimeMillis }
                        c.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        c.set(Calendar.MINUTE, timePickerState.minute)
                        c.set(Calendar.SECOND, 0)
                        viewModel.onCustomTimeChange(c.timeInMillis)
                        showTimePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showTemplateSheet && templates.isNotEmpty()) {
        TemplatePickerSheet(
            templates = templates,
            onTemplateSelected = viewModel::onTemplateSelected,
            onDismiss = { showTemplateSheet = false },
        )
    }

    AppScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        title = {
            Text(
                when {
                    uiState.isEditMode -> stringResource(R.string.edit_title)
                    uiState.isDuplicateMode -> stringResource(R.string.duplicate_title)
                    else -> stringResource(R.string.create_title)
                },
                fontWeight = FontWeight.SemiBold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.nav_back))
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ElevatedAppCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
            OutlinedTextField(
                value = uiState.callerName,
                onValueChange = viewModel::onCallerNameChange,
                label = { Text(stringResource(R.string.caller_name_label)) },
                placeholder = { Text(stringResource(R.string.caller_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isReadOnly,
            )
            Text(
                text = stringResource(R.string.message_type_label),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ScheduleChip(
                    stringResource(R.string.message_type_text),
                    uiState.messageType == MessageType.TEXT,
                    !uiState.isReadOnly,
                ) {
                    viewModel.onMessageTypeChange(MessageType.TEXT)
                }
                ScheduleChip(
                    stringResource(R.string.message_type_voice),
                    uiState.messageType == MessageType.VOICE,
                    !uiState.isReadOnly,
                ) {
                    viewModel.onMessageTypeChange(MessageType.VOICE)
                }
            }
            when (uiState.messageType) {
                MessageType.TEXT -> {
                    Secondary3DButton(
                        text = stringResource(R.string.template_picker_button),
                        onClick = { showTemplateSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isReadOnly,
                    )
                    OutlinedTextField(
                        value = uiState.message,
                        onValueChange = viewModel::onMessageChange,
                        label = { Text(stringResource(R.string.message_label)) },
                        placeholder = { Text(stringResource(R.string.message_hint)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        minLines = 3,
                        enabled = !uiState.isReadOnly,
                    )
                }
                MessageType.VOICE -> {
                    VoiceMessageSection(
                        hasRecording = uiState.voiceMessagePath != null,
                        isRecording = uiState.isRecording,
                        recordingElapsedSec = uiState.recordingElapsedSec,
                        isPlayingPreview = uiState.isPlayingPreview,
                        enabled = !uiState.isReadOnly,
                        onStartRecording = viewModel::startRecording,
                        onStopRecording = viewModel::stopRecording,
                        onClearRecording = viewModel::clearVoiceRecording,
                        onTogglePreview = viewModel::togglePreviewPlayback,
                    )
                }
            }
            Text(text = stringResource(R.string.schedule_when_label), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ScheduleChip(stringResource(R.string.schedule_now), uiState.scheduleOption == ScheduleOption.NOW, !uiState.isReadOnly) {
                    viewModel.onScheduleOptionChange(ScheduleOption.NOW)
                }
                ScheduleChip(stringResource(R.string.schedule_30s), uiState.scheduleOption == ScheduleOption.SEC_30, !uiState.isReadOnly) {
                    viewModel.onScheduleOptionChange(ScheduleOption.SEC_30)
                }
                ScheduleChip(stringResource(R.string.schedule_1m), uiState.scheduleOption == ScheduleOption.MIN_1, !uiState.isReadOnly) {
                    viewModel.onScheduleOptionChange(ScheduleOption.MIN_1)
                }
                ScheduleChip(stringResource(R.string.schedule_5m), uiState.scheduleOption == ScheduleOption.MIN_5, !uiState.isReadOnly) {
                    viewModel.onScheduleOptionChange(ScheduleOption.MIN_5)
                }
                ScheduleChip(stringResource(R.string.schedule_15m), uiState.scheduleOption == ScheduleOption.MIN_15, !uiState.isReadOnly) {
                    viewModel.onScheduleOptionChange(ScheduleOption.MIN_15)
                }
                ScheduleChip(stringResource(R.string.schedule_custom), uiState.scheduleOption == ScheduleOption.CUSTOM, !uiState.isReadOnly) {
                    viewModel.onScheduleOptionChange(ScheduleOption.CUSTOM)
                    showDatePicker = true
                }
            }
            if (uiState.scheduleOption == ScheduleOption.CUSTOM) {
                val formatted = remember(uiState.customTimeMillis) {
                    SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
                        .format(Date(uiState.customTimeMillis))
                }
                Text(
                    text = formatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SettingRow(
                label = stringResource(R.string.vibration_enabled),
                checked = uiState.vibrationEnabled,
                onCheckedChange = viewModel::onVibrationChange,
                enabled = !uiState.isReadOnly,
            )
            SettingRow(
                label = stringResource(R.string.vibrate_only),
                checked = uiState.vibrateOnly,
                onCheckedChange = viewModel::onVibrateOnlyChange,
                enabled = !uiState.isReadOnly,
            )
                }
            }
            if (!uiState.exactAlarmsAllowed) {
                Text(
                    text = stringResource(R.string.exact_alarm_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            uiState.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Secondary3DButton(
                text = stringResource(R.string.try_call_30s),
                onClick = viewModel::scheduleQuickCallIn30Seconds,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSave && !uiState.isLoading && !uiState.isReadOnly,
            )
            Primary3DButton(
                text = if (uiState.isEditMode) {
                    stringResource(R.string.save_changes_button)
                } else {
                    stringResource(R.string.schedule_button)
                },
                onClick = viewModel::scheduleCall,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.canSave && !uiState.isLoading && !uiState.isReadOnly,
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
        if (uiState.isLoading) {
            LoadingOverlay()
        }
        }
    }
}

@Composable
private fun ScheduleChip(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Chip3D(label = label, selected = selected, enabled = enabled, onClick = onClick)
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

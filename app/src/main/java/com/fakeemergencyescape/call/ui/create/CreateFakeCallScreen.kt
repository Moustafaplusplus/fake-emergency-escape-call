package com.fakeemergencyescape.call.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.CallTemplate
import com.fakeemergencyescape.call.domain.model.MessageType
import com.fakeemergencyescape.call.ui.components.AppScaffold
import com.fakeemergencyescape.call.ui.components.Chip3D
import com.fakeemergencyescape.call.ui.components.DarkTextField
import com.fakeemergencyescape.call.ui.components.GradientOutlineButton
import com.fakeemergencyescape.call.ui.components.LoadingOverlay
import com.fakeemergencyescape.call.ui.components.Primary3DButton
import com.fakeemergencyescape.call.ui.components.SectionLabel
import com.fakeemergencyescape.call.ui.components.SegmentedOption
import com.fakeemergencyescape.call.ui.components.SegmentedToggleRow
import com.fakeemergencyescape.call.ui.components.ShowMessageSnackbar
import com.fakeemergencyescape.call.ui.theme.DarkOutline
import com.fakeemergencyescape.call.ui.theme.DarkSurfaceVariant
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
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(text = stringResource(R.string.caller_name_label))
                    DarkTextField(
                        value = uiState.callerName,
                        onValueChange = viewModel::onCallerNameChange,
                        placeholder = stringResource(R.string.caller_name_hint),
                        leadingIcon = Icons.Default.Person,
                        enabled = !uiState.isReadOnly,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel(text = stringResource(R.string.message_type_label))
                    SegmentedToggleRow(
                        options = listOf(
                            SegmentedOption(
                                label = stringResource(R.string.message_type_text),
                                icon = Icons.AutoMirrored.Filled.Message,
                            ),
                            SegmentedOption(
                                label = stringResource(R.string.message_type_voice),
                                icon = Icons.Default.Mic,
                            ),
                        ),
                        selectedIndex = if (uiState.messageType == MessageType.TEXT) 0 else 1,
                        onSelected = { index ->
                            viewModel.onMessageTypeChange(
                                if (index == 0) MessageType.TEXT else MessageType.VOICE,
                            )
                        },
                        enabled = !uiState.isReadOnly,
                    )
                }

                when (uiState.messageType) {
                    MessageType.TEXT -> {
                        if (templates.isNotEmpty()) {
                            TemplateQuickPickRow(
                                templates = templates.take(3),
                                onViewAll = { showTemplateSheet = true },
                                onTemplateSelected = viewModel::onTemplateSelected,
                            )
                        }
                        GradientOutlineButton(
                            text = stringResource(R.string.template_picker_button),
                            onClick = { showTemplateSheet = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isReadOnly,
                            leadingIcon = Icons.Default.Description,
                            trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val script = remember(uiState.scriptJson) {
                                com.fakeemergencyescape.call.domain.model.CallScriptCodec.decode(uiState.scriptJson)
                            }
                            SectionLabel(
                                text = if (script != null) {
                                    stringResource(R.string.script_conversation_label)
                                } else {
                                    stringResource(R.string.message_label)
                                },
                            )
                            if (script != null) {
                                ScriptConversationPreview(script = script)
                                if (!uiState.isReadOnly) {
                                    Text(
                                        text = stringResource(R.string.script_write_custom),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .clickable { viewModel.onClearScript() },
                                    )
                                }
                            } else {
                                DarkTextField(
                                    value = uiState.message,
                                    onValueChange = viewModel::onMessageChange,
                                    placeholder = stringResource(R.string.message_hint),
                                    enabled = !uiState.isReadOnly,
                                    singleLine = false,
                                    minLines = 4,
                                )
                            }
                        }
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

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionLabel(text = stringResource(R.string.schedule_when_label))
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkOutline.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Text(
                                text = formatted,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkOutline.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    SectionLabel(
                        text = stringResource(R.string.vibration_section_label),
                        modifier = Modifier.padding(top = 12.dp, bottom = 0.dp),
                    )
                    SettingRow(
                        label = stringResource(R.string.vibration_enabled),
                        icon = Icons.Default.Vibration,
                        checked = uiState.vibrationEnabled,
                        onCheckedChange = viewModel::onVibrationChange,
                        enabled = !uiState.isReadOnly,
                    )
                    SettingRow(
                        label = stringResource(R.string.vibrate_only),
                        icon = Icons.Default.VolumeOff,
                        checked = uiState.vibrateOnly,
                        onCheckedChange = viewModel::onVibrateOnlyChange,
                        enabled = !uiState.isReadOnly,
                    )
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
                Spacer(modifier = Modifier.height(24.dp))
            }
            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
private fun TemplateQuickPickRow(
    templates: List<CallTemplate>,
    onViewAll: () -> Unit,
    onTemplateSelected: (CallTemplate) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = stringResource(R.string.template_quick_pick_label), modifier = Modifier.padding(bottom = 0.dp))
            Text(
                text = stringResource(R.string.template_view_all),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onViewAll),
            )
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(templates, key = { it.id }) { template ->
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkOutline.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .clickable { onTemplateSelected(template) }
                        .padding(14.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = template.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                        )
                    }
                }
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = DarkOutline,
            ),
        )
    }
}

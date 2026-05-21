package com.fakeemergencyescape.call.ui.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.data.repository.FakeCallRepository.Companion.VIBRATE_ONLY_URI
import com.fakeemergencyescape.call.data.repository.SettingsRepository
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.CallTemplate
import com.fakeemergencyescape.call.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateFakeCallUiState(
    val callerName: String = "",
    val message: String = "",
    val scheduleOption: ScheduleOption = ScheduleOption.MIN_5,
    val customTimeMillis: Long = System.currentTimeMillis() + 3600_000,
    val vibrationEnabled: Boolean = true,
    val vibrateOnly: Boolean = false,
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val canSave: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val snackbarMessage: String? = null,
    val exactAlarmsAllowed: Boolean = true,
    val isReadOnly: Boolean = false,
)

@HiltViewModel
class CreateFakeCallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FakeCallRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val editCallId: String? = savedStateHandle.get<String>(Routes.ARG_FAKE_CALL_ID)

    private val _uiState = MutableStateFlow(
        CreateFakeCallUiState(
            isEditMode = editCallId != null,
            exactAlarmsAllowed = repository.canScheduleExactAlarms(),
        ),
    )
    val uiState: StateFlow<CreateFakeCallUiState> = _uiState.asStateFlow()

    val templates: StateFlow<List<CallTemplate>> = repository.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repository.observeTemplates().first()
            editCallId?.let { id ->
                _uiState.update { it.copy(isLoading = true) }
                val call = repository.getById(id)
                if (call != null) {
                    val readOnly = call.status == CallStatus.RINGING
                    _uiState.update {
                        it.copy(
                            callerName = call.callerName,
                            message = call.message,
                            customTimeMillis = call.scheduledAtMillis,
                            scheduleOption = ScheduleOption.CUSTOM,
                            vibrationEnabled = call.vibrationEnabled,
                            vibrateOnly = call.ringtoneUri == VIBRATE_ONLY_URI,
                            isLoading = false,
                            isReadOnly = readOnly,
                            canSave = !readOnly && call.callerName.isNotBlank() && call.message.isNotBlank(),
                            errorMessage = if (readOnly) {
                                "This call is ringing — wait until it ends before editing."
                            } else {
                                null
                            },
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Call not found") }
                }
            }
        }
    }

    fun onCallerNameChange(value: String) = updateForm { it.copy(callerName = value) }
    fun onMessageChange(value: String) = updateForm { it.copy(message = value) }
    fun onScheduleOptionChange(option: ScheduleOption) = updateForm { it.copy(scheduleOption = option) }
    fun onCustomTimeChange(millis: Long) = updateForm { it.copy(customTimeMillis = millis, scheduleOption = ScheduleOption.CUSTOM) }
    fun onVibrationChange(enabled: Boolean) = updateForm { it.copy(vibrationEnabled = enabled) }
    fun onVibrateOnlyChange(enabled: Boolean) = updateForm { it.copy(vibrateOnly = enabled) }
    fun onTemplateSelected(template: CallTemplate) = updateForm { it.copy(message = template.message) }
    fun clearSaveSuccess() = _uiState.update { it.copy(saveSuccess = false) }

    fun clearSnackbarMessage() = _uiState.update { it.copy(snackbarMessage = null) }

    fun scheduleTestIn30Seconds() {
        val state = _uiState.value
        if (state.callerName.isBlank() || state.message.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter caller name and message first") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rate = settingsRepository.ttsSpeechRate.first()
                val pitch = settingsRepository.ttsPitch.first()
                val locale = settingsRepository.ttsLocale.first().ifBlank {
                    java.util.Locale.getDefault().toLanguageTag()
                }
                repository.scheduleCall(
                    callerName = state.callerName,
                    message = state.message,
                    scheduledAtMillis = System.currentTimeMillis() + 30_000,
                    vibrationEnabled = state.vibrationEnabled,
                    vibrateOnly = state.vibrateOnly,
                    voiceLocale = locale,
                    speechRate = rate,
                    pitch = pitch,
                    existingId = editCallId,
                )
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Could not schedule test call")
                }
            }
        }
    }

    fun scheduleCall() {
        val state = _uiState.value
        if (state.isReadOnly) return
        if (state.callerName.isBlank() || state.message.isBlank()) {
            _uiState.update { it.copy(snackbarMessage = "Name and message are required") }
            return
        }
        val scheduledAt = ScheduleTime.toMillis(state.scheduleOption, state.customTimeMillis)
        if (!ScheduleTime.isValidFutureTime(scheduledAt)) {
            _uiState.update { it.copy(snackbarMessage = "Choose a future date and time") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, snackbarMessage = null) }
            try {
                val rate = settingsRepository.ttsSpeechRate.first()
                val pitch = settingsRepository.ttsPitch.first()
                val locale = settingsRepository.ttsLocale.first().ifBlank {
                    java.util.Locale.getDefault().toLanguageTag()
                }
                repository.scheduleCall(
                    callerName = state.callerName,
                    message = state.message,
                    scheduledAtMillis = scheduledAt,
                    vibrationEnabled = state.vibrationEnabled,
                    vibrateOnly = state.vibrateOnly,
                    voiceLocale = locale,
                    speechRate = rate,
                    pitch = pitch,
                    existingId = editCallId,
                )
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        snackbarMessage = e.message ?: "Could not save call",
                    )
                }
            }
        }
    }

    private fun updateForm(transform: (CreateFakeCallUiState) -> CreateFakeCallUiState) {
        _uiState.update { current ->
            if (current.isReadOnly) return@update current
            val next = transform(current)
            next.copy(
                canSave = next.callerName.isNotBlank() && next.message.isNotBlank(),
                errorMessage = null,
            )
        }
    }
}

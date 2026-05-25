package com.fakeemergencyescape.call.ui.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.data.repository.FakeCallRepository.Companion.VIBRATE_ONLY_URI
import com.fakeemergencyescape.call.data.repository.SettingsRepository
import com.fakeemergencyescape.call.domain.audio.VoiceMessagePlayer
import com.fakeemergencyescape.call.domain.audio.VoiceMessageRecorder
import com.fakeemergencyescape.call.domain.audio.VoiceMessageStorage
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.CallTemplate
import com.fakeemergencyescape.call.domain.model.MessageType
import com.fakeemergencyescape.call.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val messageType: MessageType = MessageType.TEXT,
    val voiceMessagePath: String? = null,
    val isRecording: Boolean = false,
    val recordingElapsedSec: Int = 0,
    val isPlayingPreview: Boolean = false,
    val scriptJson: String? = null,
    val scheduleOption: ScheduleOption = ScheduleOption.MIN_5,
    val customTimeMillis: Long = System.currentTimeMillis() + 3600_000,
    val vibrationEnabled: Boolean = true,
    val vibrateOnly: Boolean = false,
    val isEditMode: Boolean = false,
    val isDuplicateMode: Boolean = false,
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
    private val voiceMessageStorage: VoiceMessageStorage,
    private val voiceMessageRecorder: VoiceMessageRecorder,
    private val voiceMessagePlayer: VoiceMessagePlayer,
) : ViewModel() {

    private val editCallId: String? = savedStateHandle.get<String>(Routes.ARG_FAKE_CALL_ID)
    private val duplicateSourceId: String? = savedStateHandle.get<String>(Routes.ARG_SOURCE_CALL_ID)

    private val _uiState = MutableStateFlow(
        CreateFakeCallUiState(
            isEditMode = editCallId != null,
            isDuplicateMode = duplicateSourceId != null,
            exactAlarmsAllowed = repository.canScheduleExactAlarms(),
        ),
    )
    val uiState: StateFlow<CreateFakeCallUiState> = _uiState.asStateFlow()

    val templates: StateFlow<List<CallTemplate>> = repository.observeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var recordingTimerJob: Job? = null
    private var tempRecordingPath: String? = null

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
                            scriptJson = call.scriptJson,
                            messageType = call.messageType,
                            voiceMessagePath = call.voiceMessageUri,
                            customTimeMillis = call.scheduledAtMillis,
                            scheduleOption = ScheduleOption.CUSTOM,
                            vibrationEnabled = call.vibrationEnabled,
                            vibrateOnly = call.ringtoneUri == VIBRATE_ONLY_URI,
                            isLoading = false,
                            isReadOnly = readOnly,
                            canSave = !readOnly && hasValidMessage(
                                call.messageType,
                                call.message,
                                call.voiceMessageUri,
                                call.scriptJson,
                            ) && call.callerName.isNotBlank(),
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
            duplicateSourceId?.let { id ->
                _uiState.update { it.copy(isLoading = true) }
                val call = repository.getById(id)
                if (call != null) {
                    val messageText = when (call.messageType) {
                        MessageType.TEXT -> call.message
                        MessageType.VOICE -> ""
                    }
                    val voicePath = if (call.messageType == MessageType.VOICE) {
                        voiceMessageStorage.copyToTempFile(call.voiceMessageUri)
                    } else {
                        null
                    }
                    _uiState.update {
                        it.copy(
                            callerName = call.callerName,
                            message = messageText,
                            scriptJson = if (call.messageType == MessageType.TEXT) call.scriptJson else null,
                            messageType = call.messageType,
                            voiceMessagePath = voicePath,
                            scheduleOption = ScheduleOption.MIN_5,
                            customTimeMillis = ScheduleTime.toMillis(
                                ScheduleOption.MIN_5,
                                System.currentTimeMillis(),
                            ),
                            vibrationEnabled = call.vibrationEnabled,
                            vibrateOnly = call.ringtoneUri == VIBRATE_ONLY_URI,
                            isLoading = false,
                            isDuplicateMode = true,
                            isEditMode = false,
                            canSave = call.callerName.isNotBlank() && hasValidMessage(
                                call.messageType,
                                messageText,
                                voicePath,
                                if (call.messageType == MessageType.TEXT) call.scriptJson else null,
                            ),
                            snackbarMessage = if (call.messageType == MessageType.VOICE && voicePath == null) {
                                DUPLICATE_VOICE_COPY_FAILED
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

        viewModelScope.launch {
            voiceMessagePlayer.isPlaying.collect { playing ->
                _uiState.update { it.copy(isPlayingPreview = playing) }
            }
        }
    }

    fun onCallerNameChange(value: String) = updateForm { it.copy(callerName = value) }
    fun onMessageChange(value: String) = updateForm { it.copy(message = value, scriptJson = null) }

    fun onMessageTypeChange(type: MessageType) {
        updateForm { current ->
            if (type == MessageType.TEXT) {
                voiceMessagePlayer.stop()
                current.copy(messageType = type)
            } else {
                current.copy(messageType = type, scriptJson = null)
            }
        }
    }

    fun startRecording(): Boolean {
        if (_uiState.value.isReadOnly || _uiState.value.isRecording) return false
        voiceMessagePlayer.stop()
        val file = voiceMessageStorage.createTempRecordingFile()
        tempRecordingPath = file.absolutePath
        val started = voiceMessageRecorder.start(file)
        if (!started) {
            tempRecordingPath = null
            voiceMessageStorage.deleteFile(file.absolutePath)
            _uiState.update { it.copy(snackbarMessage = "Could not start recording") }
            return false
        }
        _uiState.update { it.copy(isRecording = true, recordingElapsedSec = 0) }
        recordingTimerJob?.cancel()
        recordingTimerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _uiState.update { it.copy(recordingElapsedSec = it.recordingElapsedSec + 1) }
            }
        }
        return true
    }

    fun stopRecording() {
        if (!_uiState.value.isRecording) return
        recordingTimerJob?.cancel()
        val path = voiceMessageRecorder.stop()
        _uiState.update { it.copy(isRecording = false) }
        if (path == null) {
            _uiState.update { it.copy(snackbarMessage = "Recording failed — try again") }
            return
        }
        _uiState.value.voiceMessagePath?.let { voiceMessageStorage.deleteFile(it) }
        updateForm { it.copy(voiceMessagePath = path) }
    }

    fun clearVoiceRecording() {
        voiceMessagePlayer.stop()
        voiceMessageRecorder.cancel()
        recordingTimerJob?.cancel()
        _uiState.value.voiceMessagePath?.let { voiceMessageStorage.deleteFile(it) }
        tempRecordingPath?.let { voiceMessageStorage.deleteFile(it) }
        tempRecordingPath = null
        updateForm {
            it.copy(
                voiceMessagePath = null,
                isRecording = false,
                recordingElapsedSec = 0,
            )
        }
    }

    fun togglePreviewPlayback() {
        val path = _uiState.value.voiceMessagePath ?: return
        if (_uiState.value.isPlayingPreview) {
            voiceMessagePlayer.stop()
        } else {
            voiceMessagePlayer.play(path)
        }
    }

    fun onScheduleOptionChange(option: ScheduleOption) = updateForm { it.copy(scheduleOption = option) }
    fun onCustomTimeChange(millis: Long) = updateForm { it.copy(customTimeMillis = millis, scheduleOption = ScheduleOption.CUSTOM) }
    fun onVibrationChange(enabled: Boolean) = updateForm { it.copy(vibrationEnabled = enabled) }
    fun onVibrateOnlyChange(enabled: Boolean) = updateForm { it.copy(vibrateOnly = enabled) }
    fun onClearScript() = updateForm { it.copy(scriptJson = null, message = "") }

    fun onTemplateSelected(template: CallTemplate) = updateForm { current ->
        current.copy(
            message = template.message,
            scriptJson = template.scriptJson.takeIf { it.isNotBlank() },
            callerName = if (current.callerName.isBlank()) template.suggestedCallerName else current.callerName,
        )
    }
    fun clearSaveSuccess() = _uiState.update { it.copy(saveSuccess = false) }
    fun clearSnackbarMessage() = _uiState.update { it.copy(snackbarMessage = null) }

    fun scheduleQuickCallIn30Seconds() {
        val state = _uiState.value
        if (!isFormValid(state)) {
            _uiState.update { it.copy(errorMessage = validationError(state)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                saveCall(System.currentTimeMillis() + 30_000, state)
                _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Could not schedule call")
                }
            }
        }
    }

    fun scheduleCall() {
        val state = _uiState.value
        if (state.isReadOnly) return
        if (!isFormValid(state)) {
            _uiState.update { it.copy(snackbarMessage = validationError(state)) }
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
                saveCall(scheduledAt, state)
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

    private suspend fun saveCall(scheduledAtMillis: Long, state: CreateFakeCallUiState) {
        val rate = settingsRepository.ttsSpeechRate.first()
        val pitch = settingsRepository.ttsPitch.first()
        val locale = settingsRepository.ttsLocale.first().ifBlank {
            java.util.Locale.getDefault().toLanguageTag()
        }
        val displayMessage = when (state.messageType) {
            MessageType.TEXT -> {
                when {
                    state.message.isNotBlank() -> state.message
                    !state.scriptJson.isNullOrBlank() ->
                        com.fakeemergencyescape.call.domain.model.CallScriptCodec
                            .decode(state.scriptJson)?.lines?.firstOrNull()?.text.orEmpty()
                    else -> ""
                }
            }
            MessageType.VOICE -> state.message.ifBlank { VOICE_MESSAGE_LABEL }
        }
        repository.scheduleCall(
            callerName = state.callerName,
            message = displayMessage,
            messageType = state.messageType,
            scriptJson = state.scriptJson,
            voiceMessagePath = state.voiceMessagePath,
            scheduledAtMillis = scheduledAtMillis,
            vibrationEnabled = state.vibrationEnabled,
            vibrateOnly = state.vibrateOnly,
            voiceLocale = locale,
            speechRate = rate,
            pitch = pitch,
            existingId = editCallId,
        )
    }

    private fun updateForm(transform: (CreateFakeCallUiState) -> CreateFakeCallUiState) {
        _uiState.update { current ->
            if (current.isReadOnly) return@update current
            val next = transform(current)
            next.copy(
                canSave = next.callerName.isNotBlank() && hasValidMessage(
                    next.messageType,
                    next.message,
                    next.voiceMessagePath,
                    next.scriptJson,
                ),
                errorMessage = null,
            )
        }
    }

    private fun hasValidMessage(
        messageType: MessageType,
        message: String,
        voicePath: String?,
        scriptJson: String? = null,
    ): Boolean = when (messageType) {
        MessageType.TEXT -> message.isNotBlank() || !scriptJson.isNullOrBlank()
        MessageType.VOICE -> voiceMessageStorage.exists(voicePath)
    }

    private fun isFormValid(state: CreateFakeCallUiState): Boolean =
        state.callerName.isNotBlank() &&
            hasValidMessage(state.messageType, state.message, state.voiceMessagePath, state.scriptJson)

    private fun validationError(state: CreateFakeCallUiState): String = when {
        state.callerName.isBlank() -> "Enter caller name"
        state.messageType == MessageType.VOICE && !voiceMessageStorage.exists(state.voiceMessagePath) ->
            "Record a voice message first"
        else -> "Enter a message"
    }

    override fun onCleared() {
        voiceMessageRecorder.cancel()
        voiceMessagePlayer.stop()
        recordingTimerJob?.cancel()
        super.onCleared()
    }

    companion object {
        const val VOICE_MESSAGE_LABEL = "Voice message"
        const val DUPLICATE_VOICE_COPY_FAILED =
            "Could not copy voice recording — record again or use text"
    }
}

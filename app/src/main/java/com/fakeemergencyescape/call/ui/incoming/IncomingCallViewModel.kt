package com.fakeemergencyescape.call.ui.incoming

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.domain.audio.CallAudioRouter
import com.fakeemergencyescape.call.domain.audio.DeviceAudioHelper
import com.fakeemergencyescape.call.domain.audio.RingingController
import com.fakeemergencyescape.call.domain.audio.TextToSpeechManager
import com.fakeemergencyescape.call.domain.audio.TtsState
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.navigation.Routes
import com.fakeemergencyescape.call.notifications.CallNotificationManager
import com.fakeemergencyescape.call.ui.preview.PreviewCallData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class IncomingScreen {
    INCOMING,
    ACTIVE,
}

data class IncomingCallUiState(
    val callerName: String = "",
    val callId: String = "",
    val message: String = "",
    val screen: IncomingScreen = IncomingScreen.INCOMING,
    val isLoading: Boolean = true,
    val speakerOn: Boolean = false,
    val muted: Boolean = false,
    val callDurationSeconds: Int = 0,
    val isSpeaking: Boolean = false,
    val ttsError: String? = null,
    val showNoEarpieceHint: Boolean = false,
)

fun IncomingCallUiState.formattedCallDuration(): String {
    val minutes = callDurationSeconds / 60
    val seconds = callDurationSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: FakeCallRepository,
    private val ringingController: RingingController,
    private val callAudioRouter: CallAudioRouter,
    private val textToSpeechManager: TextToSpeechManager,
    private val deviceAudioHelper: DeviceAudioHelper,
    private val callNotificationManager: CallNotificationManager,
) : ViewModel() {

    private val callId: String = savedStateHandle.get<String>(Routes.ARG_FAKE_CALL_ID).orEmpty()

    private val _uiState = MutableStateFlow(IncomingCallUiState(callId = callId))
    val uiState: StateFlow<IncomingCallUiState> = _uiState.asStateFlow()

    private val _finish = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val finish: SharedFlow<Unit> = _finish.asSharedFlow()

    private var activeCall: FakeCall? = null
    private var speakJob: Job? = null
    private var durationJob: Job? = null

    init {
        viewModelScope.launch {
            val fromDb = repository.getById(callId)
            activeCall = fromDb
            val preview = PreviewCallData.findById(callId)
            val name = fromDb?.callerName ?: preview.callerName
            val message = fromDb?.message ?: preview.message
            _uiState.update {
                it.copy(
                    callerName = name,
                    message = message,
                    isLoading = false,
                    showNoEarpieceHint = !deviceAudioHelper.hasEarpiece(),
                )
            }
            if (fromDb?.status == CallStatus.ANSWERED) {
                startActiveCall(speakDelayMs = 0)
            }
        }

        viewModelScope.launch {
            repository.observeCall(callId).collect { call ->
                when (call?.status) {
                    CallStatus.MISSED,
                    CallStatus.DECLINED,
                    CallStatus.CANCELLED,
                    CallStatus.COMPLETED,
                    -> {
                        stopActiveAudio()
                        ringingController.stopRinging()
                        callNotificationManager.dismissIncomingNotification()
                        _finish.tryEmit(Unit)
                    }
                    else -> Unit
                }
            }
        }

        viewModelScope.launch {
            textToSpeechManager.state.collect { ttsState ->
                _uiState.update {
                    it.copy(isSpeaking = ttsState == TtsState.SPEAKING)
                }
            }
        }
    }

    fun onAnswer() {
        viewModelScope.launch {
            ringingController.stopRinging()
            callNotificationManager.dismissIncomingNotification()
            if (callId.isNotBlank() && !isPreviewCall()) {
                repository.answerCall(callId)
                activeCall = repository.getById(callId)
            } else {
                activeCall = activeCall?.copy(
                    callerName = _uiState.value.callerName,
                    message = _uiState.value.message,
                ) ?: PreviewCallData.findById(callId).let {
                    FakeCall(
                        id = callId,
                        callerName = it.callerName,
                        callerPhotoUri = null,
                        message = it.message,
                        scheduledAtMillis = 0,
                        ringtoneUri = null,
                        voiceLocale = Locale.getDefault().toLanguageTag(),
                        speechRate = 1f,
                        pitch = 1f,
                        vibrationEnabled = true,
                        status = CallStatus.ANSWERED,
                        createdAtMillis = 0,
                        updatedAtMillis = 0,
                    )
                }
            }
            startActiveCall(speakDelayMs = 800)
        }
    }

    fun onDecline() {
        viewModelScope.launch {
            stopActiveAudio()
            ringingController.stopRinging()
            callNotificationManager.dismissIncomingNotification()
            if (callId.isNotBlank() && !isPreviewCall()) {
                repository.declineCall(callId)
            }
            _finish.tryEmit(Unit)
        }
    }

    fun onToggleSpeaker() {
        val newSpeaker = !_uiState.value.speakerOn
        callAudioRouter.setSpeakerphoneOn(newSpeaker)
        _uiState.update { it.copy(speakerOn = newSpeaker) }
        if (_uiState.value.screen == IncomingScreen.ACTIVE && !_uiState.value.muted) {
            replayMessage()
        }
    }

    fun onToggleMute() {
        val newMuted = !_uiState.value.muted
        if (newMuted) {
            textToSpeechManager.stop()
        }
        _uiState.update { it.copy(muted = newMuted) }
        if (!newMuted && _uiState.value.screen == IncomingScreen.ACTIVE) {
            replayMessage()
        }
    }

    fun onReplay() {
        replayMessage()
    }

    /** Used by debug "Preview active call" — opens directly on the in-call UI. */
    fun showActivePreview() {
        if (!isPreviewCall() || _uiState.value.screen == IncomingScreen.ACTIVE) return
        viewModelScope.launch {
            val preview = PreviewCallData.findById(callId)
            activeCall = FakeCall(
                id = callId,
                callerName = _uiState.value.callerName.ifBlank { preview.callerName },
                callerPhotoUri = null,
                message = _uiState.value.message.ifBlank { preview.message },
                scheduledAtMillis = 0,
                ringtoneUri = null,
                voiceLocale = Locale.getDefault().toLanguageTag(),
                speechRate = 1f,
                pitch = 1f,
                vibrationEnabled = true,
                status = CallStatus.ANSWERED,
                createdAtMillis = 0,
                updatedAtMillis = 0,
            )
            startActiveCall(speakDelayMs = 0)
        }
    }

    fun onEndCall() {
        viewModelScope.launch {
            stopActiveAudio()
            callNotificationManager.dismissIncomingNotification()
            if (callId.isNotBlank() && !isPreviewCall()) {
                repository.completeCall(callId)
            }
            _finish.tryEmit(Unit)
        }
    }

    private fun startActiveCall(speakDelayMs: Long) {
        callAudioRouter.enterCallMode(speakerOn = false)
        durationJob?.cancel()
        _uiState.update {
            it.copy(
                screen = IncomingScreen.ACTIVE,
                speakerOn = false,
                muted = false,
                callDurationSeconds = 0,
                ttsError = null,
            )
        }
        startCallDurationTimer()
        speakJob?.cancel()
        speakJob = viewModelScope.launch {
            val ready = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
                textToSpeechManager.initialize { success ->
                    cont.resume(success) {}
                }
            }
            if (!ready) {
                _uiState.update {
                    it.copy(ttsError = "Speech not available. Install Google Text-to-speech from Play Store.")
                }
                return@launch
            }
            if (speakDelayMs > 0) delay(speakDelayMs)
            speakCurrentMessage()
        }
    }

    private fun replayMessage() {
        if (_uiState.value.muted) return
        speakJob?.cancel()
        textToSpeechManager.stop()
        speakJob = viewModelScope.launch {
            speakCurrentMessage()
        }
    }

    private fun startCallDurationTimer() {
        durationJob?.cancel()
        durationJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                _uiState.update { it.copy(callDurationSeconds = it.callDurationSeconds + 1) }
            }
        }
    }

    private fun speakCurrentMessage() {
        if (_uiState.value.muted) return
        val call = activeCall ?: return
        val text = call.message.ifBlank { _uiState.value.message }
        if (text.isBlank()) return

        textToSpeechManager.speak(
            text = text,
            locale = parseLocale(call.voiceLocale),
            rate = call.speechRate,
            pitch = call.pitch,
        )
    }

    private fun stopActiveAudio() {
        speakJob?.cancel()
        durationJob?.cancel()
        textToSpeechManager.stop()
        callAudioRouter.exitCallMode()
    }

    private fun parseLocale(tag: String): Locale {
        if (tag.isBlank()) return Locale.getDefault()
        val parts = tag.split("-", "_")
        return when (parts.size) {
            1 -> Locale(parts[0])
            2 -> Locale(parts[0], parts[1])
            else -> Locale.forLanguageTag(tag)
        }
    }

    private fun isPreviewCall(): Boolean =
        callId == PreviewCallData.PREVIEW_ID || callId.startsWith("sample-")

    override fun onCleared() {
        stopActiveAudio()
        super.onCleared()
    }
}

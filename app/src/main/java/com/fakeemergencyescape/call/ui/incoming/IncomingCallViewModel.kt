package com.fakeemergencyescape.call.ui.incoming

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.ActiveCallAppearanceRepository
import com.fakeemergencyescape.call.data.repository.CallAppearanceRepository
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.domain.model.ActiveCallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.CallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.DefaultActiveCallAppearance
import com.fakeemergencyescape.call.domain.audio.CallAudioRouter
import com.fakeemergencyescape.call.domain.audio.DeviceAudioHelper
import com.fakeemergencyescape.call.domain.audio.RingingController
import com.fakeemergencyescape.call.domain.audio.TextToSpeechManager
import com.fakeemergencyescape.call.domain.audio.TtsState
import com.fakeemergencyescape.call.domain.audio.VoiceMessagePlayer
import com.fakeemergencyescape.call.domain.audio.VoiceMessageStorage
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.domain.model.CallScriptCodec
import com.fakeemergencyescape.call.domain.model.MessageType
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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
    private val voiceMessagePlayer: VoiceMessagePlayer,
    private val voiceMessageStorage: VoiceMessageStorage,
    callAppearanceRepository: CallAppearanceRepository,
    activeCallAppearanceRepository: ActiveCallAppearanceRepository,
) : ViewModel() {

    private val callId: String = savedStateHandle.get<String>(Routes.ARG_FAKE_CALL_ID).orEmpty()

    private val _uiState = MutableStateFlow(IncomingCallUiState(callId = callId))
    val uiState: StateFlow<IncomingCallUiState> = _uiState.asStateFlow()

    val callAppearance: StateFlow<CallAppearanceSettings> = callAppearanceRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CallAppearanceSettings())

    val activeCallAppearance: StateFlow<ActiveCallAppearanceSettings> = activeCallAppearanceRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DefaultActiveCallAppearance)

    private val _finish = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val finish: SharedFlow<Unit> = _finish.asSharedFlow()

    private var activeCall: FakeCall? = null
    private var speakJob: Job? = null
    private var durationJob: Job? = null

    init {
        if (PreviewCallData.isPreviewCall(callId)) {
            viewModelScope.launch { loadPreviewCall() }
        } else {
            viewModelScope.launch {
                val fromDb = repository.getById(callId)
                activeCall = fromDb
                _uiState.update {
                    it.copy(
                        callerName = fromDb?.callerName.orEmpty(),
                        message = fromDb?.message.orEmpty(),
                        isLoading = false,
                        showNoEarpieceHint = !deviceAudioHelper.hasEarpiece(),
                    )
                }
                if (fromDb?.status == CallStatus.ANSWERED) {
                    startActiveCall(speakDelayMs = 0)
                }
            }
        }

        viewModelScope.launch {
            if (PreviewCallData.isPreviewCall(callId)) return@launch
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
                if (activeCall?.messageType != MessageType.VOICE) {
                    _uiState.update {
                        it.copy(isSpeaking = ttsState == TtsState.SPEAKING)
                    }
                }
            }
        }

        viewModelScope.launch {
            voiceMessagePlayer.isPlaying.collect { playing ->
                if (activeCall?.messageType == MessageType.VOICE) {
                    _uiState.update { it.copy(isSpeaking = playing) }
                }
            }
        }
    }

    fun onAnswer() {
        viewModelScope.launch {
            ringingController.stopRinging()
            callNotificationManager.dismissIncomingNotification()
            if (isPreviewCall()) {
                activeCall = PreviewCallData.findById(callId)
                startActiveCall(speakDelayMs = 800)
                return@launch
            }
            if (callId.isNotBlank()) {
                repository.answerCall(callId)
                activeCall = repository.getById(callId)
            }
            startActiveCall(speakDelayMs = 800)
        }
    }

    fun onDecline() {
        viewModelScope.launch {
            stopActiveAudio()
            ringingController.stopRinging()
            callNotificationManager.dismissIncomingNotification()
            if (isPreviewCall()) {
                _finish.tryEmit(Unit)
                return@launch
            }
            if (callId.isNotBlank()) {
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
            stopMessagePlayback()
        }
        _uiState.update { it.copy(muted = newMuted) }
        if (!newMuted && _uiState.value.screen == IncomingScreen.ACTIVE) {
            replayMessage()
        }
    }

    fun onReplay() {
        replayMessage()
    }

    fun onEndCall() {
        viewModelScope.launch {
            stopActiveAudio()
            callNotificationManager.dismissIncomingNotification()
            if (!isPreviewCall() && callId.isNotBlank()) {
                repository.completeCall(callId)
            }
            _finish.tryEmit(Unit)
        }
    }

    private suspend fun loadPreviewCall() {
        val preview = PreviewCallData.findById(callId) ?: return
        activeCall = preview
        _uiState.update {
            it.copy(
                callerName = preview.callerName,
                message = preview.message,
                isLoading = false,
                showNoEarpieceHint = !deviceAudioHelper.hasEarpiece(),
            )
        }
        if (callId == PreviewCallData.ACTIVE_ID) {
            startActiveCall(speakDelayMs = 0)
        }
    }

    private fun isPreviewCall(): Boolean = PreviewCallData.isPreviewCall(callId)

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
            if (speakDelayMs > 0) delay(speakDelayMs)
            playCurrentMessage()
        }
    }

    private fun replayMessage() {
        if (_uiState.value.muted) return
        speakJob?.cancel()
        stopMessagePlayback()
        speakJob = viewModelScope.launch {
            playCurrentMessage()
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

    private suspend fun playCurrentMessage() {
        if (_uiState.value.muted) return
        val call = activeCall ?: return
        when (call.messageType) {
            MessageType.VOICE -> playVoiceMessage(call.voiceMessageUri)
            MessageType.TEXT -> playTextMessage(call)
        }
    }

    private fun playVoiceMessage(uri: String?) {
        if (!voiceMessageStorage.exists(uri)) {
            _uiState.update {
                it.copy(ttsError = "Voice message file is missing. Re-record in the scheduled call.")
            }
            return
        }
        _uiState.update { it.copy(ttsError = null) }
        voiceMessagePlayer.play(uri!!)
    }

    private suspend fun playTextMessage(call: FakeCall) {
        val text = call.message.ifBlank { _uiState.value.message }
        if (text.isBlank() && call.scriptJson.isNullOrBlank()) return
        val ready = kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            textToSpeechManager.initialize { success ->
                cont.resume(success) {}
            }
        }
        if (!ready) {
            _uiState.update {
                it.copy(ttsError = "Speech not available. Check your device speech settings or install a speech app from the Play Store.")
            }
            return
        }
        _uiState.update { it.copy(ttsError = null) }
        val locale = parseLocale(call.voiceLocale)
        val script = CallScriptCodec.decode(call.scriptJson)
        if (script != null) {
            textToSpeechManager.speakSequence(
                lines = script.lines,
                locale = locale,
                rate = call.speechRate,
                pitch = call.pitch,
            )
        } else {
            textToSpeechManager.speak(
                text = text,
                locale = locale,
                rate = call.speechRate,
                pitch = call.pitch,
            )
        }
    }

    private fun stopMessagePlayback() {
        textToSpeechManager.stop()
        voiceMessagePlayer.stop()
    }

    private fun stopActiveAudio() {
        speakJob?.cancel()
        durationJob?.cancel()
        stopMessagePlayback()
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

    override fun onCleared() {
        stopActiveAudio()
        super.onCleared()
    }
}

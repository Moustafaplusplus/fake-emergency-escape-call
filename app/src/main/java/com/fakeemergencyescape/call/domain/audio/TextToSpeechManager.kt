package com.fakeemergencyescape.call.domain.audio

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TtsState {
    IDLE,
    INITIALIZING,
    READY,
    SPEAKING,
    ERROR,
}

@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var tts: TextToSpeech? = null

    private val _state = MutableStateFlow(TtsState.IDLE)
    val state: StateFlow<TtsState> = _state.asStateFlow()

    private val communicationAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private var onSpeakingDone: (() -> Unit)? = null

    fun initialize(onResult: (Boolean) -> Unit = {}) {
        if (tts != null) {
            onResult(_state.value == TtsState.READY || _state.value == TtsState.SPEAKING)
            return
        }
        _state.value = TtsState.INITIALIZING
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    engine.setAudioAttributes(communicationAttributes)
                }
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _state.value = TtsState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        _state.value = TtsState.READY
                        onSpeakingDone?.invoke()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _state.value = TtsState.ERROR
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _state.value = TtsState.ERROR
                    }
                })
                _state.value = TtsState.READY
                onResult(true)
            } else {
                _state.value = TtsState.ERROR
                onResult(false)
            }
        }
    }

    /**
     * Speak while [CallAudioRouter] is already in call mode.
     */
    fun speak(
        text: String,
        locale: Locale,
        rate: Float,
        pitch: Float,
        onDone: () -> Unit = {},
    ) {
        val engine = tts
        if (engine == null || _state.value == TtsState.ERROR) {
            onDone()
            return
        }
        onSpeakingDone = onDone
        val localeResult = engine.setLanguage(locale)
        if (localeResult == TextToSpeech.LANG_MISSING_DATA ||
            localeResult == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            engine.setLanguage(Locale.getDefault())
        }
        engine.setSpeechRate(rate)
        engine.setPitch(pitch)
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
    }

    fun stop() {
        tts?.stop()
        if (_state.value != TtsState.ERROR && _state.value != TtsState.IDLE) {
            _state.value = TtsState.READY
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _state.value = TtsState.IDLE
    }

    companion object {
        private const val UTTERANCE_ID = "fake_call_utterance"
    }
}

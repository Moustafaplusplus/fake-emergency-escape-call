package com.fakeemergencyescape.call.domain.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class VoiceMessagePlayer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val communicationAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    fun play(filePath: String, onDone: () -> Unit = {}) {
        stop()
        val file = File(filePath)
        if (!file.exists()) {
            onDone()
            return
        }
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                setAudioAttributes(communicationAttributes)
                setOnCompletionListener {
                    _isPlaying.value = false
                    onDone()
                }
                setOnErrorListener { _, _, _ ->
                    _isPlaying.value = false
                    onDone()
                    true
                }
                prepare()
                start()
                _isPlaying.value = true
            }
        } catch (_: Exception) {
            _isPlaying.value = false
            onDone()
        }
    }

    fun stop() {
        mediaPlayer?.run {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
    }
}

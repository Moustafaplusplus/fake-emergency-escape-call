package com.fakeemergencyescape.call.domain.audio

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Phase 5: full TTS integration; Phase 4 enters call mode on answer. */
@Singleton
class CallAudioRouter @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val audioManager = context.getSystemService(AudioManager::class.java)
    private var previousMode = AudioManager.MODE_NORMAL

    fun enterCallMode(speakerOn: Boolean = false) {
        previousMode = audioManager.mode
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = speakerOn
        @Suppress("DEPRECATION")
        audioManager.requestAudioFocus(
            null,
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
        )
    }

    fun setSpeakerphoneOn(enabled: Boolean) {
        audioManager.isSpeakerphoneOn = enabled
    }

    fun exitCallMode() {
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = previousMode
        audioManager.abandonAudioFocus(null)
    }
}

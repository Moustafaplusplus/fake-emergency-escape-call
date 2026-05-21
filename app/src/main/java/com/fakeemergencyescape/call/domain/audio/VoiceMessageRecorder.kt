package com.fakeemergencyescape.call.domain.audio

import android.media.MediaRecorder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context

@Singleton
class VoiceMessageRecorder @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    val isRecording: Boolean
        get() = mediaRecorder != null

    fun start(outputFile: File): Boolean {
        stop()
        return try {
            this.outputFile = outputFile
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            true
        } catch (_: Exception) {
            stop()
            false
        }
    }

    fun stop(): String? {
        val file = outputFile
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            outputFile = null
            file?.takeIf { it.exists() && it.length() > 0 }?.absolutePath
        } catch (_: Exception) {
            mediaRecorder?.release()
            mediaRecorder = null
            outputFile = null
            file?.delete()
            null
        }
    }

    fun cancel() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
            mediaRecorder?.release()
        }
        mediaRecorder = null
        outputFile?.delete()
        outputFile = null
    }
}

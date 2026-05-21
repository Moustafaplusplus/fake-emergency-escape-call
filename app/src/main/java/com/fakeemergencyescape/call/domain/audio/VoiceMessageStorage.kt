package com.fakeemergencyescape.call.domain.audio

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceMessageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val voiceDir: File
        get() = File(context.filesDir, "voice_messages").apply { mkdirs() }

    fun createTempRecordingFile(): File =
        File(voiceDir, "temp_${System.currentTimeMillis()}.m4a")

    fun fileForCall(callId: String): File =
        File(voiceDir, "$callId.m4a")

    fun finalizeRecording(sourcePath: String, callId: String): String {
        val source = File(sourcePath)
        val dest = fileForCall(callId)
        if (source.absolutePath != dest.absolutePath) {
            source.copyTo(dest, overwrite = true)
            source.delete()
        }
        return dest.absolutePath
    }

    fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        File(path).delete()
    }

    fun deleteForCall(callId: String) {
        fileForCall(callId).delete()
    }

    fun exists(path: String?): Boolean =
        !path.isNullOrBlank() && File(path).exists() && File(path).length() > 0

    /** Copies an existing recording to a new temp file (for duplicating a call). */
    fun copyToTempFile(sourcePath: String?): String? {
        if (!exists(sourcePath)) return null
        val dest = createTempRecordingFile()
        return try {
            File(sourcePath!!).copyTo(dest, overwrite = true)
            dest.absolutePath
        } catch (_: Exception) {
            dest.delete()
            null
        }
    }
}

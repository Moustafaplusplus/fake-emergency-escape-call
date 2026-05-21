package com.fakeemergencyescape.call.domain

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallBackgroundStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private fun dirFor(subfolder: String): File =
        File(context.filesDir, subfolder).apply { mkdirs() }

    fun importFromUri(uri: Uri): String? = importFromUri(uri, "call_backgrounds", "background")

    fun importActiveFromUri(uri: Uri): String? = importFromUri(uri, "active_call_backgrounds", "active_bg")

    private fun importFromUri(uri: Uri, subfolder: String, prefix: String): String? = try {
        val dest = File(dirFor(subfolder), "${prefix}_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        }
        if (dest.exists() && dest.length() > 0) dest.absolutePath else null
    } catch (_: Exception) {
        null
    }

    fun exists(path: String?): Boolean =
        !path.isNullOrBlank() && File(path).exists() && File(path).length() > 0

    fun delete(path: String?) {
        if (!path.isNullOrBlank()) {
            runCatching { File(path).delete() }
        }
    }
}

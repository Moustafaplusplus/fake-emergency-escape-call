package com.fakeemergencyescape.call.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannels @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun ensureCreated() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        // HIGH importance is required for full-screen intent when the app is in the background
        // (unlocked phone). Sound/vibration are disabled — the app plays its own ringtone.
        val ringingChannel = NotificationChannel(
            CHANNEL_FAKE_CALL_RINGING,
            "Incoming calls",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Opens the full-screen call UI when a scheduled call rings."
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(ringingChannel)
    }

    companion object {
        const val CHANNEL_FAKE_CALL_RINGING = "fake_call_ringing_v3"
    }
}

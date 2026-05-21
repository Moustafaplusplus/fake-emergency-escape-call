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

        val channel = NotificationChannel(
            CHANNEL_FAKE_CALL,
            "Simulated incoming calls",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerts when a scheduled simulated call is ringing"
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_FAKE_CALL = "fake_call_channel"
    }
}

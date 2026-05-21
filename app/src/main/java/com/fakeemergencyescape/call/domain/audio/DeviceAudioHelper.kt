package com.fakeemergencyescape.call.domain.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceAudioHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun hasEarpiece(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioManager = context.getSystemService(AudioManager::class.java)
            val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            if (outputs.any { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }) {
                return true
            }
        }
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }
}

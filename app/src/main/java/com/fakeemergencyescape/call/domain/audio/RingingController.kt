package com.fakeemergencyescape.call.domain.audio

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.service.FakeCallRingtoneService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RingingController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ringtonePlayer: RingtonePlayer,
    private val vibrationPlayer: VibrationPlayer,
) {
    fun startRinging(call: FakeCall) {
        val intent = Intent(context, FakeCallRingtoneService::class.java).apply {
            putExtra(FakeCallRingtoneService.EXTRA_FAKE_CALL_ID, call.id)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun stopRinging() {
        ringtonePlayer.stop()
        vibrationPlayer.stop()
        val stopIntent = Intent(context, FakeCallRingtoneService::class.java).apply {
            action = FakeCallRingtoneService.ACTION_STOP
        }
        context.startService(stopIntent)
    }
}

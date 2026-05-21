package com.fakeemergencyescape.call.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.domain.audio.RingtonePlayer
import com.fakeemergencyescape.call.domain.audio.VibrationPlayer
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.scheduler.AlarmConstants
import com.fakeemergencyescape.call.notifications.CallNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FakeCallRingtoneService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @Inject lateinit var repository: FakeCallRepository
    @Inject lateinit var ringtonePlayer: RingtonePlayer
    @Inject lateinit var vibrationPlayer: VibrationPlayer
    @Inject lateinit var callNotificationManager: CallNotificationManager
    override fun onBind(intent: Intent?) = null

    private var timeoutJob: Job? = null
    private var activeCallId: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopRingingAndSelf()
            return START_NOT_STICKY
        }

        val callId = intent?.getStringExtra(EXTRA_FAKE_CALL_ID)
        val callerName = intent?.getStringExtra(EXTRA_CALLER_NAME) ?: ""
        
        if (callId.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Fix: Call startForeground immediately to avoid ForegroundServiceDidNotStartInTimeException
        val initialNotification = callNotificationManager.buildRingingServiceNotification(callId, callerName)
        startForeground(CallNotificationManager.RINGING_NOTIFICATION_ID, initialNotification)

        wakeScreenForIncomingCall()
        callNotificationManager.postRingingNotification(callId, callerName)
        callNotificationManager.launchIncomingCallUi(this, callId)

        activeCallId = callId
        serviceScope.launch {
            val call = repository.getById(callId)
            if (call == null || call.status != CallStatus.RINGING) {
                Log.w(AlarmConstants.LOG_TAG, "Ring service: call $callId not RINGING, stopping")
                stopRingingAndSelf()
                return@launch
            }

            // Update notification with full call details if needed (callerName might have changed or was empty)
            val notification = callNotificationManager.buildRingingServiceNotification(call)
            startForeground(CallNotificationManager.RINGING_NOTIFICATION_ID, notification)
            callNotificationManager.postRingingNotification(call)

            val vibrateOnly = call.ringtoneUri == FakeCallRepository.VIBRATE_ONLY_URI
            if (!vibrateOnly) {
                val customUri = call.ringtoneUri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                ringtonePlayer.start(customUri)
            }
            if (call.vibrationEnabled) {
                vibrationPlayer.start()
            }

            startMissedTimeout(callId)

            Log.i(AlarmConstants.LOG_TAG, "Ring service started for $callId vibrateOnly=$vibrateOnly")
        }

        return START_STICKY
    }

    private fun startMissedTimeout(callId: String) {
        timeoutJob?.cancel()
        timeoutJob = serviceScope.launch {
            delay(RING_TIMEOUT_MS)
            val current = repository.getById(callId)
            if (current?.status == CallStatus.RINGING) {
                Log.i(AlarmConstants.LOG_TAG, "Ring timeout → MISSED for $callId")
                repository.missCall(callId)
                stopRingingAndSelf()
            }
        }
    }

    private fun wakeScreenForIncomingCall() {
        val powerManager = getSystemService(PowerManager::class.java) ?: return
        try {
            @Suppress("DEPRECATION")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "FakeEscapeCall:incoming",
            )
            wakeLock.acquire(20_000L)
        } catch (e: Exception) {
            Log.w(AlarmConstants.LOG_TAG, "Wake screen for incoming call failed", e)
        }
    }

    private fun stopRingingAndSelf() {
        timeoutJob?.cancel()
        timeoutJob = null
        activeCallId = null
        ringtonePlayer.stop()
        vibrationPlayer.stop()
        callNotificationManager.dismissIncomingNotification()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        serviceScope.cancel()
        ringtonePlayer.stop()
        vibrationPlayer.stop()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_FAKE_CALL_ID = AlarmConstants.EXTRA_FAKE_CALL_ID
        const val EXTRA_CALLER_NAME = "com.fakeemergencyescape.call.EXTRA_CALLER_NAME"
        const val ACTION_STOP = "com.fakeemergencyescape.call.ACTION_STOP_RING"
        private const val RING_TIMEOUT_MS = 45_000L
    }
}

package com.fakeemergencyescape.call.service

import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.domain.audio.RingtonePlayer
import com.fakeemergencyescape.call.domain.audio.VibrationPlayer
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.scheduler.AlarmConstants
import com.fakeemergencyescape.call.navigation.Routes
import com.fakeemergencyescape.call.notifications.CallNotificationManager
import com.fakeemergencyescape.call.permissions.PermissionManager
import com.fakeemergencyescape.call.ui.incoming.IncomingCallActivity
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
    @Inject lateinit var permissionManager: PermissionManager

    override fun onBind(intent: Intent?) = null

    private var timeoutJob: Job? = null
    private var activeCallId: String? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopRingingAndSelf()
            return START_NOT_STICKY
        }

        val callId = intent?.getStringExtra(EXTRA_FAKE_CALL_ID)
        if (callId.isNullOrBlank()) {
            stopSelf()
            return START_NOT_STICKY
        }

        activeCallId = callId
        serviceScope.launch {
            val call = repository.getById(callId)
            if (call == null || call.status != CallStatus.RINGING) {
                Log.w(AlarmConstants.LOG_TAG, "Ring service: call $callId not RINGING, stopping")
                stopRingingAndSelf()
                return@launch
            }

            val notification = callNotificationManager.buildRingingNotification(call)
            startForeground(CallNotificationManager.RINGING_NOTIFICATION_ID, notification)

            val vibrateOnly = call.ringtoneUri == FakeCallRepository.VIBRATE_ONLY_URI
            if (!vibrateOnly) {
                val customUri = call.ringtoneUri?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                ringtonePlayer.start(customUri)
            }
            if (call.vibrationEnabled) {
                vibrationPlayer.start()
            }

            maybeLaunchIncomingUi(callId)
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

    private fun maybeLaunchIncomingUi(callId: String) {
        if (permissionManager.canUseFullScreenIntent() && !isAppInForeground()) {
            return
        }
        launchIncomingCallActivity(callId)
    }

    private fun isAppInForeground(): Boolean {
        val processes = getSystemService(ActivityManager::class.java).runningAppProcesses
            ?: return false
        return processes.any {
            it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                it.processName == packageName
        }
    }

    private fun launchIncomingCallActivity(callId: String) {
        val activityIntent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(Routes.ARG_FAKE_CALL_ID, callId)
        }
        startActivity(activityIntent)
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
        const val ACTION_STOP = "com.fakeemergencyescape.call.ACTION_STOP_RING"
        private const val RING_TIMEOUT_MS = 45_000L
    }
}

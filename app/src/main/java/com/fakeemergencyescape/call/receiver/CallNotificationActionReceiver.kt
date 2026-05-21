package com.fakeemergencyescape.call.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.domain.audio.RingingController
import com.fakeemergencyescape.call.domain.scheduler.AlarmConstants
import com.fakeemergencyescape.call.navigation.Routes
import com.fakeemergencyescape.call.notifications.CallNotificationActions
import com.fakeemergencyescape.call.notifications.CallNotificationManager
import com.fakeemergencyescape.call.ui.incoming.IncomingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallNotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: FakeCallRepository
    @Inject lateinit var ringingController: RingingController
    @Inject lateinit var callNotificationManager: CallNotificationManager

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val callId = intent.getStringExtra(CallNotificationActions.EXTRA_FAKE_CALL_ID)
        if (callId.isNullOrBlank()) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (action) {
                    CallNotificationActions.ACTION_ANSWER -> handleAnswer(context, callId)
                    CallNotificationActions.ACTION_DECLINE -> handleDecline(callId)
                }
            } catch (e: Exception) {
                Log.e(AlarmConstants.LOG_TAG, "Notification action failed: $action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun handleAnswer(context: Context, callId: String) {
        ringingController.stopRinging()
        callNotificationManager.dismissIncomingNotification()
        val activityIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(Routes.ARG_FAKE_CALL_ID, callId)
            putExtra(IncomingCallActivity.EXTRA_AUTO_ANSWER, true)
        }
        context.startActivity(activityIntent)
    }

    private suspend fun handleDecline(callId: String) {
        ringingController.stopRinging()
        val call = repository.getById(callId)
        if (call?.status == com.fakeemergencyescape.call.domain.model.CallStatus.RINGING) {
            repository.declineCall(callId)
        }
        callNotificationManager.dismissIncomingNotification()
    }
}

package com.fakeemergencyescape.call.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.domain.audio.RingingController
import com.fakeemergencyescape.call.domain.scheduler.AlarmConstants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FakeCallAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var repository: FakeCallRepository
    @Inject lateinit var ringingController: RingingController

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != AlarmConstants.ACTION_FAKE_CALL_ALARM) return

        val callId = intent.getStringExtra(AlarmConstants.EXTRA_FAKE_CALL_ID)
        if (callId.isNullOrBlank()) {
            Log.e(AlarmConstants.LOG_TAG, "Alarm received without fakeCallId")
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val triggered = repository.onAlarmTriggered(callId)
                Log.i(
                    AlarmConstants.LOG_TAG,
                    "Alarm fired: fakeCallId=$callId statusUpdated=$triggered",
                )
                if (triggered) {
                    val call = repository.getById(callId)
                    if (call != null) {
                        ringingController.startRinging(call)
                    }
                }
            } catch (e: Exception) {
                Log.e(AlarmConstants.LOG_TAG, "Error handling alarm for $callId", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

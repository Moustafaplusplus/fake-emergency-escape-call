package com.fakeemergencyescape.call.domain.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.receiver.FakeCallAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeCallScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
) {

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    fun schedule(call: FakeCall) {
        val triggerAt = call.scheduledAtMillis
        val now = System.currentTimeMillis()
        if (triggerAt <= now) {
            Log.w(
                AlarmConstants.LOG_TAG,
                "Skipping alarm in the past: callId=${call.id} triggerAt=$triggerAt now=$now",
            )
            return
        }

        val pendingIntent = buildPendingIntent(call.id)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent,
                )
                Log.i(
                    AlarmConstants.LOG_TAG,
                    "Exact alarm scheduled: callId=${call.id} at=$triggerAt",
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent,
                )
                Log.w(
                    AlarmConstants.LOG_TAG,
                    "Inexact alarm scheduled (exact not allowed): callId=${call.id} at=$triggerAt",
                )
            }
        } else {
            @Suppress("DEPRECATION")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            Log.i(AlarmConstants.LOG_TAG, "Exact alarm scheduled: callId=${call.id} at=$triggerAt")
        }
    }

    fun cancel(callId: String) {
        alarmManager.cancel(buildPendingIntent(callId))
        Log.i(AlarmConstants.LOG_TAG, "Alarm cancelled: callId=$callId")
    }

    fun reschedule(call: FakeCall) {
        cancel(call.id)
        schedule(call)
    }

    private fun buildPendingIntent(callId: String): PendingIntent {
        val intent = Intent(context, FakeCallAlarmReceiver::class.java).apply {
            action = AlarmConstants.ACTION_FAKE_CALL_ALARM
            putExtra(AlarmConstants.EXTRA_FAKE_CALL_ID, callId)
        }
        return PendingIntent.getBroadcast(
            context,
            pendingIntentRequestCode(callId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun pendingIntentRequestCode(callId: String): Int = callId.hashCode()
}

package com.fakeemergencyescape.call.notifications

import android.app.ActivityOptions
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.receiver.CallNotificationActionReceiver
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.domain.scheduler.AlarmConstants
import com.fakeemergencyescape.call.permissions.PermissionManager
import com.fakeemergencyescape.call.ui.incoming.IncomingCallActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationChannels: NotificationChannels,
    private val permissionManager: PermissionManager,
) {
    private val systemNotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

    fun buildRingingServiceNotification(call: FakeCall): Notification {
        return buildRingingServiceNotification(call.id, call.callerName)
    }

    fun buildRingingServiceNotification(callId: String, callerName: String): Notification {
        notificationChannels.ensureCreated()

        val displayName = callerName.ifBlank { context.getString(R.string.app_name) }
        val contentIntent = activityPendingIntent(callId, autoAnswer = false, kind = "content")
        val fullScreenIntent = activityPendingIntent(callId, autoAnswer = false, kind = "fsi")
        val answerIntent = actionPendingIntent(CallNotificationActions.ACTION_ANSWER, callId)
        val declineIntent = actionPendingIntent(CallNotificationActions.ACTION_DECLINE, callId)

        val builder = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_FAKE_CALL_RINGING)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val caller = Person.Builder().setName(displayName).build()
            builder.setStyle(
                NotificationCompat.CallStyle.forIncomingCall(caller, declineIntent, answerIntent),
            )
        } else {
            builder
                .setContentTitle(displayName)
                .setContentText(context.getString(R.string.notification_incoming_call))
        }

        if (permissionManager.canUseFullScreenIntent()) {
            builder.setFullScreenIntent(fullScreenIntent, true)
        }

        return builder.build()
    }

    fun dismissIncomingNotification() {
        systemNotificationManager.cancel(RINGING_NOTIFICATION_ID)
    }

    /**
     * Posts the ringing notification so NotificationManager can fire the full-screen intent
     * (system-allowlisted background activity start on Android 14+).
     */
    fun postRingingNotification(call: FakeCall) {
        postRingingNotification(call.id, call.callerName)
    }

    fun postRingingNotification(callId: String, callerName: String) {
        notificationChannels.ensureCreated()
        val notification = buildRingingServiceNotification(callId, callerName)
        notifyRinging(notification)
        Log.i(AlarmConstants.LOG_TAG, "Posted ringing notification for $callId fsiAllowed=${permissionManager.canUseFullScreenIntent()}")
    }

    /**
     * Best-effort direct launch while the alarm or ring service is active. PendingIntent.send()
     * is blocked on Android 15 when the app has no visible activity.
     */
    fun launchIncomingCallUi(
        launchContext: Context,
        callId: String,
        autoAnswer: Boolean = false,
    ) {
        val intent = IncomingCallActivity.createLaunchIntent(
            context = launchContext,
            callId = callId,
            autoAnswer = autoAnswer,
            dismissToHome = true,
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                launchContext.startActivity(intent, senderBalBundle())
            } else {
                launchContext.startActivity(intent)
            }
            Log.i(
                AlarmConstants.LOG_TAG,
                "Incoming call UI launched for $callId autoAnswer=$autoAnswer",
            )
        } catch (e: Exception) {
            Log.w(AlarmConstants.LOG_TAG, "Direct activity launch failed for $callId", e)
        }
    }

    private fun notifyRinging(notification: Notification) {
        systemNotificationManager.notify(RINGING_NOTIFICATION_ID, notification)
    }

    private fun actionPendingIntent(action: String, callId: String): PendingIntent {
        val intent = Intent(context, CallNotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(CallNotificationActions.EXTRA_FAKE_CALL_ID, callId)
        }
        return PendingIntent.getBroadcast(
            context,
            pendingRequestCode(action, callId, false),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun activityPendingIntent(
        callId: String,
        autoAnswer: Boolean,
        kind: String,
    ): PendingIntent {
        val intent = IncomingCallActivity.createLaunchIntent(
            context = context,
            callId = callId,
            autoAnswer = autoAnswer,
            dismissToHome = true,
        )
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent.getActivity(
                context,
                pendingRequestCode(kind, callId, autoAnswer),
                intent,
                flags,
                creatorBalBundle(),
            )
        } else {
            PendingIntent.getActivity(
                context,
                pendingRequestCode(kind, callId, autoAnswer),
                intent,
                flags,
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun backgroundActivityStartMode(): Int =
        @Suppress("DEPRECATION")
        ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun creatorBalBundle(): android.os.Bundle =
        ActivityOptions.makeBasic()
            .setPendingIntentCreatorBackgroundActivityStartMode(backgroundActivityStartMode())
            .toBundle()

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun senderBalBundle(): android.os.Bundle =
        ActivityOptions.makeBasic()
            .setPendingIntentBackgroundActivityStartMode(backgroundActivityStartMode())
            .toBundle()

    private fun pendingRequestCode(kind: String, callId: String, extra: Boolean): Int =
        (kind.hashCode() xor callId.hashCode() xor if (extra) 1 else 0)

    companion object {
        const val RINGING_NOTIFICATION_ID = 1001
    }
}

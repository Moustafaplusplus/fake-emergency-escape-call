package com.fakeemergencyescape.call.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.navigation.Routes
import com.fakeemergencyescape.call.permissions.PermissionManager
import com.fakeemergencyescape.call.receiver.CallNotificationActionReceiver
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

    fun buildRingingNotification(call: FakeCall): Notification {
        notificationChannels.ensureCreated()

        val contentIntent = activityPendingIntent(call.id, autoAnswer = false)
        val fullScreenPendingIntent = activityPendingIntent(call.id, autoAnswer = false)
        val answerIntent = actionPendingIntent(CallNotificationActions.ACTION_ANSWER, call.id)
        val declineIntent = actionPendingIntent(CallNotificationActions.ACTION_DECLINE, call.id)

        val builder = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_FAKE_CALL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_ringing_title, call.callerName))
            .setContentText(context.getString(R.string.notification_ringing_text))
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)
            .addAction(
                R.drawable.ic_notification_answer,
                context.getString(R.string.incoming_answer),
                answerIntent,
            )
            .addAction(
                R.drawable.ic_notification_decline,
                context.getString(R.string.incoming_decline),
                declineIntent,
            )

        if (permissionManager.canUseFullScreenIntent()) {
            builder.setFullScreenIntent(fullScreenPendingIntent, true)
        }

        return builder.build()
    }

    fun dismissIncomingNotification() {
        systemNotificationManager.cancel(RINGING_NOTIFICATION_ID)
    }

    fun notifyIncoming(call: FakeCall) {
        notificationChannels.ensureCreated()
        systemNotificationManager.notify(RINGING_NOTIFICATION_ID, buildRingingNotification(call))
    }

    private fun activityPendingIntent(callId: String, autoAnswer: Boolean): PendingIntent {
        val intent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Routes.ARG_FAKE_CALL_ID, callId)
            if (autoAnswer) {
                putExtra(IncomingCallActivity.EXTRA_AUTO_ANSWER, true)
            }
        }
        return PendingIntent.getActivity(
            context,
            pendingRequestCode("activity", callId, autoAnswer),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
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

    private fun pendingRequestCode(kind: String, callId: String, extra: Boolean): Int =
        (kind.hashCode() xor callId.hashCode() xor if (extra) 1 else 0)

    companion object {
        const val RINGING_NOTIFICATION_ID = 1001
    }
}

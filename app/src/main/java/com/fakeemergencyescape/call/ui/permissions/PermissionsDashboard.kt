package com.fakeemergencyescape.call.ui.permissions

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.R

@Composable
fun PermissionsDashboard(
    status: PermissionStatusState,
    showDegradedHints: Boolean,
    onOpenNotificationSettings: () -> Unit,
    onRequestNotifications: (() -> Unit)? = null,
    onOpenExactAlarmSettings: () -> Unit,
    onOpenFullScreenIntentSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.permissions_section_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.permissions_section_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        PermissionCheckRow(
            title = stringResource(R.string.permission_notifications_title),
            description = stringResource(R.string.permission_notifications_desc),
            granted = status.canPostNotifications,
            actionLabel = if (!status.canPostNotifications && onRequestNotifications != null) {
                stringResource(R.string.permission_grant)
            } else {
                stringResource(R.string.permission_open_settings)
            },
            showAction = !status.canPostNotifications,
            onAction = {
                if (!status.canPostNotifications && onRequestNotifications != null) {
                    onRequestNotifications()
                } else {
                    onOpenNotificationSettings()
                }
            },
        )
        if (showDegradedHints && !status.canPostNotifications) {
            DegradedHint(stringResource(R.string.permission_degraded_notifications))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionCheckRow(
                title = stringResource(R.string.permission_exact_alarms_title),
                description = stringResource(R.string.permission_exact_alarms_desc),
                granted = status.canScheduleExactAlarms,
                actionLabel = stringResource(R.string.permission_open_settings),
                showAction = !status.canScheduleExactAlarms,
                onAction = onOpenExactAlarmSettings,
            )
            if (showDegradedHints && !status.canScheduleExactAlarms) {
                DegradedHint(stringResource(R.string.permission_degraded_exact_alarms))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PermissionCheckRow(
                title = stringResource(R.string.permission_full_screen_title),
                description = stringResource(R.string.permission_full_screen_desc),
                granted = status.canUseFullScreenIntent,
                actionLabel = stringResource(R.string.permission_open_settings),
                showAction = !status.canUseFullScreenIntent,
                onAction = onOpenFullScreenIntentSettings,
            )
            if (showDegradedHints && !status.canUseFullScreenIntent) {
                DegradedHint(stringResource(R.string.permission_degraded_full_screen))
            }
        }

        PermissionCheckRow(
            title = stringResource(R.string.permission_battery_title),
            description = stringResource(R.string.permission_battery_desc),
            granted = status.isIgnoringBatteryOptimizations,
            actionLabel = stringResource(R.string.permission_open_settings),
            showAction = !status.isIgnoringBatteryOptimizations,
            onAction = onOpenBatterySettings,
        )
        if (showDegradedHints && !status.isIgnoringBatteryOptimizations) {
            DegradedHint(stringResource(R.string.permission_degraded_battery))
        }
    }
}

@Composable
private fun PermissionCheckRow(
    title: String,
    description: String,
    granted: Boolean,
    actionLabel: String,
    showAction: Boolean,
    onAction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = if (granted) "✅" else "❌",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        if (showAction) {
            OutlinedButton(onClick = onAction, modifier = Modifier.fillMaxWidth()) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun DegradedHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.tertiary,
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
    )
}

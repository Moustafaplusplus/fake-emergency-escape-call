package com.fakeemergencyescape.call.ui.permissions

data class PermissionStatusState(
    val canPostNotifications: Boolean = true,
    val canScheduleExactAlarms: Boolean = true,
    val canUseFullScreenIntent: Boolean = true,
    val isIgnoringBatteryOptimizations: Boolean = false,
)

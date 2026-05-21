package com.fakeemergencyescape.call.ui.create

enum class ScheduleOption {
    NOW,
    SEC_30,
    MIN_1,
    MIN_5,
    MIN_15,
    CUSTOM,
}

object ScheduleTime {
    private const val NOW_BUFFER_MS = 2_000L

    fun toMillis(option: ScheduleOption, customTimeMillis: Long): Long {
        val now = System.currentTimeMillis()
        return when (option) {
            ScheduleOption.NOW -> now + NOW_BUFFER_MS
            ScheduleOption.SEC_30 -> now + 30_000
            ScheduleOption.MIN_1 -> now + 60_000
            ScheduleOption.MIN_5 -> now + 5 * 60_000
            ScheduleOption.MIN_15 -> now + 15 * 60_000
            ScheduleOption.CUSTOM -> customTimeMillis.coerceAtLeast(now + NOW_BUFFER_MS)
        }
    }

    fun isValidFutureTime(scheduledAtMillis: Long): Boolean =
        scheduledAtMillis >= System.currentTimeMillis() + NOW_BUFFER_MS
}

package com.fakeemergencyescape.call.data.local

import androidx.room.TypeConverter
import com.fakeemergencyescape.call.domain.model.CallStatus

class CallStatusConverter {
    @TypeConverter
    fun fromStatus(status: CallStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): CallStatus = CallStatus.fromString(value)
}

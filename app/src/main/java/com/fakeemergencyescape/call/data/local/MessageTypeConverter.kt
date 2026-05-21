package com.fakeemergencyescape.call.data.local

import androidx.room.TypeConverter
import com.fakeemergencyescape.call.domain.model.MessageType

class MessageTypeConverter {
    @TypeConverter
    fun fromMessageType(type: MessageType): String = type.name

    @TypeConverter
    fun toMessageType(value: String): MessageType =
        MessageType.entries.find { it.name == value } ?: MessageType.TEXT
}

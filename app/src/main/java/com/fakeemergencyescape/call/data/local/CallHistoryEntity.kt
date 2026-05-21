package com.fakeemergencyescape.call.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "call_history",
    foreignKeys = [
        ForeignKey(
            entity = FakeCallEntity::class,
            parentColumns = ["id"],
            childColumns = ["fakeCallId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("fakeCallId")],
)
data class CallHistoryEntity(
    @PrimaryKey val id: String,
    val fakeCallId: String,
    val triggeredAtMillis: Long?,
    val answeredAtMillis: Long?,
    val declinedAtMillis: Long?,
    val completedAtMillis: Long?,
    val finalStatus: String,
)

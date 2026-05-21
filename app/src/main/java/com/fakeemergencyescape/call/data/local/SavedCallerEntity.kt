package com.fakeemergencyescape.call.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Schema for Phase 9 — saved callers UI. */
@Entity(tableName = "saved_callers")
data class SavedCallerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val photoUri: String?,
    val defaultMessage: String?,
    val createdAtMillis: Long,
)

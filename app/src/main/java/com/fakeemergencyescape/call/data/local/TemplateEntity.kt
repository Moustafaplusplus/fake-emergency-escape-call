package com.fakeemergencyescape.call.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val message: String,
    val scriptJson: String,
    val suggestedCallerName: String,
)

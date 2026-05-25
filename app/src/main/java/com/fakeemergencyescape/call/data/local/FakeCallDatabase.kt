package com.fakeemergencyescape.call.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FakeCallEntity::class,
        TemplateEntity::class,
        CallHistoryEntity::class,
        SavedCallerEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(CallStatusConverter::class, MessageTypeConverter::class)
abstract class FakeCallDatabase : RoomDatabase() {
    abstract fun fakeCallDao(): FakeCallDao
    abstract fun templateDao(): TemplateDao
    abstract fun callHistoryDao(): CallHistoryDao
}

package com.fakeemergencyescape.call.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE fake_calls ADD COLUMN messageType TEXT NOT NULL DEFAULT 'TEXT'",
        )
        db.execSQL(
            "ALTER TABLE fake_calls ADD COLUMN voiceMessageUri TEXT",
        )
    }
}

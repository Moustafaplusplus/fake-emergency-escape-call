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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE fake_calls ADD COLUMN scriptJson TEXT",
        )
        db.execSQL(
            "ALTER TABLE templates ADD COLUMN scriptJson TEXT NOT NULL DEFAULT ''",
        )
        db.execSQL(
            "ALTER TABLE templates ADD COLUMN suggestedCallerName TEXT NOT NULL DEFAULT ''",
        )
    }
}

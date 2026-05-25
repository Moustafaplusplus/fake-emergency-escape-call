package com.fakeemergencyescape.call.di

import android.content.Context
import androidx.room.Room
import com.fakeemergencyescape.call.data.local.CallHistoryDao
import com.fakeemergencyescape.call.data.local.FakeCallDao
import com.fakeemergencyescape.call.data.local.FakeCallDatabase
import com.fakeemergencyescape.call.data.local.MIGRATION_1_2
import com.fakeemergencyescape.call.data.local.MIGRATION_2_3
import com.fakeemergencyescape.call.data.local.TemplateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FakeCallDatabase =
        Room.databaseBuilder(
            context,
            FakeCallDatabase::class.java,
            "fake_call_database",
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun provideFakeCallDao(database: FakeCallDatabase): FakeCallDao = database.fakeCallDao()

    @Provides
    fun provideTemplateDao(database: FakeCallDatabase): TemplateDao = database.templateDao()

    @Provides
    fun provideCallHistoryDao(database: FakeCallDatabase): CallHistoryDao = database.callHistoryDao()
}

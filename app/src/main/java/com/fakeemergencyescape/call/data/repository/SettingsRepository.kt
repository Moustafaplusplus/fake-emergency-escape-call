package com.fakeemergencyescape.call.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    val defaultVibrationEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_VIBRATION] ?: true
    }

    val ttsSpeechRate: Flow<Float> = dataStore.data.map { prefs ->
        prefs[KEY_TTS_RATE] ?: 1f
    }

    val ttsPitch: Flow<Float> = dataStore.data.map { prefs ->
        prefs[KEY_TTS_PITCH] ?: 1f
    }

    val ttsLocale: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_TTS_LOCALE] ?: ""
    }

    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDefaultVibration(enabled: Boolean) {
        dataStore.edit { it[KEY_DEFAULT_VIBRATION] = enabled }
    }

    suspend fun setTtsSpeechRate(rate: Float) {
        dataStore.edit { it[KEY_TTS_RATE] = rate }
    }

    suspend fun setTtsPitch(pitch: Float) {
        dataStore.edit { it[KEY_TTS_PITCH] = pitch }
    }

    suspend fun setTtsLocale(localeTag: String) {
        dataStore.edit { it[KEY_TTS_LOCALE] = localeTag }
    }

    companion object {
        private val KEY_DEFAULT_VIBRATION = booleanPreferencesKey("default_vibration_enabled")
        private val KEY_TTS_RATE = floatPreferencesKey("tts_speech_rate")
        private val KEY_TTS_PITCH = floatPreferencesKey("tts_pitch")
        private val KEY_TTS_LOCALE = stringPreferencesKey("tts_locale")
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}

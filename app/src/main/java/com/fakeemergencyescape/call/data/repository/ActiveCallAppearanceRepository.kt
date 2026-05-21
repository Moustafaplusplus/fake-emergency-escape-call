package com.fakeemergencyescape.call.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fakeemergencyescape.call.domain.CallBackgroundStorage
import com.fakeemergencyescape.call.domain.model.ActiveCallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.CallBackgroundType
import com.fakeemergencyescape.call.domain.model.DefaultActiveCallAppearance
import com.fakeemergencyescape.call.domain.model.decodeActiveControls
import com.fakeemergencyescape.call.domain.model.encodeActiveControls
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.activeCallAppearanceStore: DataStore<Preferences> by preferencesDataStore(
    name = "active_call_appearance",
)

@Singleton
class ActiveCallAppearanceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backgroundStorage: CallBackgroundStorage,
) {
    private val store = context.activeCallAppearanceStore

    val settings: Flow<ActiveCallAppearanceSettings> = store.data.map(::readFromPrefs)

    suspend fun current(): ActiveCallAppearanceSettings = settings.first()

    suspend fun save(settings: ActiveCallAppearanceSettings) {
        store.edit { prefs -> writeToPrefs(prefs, settings) }
    }

    suspend fun resetToDefaults() {
        val oldPath = settings.first().backgroundImagePath
        backgroundStorage.delete(oldPath)
        store.edit { it.clear() }
    }

    private fun readFromPrefs(prefs: Preferences): ActiveCallAppearanceSettings {
        val rawType = prefs[KEY_BG_TYPE]?.toInt() ?: 0
        val backgroundType = when (rawType) {
            CallBackgroundType.IMAGE.ordinal -> CallBackgroundType.IMAGE
            CallBackgroundType.SOLID.ordinal -> CallBackgroundType.SOLID
            else -> CallBackgroundType.GRADIENT
        }
        return ActiveCallAppearanceSettings(
            backgroundType = backgroundType,
            gradientTopArgb = prefs[KEY_GRADIENT_TOP] ?: DefaultActiveCallAppearance.gradientTopArgb,
            gradientBottomArgb = prefs[KEY_GRADIENT_BOTTOM] ?: DefaultActiveCallAppearance.gradientBottomArgb,
            solidColorArgb = prefs[KEY_SOLID_COLOR] ?: DefaultActiveCallAppearance.solidColorArgb,
            backgroundImagePath = prefs[KEY_IMAGE_PATH],
            blurRadiusDp = prefs[KEY_BLUR] ?: 0f,
            overlayAlpha = prefs[KEY_OVERLAY] ?: 0.4f,
            controls = decodeActiveControls(prefs[KEY_CONTROLS]),
            endCallX = prefs[KEY_END_X] ?: 0.5f,
            endCallY = prefs[KEY_END_Y] ?: 0.88f,
            controlSizeScale = prefs[KEY_CONTROL_SIZE] ?: 1f,
            callerNameScale = prefs[KEY_NAME_SCALE] ?: 1f,
            avatarScale = prefs[KEY_AVATAR_SCALE] ?: 1f,
        )
    }

    private fun writeToPrefs(prefs: MutablePreferences, s: ActiveCallAppearanceSettings) {
        prefs[KEY_BG_TYPE] = s.backgroundType.ordinal.toLong()
        prefs[KEY_GRADIENT_TOP] = s.gradientTopArgb
        prefs[KEY_GRADIENT_BOTTOM] = s.gradientBottomArgb
        prefs[KEY_SOLID_COLOR] = s.solidColorArgb
        if (s.backgroundImagePath != null) {
            prefs[KEY_IMAGE_PATH] = s.backgroundImagePath
        } else {
            prefs.remove(KEY_IMAGE_PATH)
        }
        prefs[KEY_BLUR] = s.blurRadiusDp
        prefs[KEY_OVERLAY] = s.overlayAlpha
        prefs[KEY_CONTROLS] = encodeActiveControls(s.controls)
        prefs[KEY_END_X] = s.endCallX
        prefs[KEY_END_Y] = s.endCallY
        prefs[KEY_CONTROL_SIZE] = s.controlSizeScale
        prefs[KEY_NAME_SCALE] = s.callerNameScale
        prefs[KEY_AVATAR_SCALE] = s.avatarScale
    }

    companion object {
        private val KEY_BG_TYPE = longPreferencesKey("bg_type")
        private val KEY_GRADIENT_TOP = longPreferencesKey("gradient_top")
        private val KEY_GRADIENT_BOTTOM = longPreferencesKey("gradient_bottom")
        private val KEY_SOLID_COLOR = longPreferencesKey("solid_color")
        private val KEY_IMAGE_PATH = stringPreferencesKey("image_path")
        private val KEY_BLUR = floatPreferencesKey("blur")
        private val KEY_OVERLAY = floatPreferencesKey("overlay")
        private val KEY_CONTROLS = stringPreferencesKey("controls")
        private val KEY_END_X = floatPreferencesKey("end_x")
        private val KEY_END_Y = floatPreferencesKey("end_y")
        private val KEY_CONTROL_SIZE = floatPreferencesKey("control_size")
        private val KEY_NAME_SCALE = floatPreferencesKey("name_scale")
        private val KEY_AVATAR_SCALE = floatPreferencesKey("avatar_scale")
    }
}

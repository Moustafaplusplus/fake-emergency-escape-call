package com.fakeemergencyescape.call.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fakeemergencyescape.call.domain.CallBackgroundStorage
import com.fakeemergencyescape.call.domain.model.CallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.CallBackgroundType
import com.fakeemergencyescape.call.domain.model.DefaultCallAppearance
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.callAppearanceStore: DataStore<Preferences> by preferencesDataStore(
    name = "call_appearance",
)

@Singleton
class CallAppearanceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backgroundStorage: CallBackgroundStorage,
) {
    private val store = context.callAppearanceStore

    val settings: Flow<CallAppearanceSettings> = store.data.map(::readFromPrefs)

    suspend fun current(): CallAppearanceSettings = settings.first()

    suspend fun save(settings: CallAppearanceSettings) {
        store.edit { prefs -> writeToPrefs(prefs, settings) }
    }

    suspend fun resetToDefaults() {
        val oldPath = settings.first().backgroundImagePath
        backgroundStorage.delete(oldPath)
        store.edit { it.clear() }
    }

    suspend fun clearBackgroundImage() {
        val current = settings.first()
        backgroundStorage.delete(current.backgroundImagePath)
        save(
            current.copy(
                backgroundImagePath = null,
                backgroundType = if (current.backgroundType == CallBackgroundType.IMAGE) {
                    CallBackgroundType.GRADIENT
                } else {
                    current.backgroundType
                },
            ),
        )
    }

    private fun readFromPrefs(prefs: Preferences): CallAppearanceSettings {
        val rawType = prefs[KEY_BG_TYPE]?.toInt() ?: 0
        val backgroundType = when (rawType) {
            CallBackgroundType.IMAGE.ordinal -> CallBackgroundType.IMAGE
            CallBackgroundType.SOLID.ordinal -> CallBackgroundType.SOLID
            else -> CallBackgroundType.GRADIENT
        }
        val hasNewPositions = prefs[KEY_DECLINE_X] != null
        val declineX = prefs[KEY_DECLINE_X] ?: 0.28f
        val declineY = prefs[KEY_DECLINE_Y] ?: 0.82f
        val answerX = prefs[KEY_ANSWER_X] ?: 0.72f
        val answerY = prefs[KEY_ANSWER_Y] ?: 0.82f

        if (!hasNewPositions) {
            val legacyOffset = prefs[KEY_BUTTON_OFFSET] ?: 0f
            val legacyLayout = prefs[KEY_BUTTON_LAYOUT]?.toInt() ?: 0
            val migrated = migrateLegacyButtons(legacyLayout, legacyOffset)
            return CallAppearanceSettings(
                backgroundType = backgroundType,
                gradientTopArgb = prefs[KEY_GRADIENT_TOP] ?: DefaultCallAppearance.gradientTopArgb,
                gradientBottomArgb = prefs[KEY_GRADIENT_BOTTOM] ?: DefaultCallAppearance.gradientBottomArgb,
                solidColorArgb = prefs[KEY_SOLID_COLOR] ?: DefaultCallAppearance.solidColorArgb,
                backgroundImagePath = prefs[KEY_IMAGE_PATH],
                blurRadiusDp = prefs[KEY_BLUR] ?: 0f,
                overlayAlpha = prefs[KEY_OVERLAY] ?: 0.4f,
                declineButtonX = migrated[0],
                declineButtonY = migrated[1],
                answerButtonX = migrated[2],
                answerButtonY = migrated[3],
                buttonSizeScale = prefs[KEY_BUTTON_SIZE] ?: prefs[KEY_BUTTON_SPACING] ?: 1f,
                callerNameScale = prefs[KEY_NAME_SCALE] ?: 1f,
                avatarScale = prefs[KEY_AVATAR_SCALE] ?: 1f,
                showMobileLabel = prefs[KEY_SHOW_MOBILE] ?: true,
            )
        }

        return CallAppearanceSettings(
            backgroundType = backgroundType,
            gradientTopArgb = prefs[KEY_GRADIENT_TOP] ?: DefaultCallAppearance.gradientTopArgb,
            gradientBottomArgb = prefs[KEY_GRADIENT_BOTTOM] ?: DefaultCallAppearance.gradientBottomArgb,
            solidColorArgb = prefs[KEY_SOLID_COLOR] ?: DefaultCallAppearance.solidColorArgb,
            backgroundImagePath = prefs[KEY_IMAGE_PATH],
            blurRadiusDp = prefs[KEY_BLUR] ?: 0f,
            overlayAlpha = prefs[KEY_OVERLAY] ?: 0.4f,
            declineButtonX = declineX,
            declineButtonY = declineY,
            answerButtonX = answerX,
            answerButtonY = answerY,
            buttonSizeScale = prefs[KEY_BUTTON_SIZE] ?: 1f,
            callerNameScale = prefs[KEY_NAME_SCALE] ?: 1f,
            avatarScale = prefs[KEY_AVATAR_SCALE] ?: 1f,
            showMobileLabel = prefs[KEY_SHOW_MOBILE] ?: true,
        )
    }

    private fun migrateLegacyButtons(layout: Int, offsetX: Float): List<Float> {
        val shift = offsetX * 0.12f
        return when (layout) {
            1 -> listOf(0.72f + shift, 0.82f, 0.28f + shift, 0.82f)
            2 -> listOf(0.42f, 0.82f, 0.58f, 0.82f)
            else -> listOf(0.28f + shift, 0.82f, 0.72f + shift, 0.82f)
        }
    }

    private fun writeToPrefs(prefs: MutablePreferences, s: CallAppearanceSettings) {
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
        prefs[KEY_DECLINE_X] = s.declineButtonX
        prefs[KEY_DECLINE_Y] = s.declineButtonY
        prefs[KEY_ANSWER_X] = s.answerButtonX
        prefs[KEY_ANSWER_Y] = s.answerButtonY
        prefs[KEY_BUTTON_SIZE] = s.buttonSizeScale
        prefs[KEY_NAME_SCALE] = s.callerNameScale
        prefs[KEY_AVATAR_SCALE] = s.avatarScale
        prefs[KEY_SHOW_MOBILE] = s.showMobileLabel
    }

    companion object {
        private val KEY_BG_TYPE = longPreferencesKey("bg_type")
        private val KEY_GRADIENT_TOP = longPreferencesKey("gradient_top")
        private val KEY_GRADIENT_BOTTOM = longPreferencesKey("gradient_bottom")
        private val KEY_SOLID_COLOR = longPreferencesKey("solid_color")
        private val KEY_IMAGE_PATH = stringPreferencesKey("image_path")
        private val KEY_BLUR = floatPreferencesKey("blur")
        private val KEY_OVERLAY = floatPreferencesKey("overlay")
        private val KEY_DECLINE_X = floatPreferencesKey("decline_x")
        private val KEY_DECLINE_Y = floatPreferencesKey("decline_y")
        private val KEY_ANSWER_X = floatPreferencesKey("answer_x")
        private val KEY_ANSWER_Y = floatPreferencesKey("answer_y")
        private val KEY_BUTTON_SIZE = floatPreferencesKey("button_size")
        private val KEY_NAME_SCALE = floatPreferencesKey("name_scale")
        private val KEY_AVATAR_SCALE = floatPreferencesKey("avatar_scale")
        private val KEY_SHOW_MOBILE = booleanPreferencesKey("show_mobile")
        private val KEY_BUTTON_LAYOUT = longPreferencesKey("button_layout")
        private val KEY_BUTTON_OFFSET = floatPreferencesKey("button_offset")
        private val KEY_BUTTON_SPACING = floatPreferencesKey("button_spacing")
    }
}

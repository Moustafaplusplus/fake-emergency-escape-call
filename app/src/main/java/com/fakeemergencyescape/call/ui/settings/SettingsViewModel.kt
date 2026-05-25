package com.fakeemergencyescape.call.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.SettingsRepository
import com.fakeemergencyescape.call.domain.audio.CallAudioRouter
import com.fakeemergencyescape.call.domain.audio.TextToSpeechManager
import com.fakeemergencyescape.call.permissions.PermissionManager
import com.fakeemergencyescape.call.ui.permissions.PermissionStatusState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val speechRate: Float = 1f,
    val speechPitch: Float = 1f,
    val localeTag: String = "",
    val localeLabel: String = "System default",
    val defaultVibration: Boolean = true,
    val previewMessage: String? = null,
    val isPreviewing: Boolean = false,
    val permissions: PermissionStatusState = PermissionStatusState(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val textToSpeechManager: TextToSpeechManager,
    private val callAudioRouter: CallAudioRouter,
    private val permissionManager: PermissionManager,
) : ViewModel() {

    private val _previewMessage = MutableStateFlow<String?>(null)
    private val _isPreviewing = MutableStateFlow(false)
    private val _permissions = MutableStateFlow(PermissionStatusState())

    init {
        refreshPermissions()
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            settingsRepository.ttsSpeechRate,
            settingsRepository.ttsPitch,
            settingsRepository.ttsLocale,
            settingsRepository.defaultVibrationEnabled,
            _previewMessage,
        ) { rate, pitch, localeTag, vibration, previewMsg ->
            SettingsUiState(
                speechRate = rate,
                speechPitch = pitch,
                localeTag = localeTag,
                localeLabel = localeLabelForTag(localeTag),
                defaultVibration = vibration,
                previewMessage = previewMsg,
            )
        },
        _isPreviewing,
        _permissions,
    ) { state, previewing, permissions ->
        state.copy(isPreviewing = previewing, permissions = permissions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun refreshPermissions() {
        _permissions.value = PermissionStatusState(
            canPostNotifications = permissionManager.canPostNotifications(),
            canScheduleExactAlarms = permissionManager.canScheduleExactAlarms(),
            canUseFullScreenIntent = permissionManager.canUseFullScreenIntent(),
            isIgnoringBatteryOptimizations = permissionManager.isIgnoringBatteryOptimizations(),
        )
    }

    fun openExactAlarmSettings() = permissionManager.openExactAlarmSettings()

    fun openFullScreenIntentSettings() = permissionManager.openFullScreenIntentSettings()

    fun openBatterySettings() = permissionManager.openBatteryOptimizationSettings()

    fun openNotificationSettings() = permissionManager.openNotificationSettings()

    fun onSpeechRateChange(rate: Float) {
        viewModelScope.launch { settingsRepository.setTtsSpeechRate(rate) }
    }

    fun onSpeechPitchChange(pitch: Float) {
        viewModelScope.launch { settingsRepository.setTtsPitch(pitch) }
    }

    fun onDefaultVibrationChange(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDefaultVibration(enabled) }
    }

    fun onLocaleSelected(tag: String) {
        viewModelScope.launch { settingsRepository.setTtsLocale(tag) }
    }

    fun previewVoice() {
        viewModelScope.launch {
            _isPreviewing.value = true
            _previewMessage.value = null
            val state = uiState.value
            callAudioRouter.enterCallMode(speakerOn = false)
            textToSpeechManager.initialize { ready ->
                if (!ready) {
                    _previewMessage.value = "Speech not available. Check your device speech settings or install a speech app from the Play Store."
                    _isPreviewing.value = false
                    callAudioRouter.exitCallMode()
                    return@initialize
                }
                textToSpeechManager.speak(
                    text = "This is how your message will sound when you answer a call.",
                    locale = localeFromTag(state.localeTag),
                    rate = state.speechRate,
                    pitch = state.speechPitch,
                    onDone = {
                        callAudioRouter.exitCallMode()
                        _isPreviewing.value = false
                    },
                )
            }
        }
    }

    private fun localeFromTag(tag: String): Locale =
        if (tag.isBlank()) Locale.getDefault() else Locale.forLanguageTag(tag)

    private fun localeLabelForTag(tag: String): String = when (tag) {
        "" -> "System default"
        "en" -> "English"
        "ar" -> "Arabic"
        "fr" -> "French"
        "es" -> "Spanish"
        else -> tag
    }

    companion object {
        val LOCALE_OPTIONS = listOf(
            "" to "System default",
            "en" to "English",
            "ar" to "Arabic",
            "fr" to "French",
            "es" to "Spanish",
        )
    }
}

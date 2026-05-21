package com.fakeemergencyescape.call.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.FakeCallRepository
import com.fakeemergencyescape.call.data.repository.SettingsRepository
import com.fakeemergencyescape.call.permissions.PermissionManager
import com.fakeemergencyescape.call.ui.permissions.PermissionStatusState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val permissions: PermissionStatusState = PermissionStatusState(),
    val testCallScheduled: Boolean = false,
    val testCallMessage: String? = null,
    val isSchedulingTest: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val permissionManager: PermissionManager,
    private val settingsRepository: SettingsRepository,
    private val fakeCallRepository: FakeCallRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        _uiState.update {
            it.copy(
                permissions = PermissionStatusState(
                    canPostNotifications = permissionManager.canPostNotifications(),
                    canScheduleExactAlarms = permissionManager.canScheduleExactAlarms(),
                    canUseFullScreenIntent = permissionManager.canUseFullScreenIntent(),
                    isIgnoringBatteryOptimizations = permissionManager.isIgnoringBatteryOptimizations(),
                ),
                errorMessage = null,
            )
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                permissions = it.permissions.copy(canPostNotifications = granted),
            )
        }
        refreshPermissions()
    }

    fun openExactAlarmSettings() = permissionManager.openExactAlarmSettings()

    fun openFullScreenIntentSettings() = permissionManager.openFullScreenIntentSettings()

    fun openBatterySettings() = permissionManager.openBatteryOptimizationSettings()

    fun openNotificationSettings() = permissionManager.openNotificationSettings()

    fun scheduleTestCall() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSchedulingTest = true, errorMessage = null) }
            try {
                val rate = settingsRepository.ttsSpeechRate.first()
                val pitch = settingsRepository.ttsPitch.first()
                val locale = settingsRepository.ttsLocale.first().ifBlank {
                    java.util.Locale.getDefault().toLanguageTag()
                }
                val vibration = settingsRepository.defaultVibrationEnabled.first()
                fakeCallRepository.scheduleCall(
                    callerName = TEST_CALLER_NAME,
                    message = TEST_MESSAGE,
                    scheduledAtMillis = System.currentTimeMillis() + 30_000,
                    vibrationEnabled = vibration,
                    vibrateOnly = false,
                    voiceLocale = locale,
                    speechRate = rate,
                    pitch = pitch,
                )
                _uiState.update {
                    it.copy(
                        isSchedulingTest = false,
                        testCallScheduled = true,
                        testCallMessage = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSchedulingTest = false,
                        errorMessage = e.message ?: "Could not schedule test call",
                    )
                }
            }
        }
    }

    fun completeOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
            onDone()
        }
    }

    companion object {
        const val TEST_CALLER_NAME = "Test Call"
        const val TEST_MESSAGE = "This is a test of your simulated call. Tap Answer to hear this message."
    }
}

package com.fakeemergencyescape.call.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AppStartUiState(
    val isReady: Boolean = false,
    val onboardingCompleted: Boolean = false,
)

@HiltViewModel
class AppStartViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppStartUiState())
    val uiState: StateFlow<AppStartUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val onboardingCompleted = settingsRepository.onboardingCompleted.first()
            delay(SPLASH_MIN_DURATION_MS)
            _uiState.value = AppStartUiState(
                isReady = true,
                onboardingCompleted = onboardingCompleted,
            )
        }
    }

    companion object {
        const val SPLASH_MIN_DURATION_MS = 1_600L
    }
}

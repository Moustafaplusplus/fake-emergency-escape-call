package com.fakeemergencyescape.call.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class AppStartUiState(
    val isReady: Boolean = false,
    val onboardingCompleted: Boolean = false,
)

@HiltViewModel
class AppStartViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState: StateFlow<AppStartUiState> = settingsRepository.onboardingCompleted
        .map { completed ->
            AppStartUiState(isReady = true, onboardingCompleted = completed)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppStartUiState(isReady = false, onboardingCompleted = false),
        )
}

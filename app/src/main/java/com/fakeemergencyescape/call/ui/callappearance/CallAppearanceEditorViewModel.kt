package com.fakeemergencyescape.call.ui.callappearance

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.CallAppearanceRepository
import com.fakeemergencyescape.call.domain.CallBackgroundStorage
import com.fakeemergencyescape.call.domain.model.CallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.CallBackgroundType
import com.fakeemergencyescape.call.domain.model.CallColorPreset
import com.fakeemergencyescape.call.domain.model.CallColorPresets
import com.fakeemergencyescape.call.domain.model.DefaultCallAppearance
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CallAppearanceEditorUiState(
    val draft: CallAppearanceSettings = DefaultCallAppearance,
    val sidebarExpanded: Boolean = false,
    val saveState: AppearanceSaveState = AppearanceSaveState.Idle,
    val message: String? = null,
    val hasUnsavedChanges: Boolean = false,
)

@HiltViewModel
class CallAppearanceEditorViewModel @Inject constructor(
    private val repository: CallAppearanceRepository,
    private val backgroundStorage: CallBackgroundStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallAppearanceEditorUiState())
    val uiState: StateFlow<CallAppearanceEditorUiState> = _uiState.asStateFlow()

    private var savedBaseline: CallAppearanceSettings = DefaultCallAppearance

    init {
        viewModelScope.launch {
            val saved = repository.current()
            savedBaseline = saved
            _uiState.update { it.copy(draft = saved, hasUnsavedChanges = false) }
        }
    }

    fun toggleSidebar() = _uiState.update { it.copy(sidebarExpanded = !it.sidebarExpanded) }

    fun setSidebarExpanded(expanded: Boolean) = _uiState.update { it.copy(sidebarExpanded = expanded) }

    fun onBackgroundType(type: CallBackgroundType) {
        updateDraft {
            when (type) {
                CallBackgroundType.SOLID -> it.copy(
                    backgroundType = type,
                    solidColorArgb = it.solidColorArgb.takeIf { c ->
                        c != DefaultCallAppearance.solidColorArgb
                    } ?: it.gradientTopArgb,
                )
                else -> it.copy(backgroundType = type)
            }
        }
    }

    fun onPresetSelected(preset: CallColorPreset) {
        updateDraft {
            it.copy(
                backgroundType = CallBackgroundType.GRADIENT,
                gradientTopArgb = preset.top,
                gradientBottomArgb = preset.bottom,
                solidColorArgb = preset.top,
            )
        }
    }

    fun onBlurChange(value: Float) = updateDraft { it.copy(blurRadiusDp = value) }

    fun onOverlayChange(value: Float) = updateDraft { it.copy(overlayAlpha = value) }

    fun onDeclinePositionChange(x: Float, y: Float) = updateDraft { it.copy(declineButtonX = x, declineButtonY = y) }

    fun onAnswerPositionChange(x: Float, y: Float) = updateDraft { it.copy(answerButtonX = x, answerButtonY = y) }

    fun onButtonSizeChange(value: Float) = updateDraft { it.copy(buttonSizeScale = value) }

    fun onNameScaleChange(value: Float) = updateDraft { it.copy(callerNameScale = value) }

    fun onAvatarScaleChange(value: Float) = updateDraft { it.copy(avatarScale = value) }

    fun onShowMobileLabelChange(show: Boolean) = updateDraft { it.copy(showMobileLabel = show) }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            val path = backgroundStorage.importFromUri(uri)
            if (path == null) {
                _uiState.update { it.copy(message = "Could not load image") }
                return@launch
            }
            updateDraft {
                it.copy(backgroundType = CallBackgroundType.IMAGE, backgroundImagePath = path)
            }
            _uiState.update { it.copy(message = null) }
        }
    }

    fun clearImage() {
        viewModelScope.launch {
            backgroundStorage.delete(_uiState.value.draft.backgroundImagePath)
            updateDraft {
                it.copy(
                    backgroundImagePath = null,
                    backgroundType = if (it.backgroundType == CallBackgroundType.IMAGE) {
                        CallBackgroundType.GRADIENT
                    } else {
                        it.backgroundType
                    },
                )
            }
        }
    }

    fun save() {
        if (_uiState.value.saveState == AppearanceSaveState.Saving) return
        viewModelScope.launch {
            _uiState.update { it.copy(saveState = AppearanceSaveState.Saving, message = null) }
            val saved = _uiState.value.draft
            repository.save(saved)
            savedBaseline = saved
            _uiState.update { it.copy(saveState = AppearanceSaveState.Saved, hasUnsavedChanges = false) }
            delay(2_000)
            _uiState.update {
                if (it.saveState == AppearanceSaveState.Saved) {
                    it.copy(saveState = AppearanceSaveState.Idle)
                } else {
                    it
                }
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            backgroundStorage.delete(_uiState.value.draft.backgroundImagePath)
            _uiState.update {
                it.copy(
                    draft = DefaultCallAppearance,
                    message = null,
                    hasUnsavedChanges = DefaultCallAppearance != savedBaseline,
                )
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun updateDraft(transform: (CallAppearanceSettings) -> CallAppearanceSettings) {
        _uiState.update {
            val newDraft = transform(it.draft)
            it.copy(draft = newDraft, hasUnsavedChanges = newDraft != savedBaseline)
        }
    }

    val colorPresets: List<CallColorPreset> = CallColorPresets
}

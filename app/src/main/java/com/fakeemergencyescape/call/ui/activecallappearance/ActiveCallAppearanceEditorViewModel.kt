package com.fakeemergencyescape.call.ui.activecallappearance

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fakeemergencyescape.call.data.repository.ActiveCallAppearanceRepository
import com.fakeemergencyescape.call.domain.CallBackgroundStorage
import com.fakeemergencyescape.call.domain.model.ActiveCallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.ActiveCallControlId
import com.fakeemergencyescape.call.domain.model.CallBackgroundType
import com.fakeemergencyescape.call.domain.model.CallColorPreset
import com.fakeemergencyescape.call.domain.model.CallColorPresets
import com.fakeemergencyescape.call.domain.model.DefaultActiveCallAppearance
import com.fakeemergencyescape.call.domain.model.withControl
import com.fakeemergencyescape.call.ui.callappearance.AppearanceSaveState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveCallAppearanceEditorUiState(
    val draft: ActiveCallAppearanceSettings = DefaultActiveCallAppearance,
    val sidebarExpanded: Boolean = false,
    val saveState: AppearanceSaveState = AppearanceSaveState.Idle,
    val message: String? = null,
    val hasUnsavedChanges: Boolean = false,
)

@HiltViewModel
class ActiveCallAppearanceEditorViewModel @Inject constructor(
    private val repository: ActiveCallAppearanceRepository,
    private val backgroundStorage: CallBackgroundStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActiveCallAppearanceEditorUiState())
    val uiState: StateFlow<ActiveCallAppearanceEditorUiState> = _uiState.asStateFlow()

    private var savedBaseline: ActiveCallAppearanceSettings = DefaultActiveCallAppearance

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
                        c != DefaultActiveCallAppearance.solidColorArgb
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

    fun onControlPositionChange(id: ActiveCallControlId, x: Float, y: Float) {
        updateDraft { it.withControl(id) { placement -> placement.copy(x = x, y = y) } }
    }

    fun onControlRemove(id: ActiveCallControlId) {
        updateDraft { it.withControl(id) { placement -> placement.copy(visible = false) } }
    }

    fun onAddControl(id: ActiveCallControlId) {
        updateDraft { it.withControl(id) { placement -> placement.copy(visible = true) } }
    }

    fun onEndCallPositionChange(x: Float, y: Float) = updateDraft { it.copy(endCallX = x, endCallY = y) }

    fun onControlSizeChange(value: Float) = updateDraft { it.copy(controlSizeScale = value) }

    fun onNameScaleChange(value: Float) = updateDraft { it.copy(callerNameScale = value) }

    fun onAvatarScaleChange(value: Float) = updateDraft { it.copy(avatarScale = value) }

    fun onImagePicked(uri: Uri) {
        viewModelScope.launch {
            val path = backgroundStorage.importActiveFromUri(uri)
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
                    draft = DefaultActiveCallAppearance,
                    message = null,
                    hasUnsavedChanges = DefaultActiveCallAppearance != savedBaseline,
                )
            }
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    private fun updateDraft(transform: (ActiveCallAppearanceSettings) -> ActiveCallAppearanceSettings) {
        _uiState.update {
            val newDraft = transform(it.draft)
            it.copy(draft = newDraft, hasUnsavedChanges = newDraft != savedBaseline)
        }
    }

    val colorPresets: List<CallColorPreset> = CallColorPresets
}

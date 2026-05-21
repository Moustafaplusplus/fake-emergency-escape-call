package com.fakeemergencyescape.call.ui.activecallappearance

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.ActiveCallControlId
import com.fakeemergencyescape.call.ui.active.ActiveCallContent
import com.fakeemergencyescape.call.ui.active.activeCallControlUi
import com.fakeemergencyescape.call.ui.active.labelRes
import com.fakeemergencyescape.call.domain.model.hiddenControlIds
import com.fakeemergencyescape.call.ui.callappearance.AppearanceEditorTopBar
import com.fakeemergencyescape.call.ui.callappearance.AppearanceSaveState
import com.fakeemergencyescape.call.ui.callappearance.AppearanceSidebarSection
import com.fakeemergencyescape.call.ui.callappearance.rememberAppearanceEditorBackHandler
import com.fakeemergencyescape.call.ui.callappearance.AppearanceSliderRow
import com.fakeemergencyescape.call.ui.callappearance.CallBackgroundEditorSection
import com.fakeemergencyescape.call.ui.components.Chip3D
import com.fakeemergencyescape.call.ui.components.Primary3DButton
import com.fakeemergencyescape.call.ui.components.Secondary3DButton
import com.fakeemergencyescape.call.ui.theme.CallScreenTheme

private val AppearanceSidebarWidth = 320.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveCallAppearanceEditorScreen(
    onBack: () -> Unit,
    viewModel: ActiveCallAppearanceEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val draft = uiState.draft
    val handleBack = rememberAppearanceEditorBackHandler(
        hasUnsavedChanges = uiState.hasUnsavedChanges,
        onNavigateBack = onBack,
    )

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let(viewModel::onImagePicked) }

    LaunchedEffect(uiState.message) {
        if (uiState.message != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CallScreenTheme {
            ActiveCallContent(
                callerName = stringResource(R.string.call_appearance_preview_name),
                callDurationFormatted = "00:42",
                appearance = draft,
                muted = false,
                speakerOn = false,
                showNoEarpieceHint = false,
                ttsError = null,
                isSpeaking = false,
                controlUi = { id -> activeCallControlUi(id) },
                onControlClick = {},
                onEndCall = {},
                modifier = Modifier.fillMaxSize(),
                editable = true,
                onControlPositionChange = viewModel::onControlPositionChange,
                onControlRemove = viewModel::onControlRemove,
                onEndCallPositionChange = viewModel::onEndCallPositionChange,
            )
        }

        if (uiState.sidebarExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(end = AppearanceSidebarWidth)
                    .pointerInput(Unit) {
                        detectTapGestures { viewModel.setSidebarExpanded(false) }
                    },
            )
        }

        AppearanceEditorTopBar(
            onBack = handleBack,
            onOpenSettings = { viewModel.setSidebarExpanded(true) },
            message = uiState.message,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Text(
            text = stringResource(R.string.active_appearance_drag_hint),
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        )

        AnimatedVisibility(
            visible = uiState.sidebarExpanded,
            enter = slideInHorizontally(animationSpec = tween(280)) { it },
            exit = slideOutHorizontally(animationSpec = tween(280)) { it },
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            ActiveAppearanceSidebar(
                draft = draft,
                viewModel = viewModel,
                saveState = uiState.saveState,
                onClose = { viewModel.setSidebarExpanded(false) },
                onPickImage = { imagePicker.launch("image/*") },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveAppearanceSidebar(
    draft: com.fakeemergencyescape.call.domain.model.ActiveCallAppearanceSettings,
    viewModel: ActiveCallAppearanceEditorViewModel,
    saveState: AppearanceSaveState,
    onClose: () -> Unit,
    onPickImage: () -> Unit,
) {
    val saveLabelRes = when (saveState) {
        AppearanceSaveState.Saving -> R.string.call_appearance_saving
        AppearanceSaveState.Saved -> R.string.call_appearance_saved
        AppearanceSaveState.Idle -> R.string.call_appearance_save
    }
    val hidden = draft.hiddenControlIds()

    Surface(
        modifier = Modifier
            .width(AppearanceSidebarWidth)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.active_appearance_panel_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.call_appearance_close_panel))
                }
            }

            CallBackgroundEditorSection(
                appearance = draft,
                colorPresets = viewModel.colorPresets,
                onBackgroundType = viewModel::onBackgroundType,
                onPresetSelected = viewModel::onPresetSelected,
                onPickImage = onPickImage,
                onClearImage = viewModel::clearImage,
                onBlurChange = viewModel::onBlurChange,
                onOverlayChange = viewModel::onOverlayChange,
            )

            AppearanceSidebarSection(stringResource(R.string.active_appearance_controls)) {
                Text(
                    stringResource(R.string.active_appearance_drag_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.active_appearance_remove_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppearanceSliderRow(
                    label = stringResource(R.string.active_appearance_control_size),
                    value = draft.controlSizeScale,
                    range = 0.7f..1.3f,
                    valueLabel = String.format("%.0f%%", draft.controlSizeScale * 100),
                    onValueChange = viewModel::onControlSizeChange,
                )
            }

            if (hidden.isNotEmpty()) {
                AppearanceSidebarSection(stringResource(R.string.active_appearance_add_controls)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        hidden.forEach { id ->
                            Chip3D(
                                label = stringResource(id.labelRes()),
                                selected = false,
                                enabled = true,
                                onClick = { viewModel.onAddControl(id) },
                            )
                        }
                    }
                }
            }

            AppearanceSidebarSection(stringResource(R.string.call_appearance_caller_info)) {
                AppearanceSliderRow(
                    label = stringResource(R.string.call_appearance_name_size),
                    value = draft.callerNameScale,
                    range = 0.75f..1.35f,
                    valueLabel = String.format("%.0f%%", draft.callerNameScale * 100),
                    onValueChange = viewModel::onNameScaleChange,
                )
                AppearanceSliderRow(
                    label = stringResource(R.string.call_appearance_avatar_size),
                    value = draft.avatarScale,
                    range = 0.75f..1.35f,
                    valueLabel = String.format("%.0f%%", draft.avatarScale * 100),
                    onValueChange = viewModel::onAvatarScaleChange,
                )
            }

            Primary3DButton(
                text = stringResource(saveLabelRes),
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = saveState != AppearanceSaveState.Saving,
                success = saveState == AppearanceSaveState.Saved,
            )
            Secondary3DButton(
                text = stringResource(R.string.call_appearance_reset),
                onClick = viewModel::resetToDefaults,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

package com.fakeemergencyescape.call.ui.callappearance

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.fakeemergencyescape.call.domain.model.CallAppearanceSettings
import com.fakeemergencyescape.call.domain.model.CallBackgroundType
import com.fakeemergencyescape.call.domain.model.CallColorPreset
import com.fakeemergencyescape.call.ui.components.Chip3D
import com.fakeemergencyescape.call.ui.components.Primary3DButton
import com.fakeemergencyescape.call.ui.components.Secondary3DButton
import com.fakeemergencyescape.call.ui.incoming.IncomingCallContent
import com.fakeemergencyescape.call.ui.callappearance.rememberAppearanceEditorBackHandler
import com.fakeemergencyescape.call.ui.theme.CallScreenTheme

private val AppearanceSidebarWidth = 320.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallAppearanceEditorScreen(
    onBack: () -> Unit,
    viewModel: CallAppearanceEditorViewModel = hiltViewModel(),
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
            IncomingCallContent(
                callerName = stringResource(R.string.call_appearance_preview_name),
                appearance = draft,
                onAnswer = {},
                onDecline = {},
                modifier = Modifier.fillMaxSize(),
                draggableButtons = true,
                onDeclinePositionChange = viewModel::onDeclinePositionChange,
                onAnswerPositionChange = viewModel::onAnswerPositionChange,
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
            text = stringResource(R.string.call_appearance_drag_hint),
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
            AppearanceSidebar(
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
private fun AppearanceSidebar(
    draft: CallAppearanceSettings,
    viewModel: CallAppearanceEditorViewModel,
    saveState: AppearanceSaveState,
    onClose: () -> Unit,
    onPickImage: () -> Unit,
) {
    val saveLabelRes = when (saveState) {
        AppearanceSaveState.Saving -> R.string.call_appearance_saving
        AppearanceSaveState.Saved -> R.string.call_appearance_saved
        AppearanceSaveState.Idle -> R.string.call_appearance_save
    }
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
                    stringResource(R.string.call_appearance_panel_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.call_appearance_close_panel))
                }
            }

            SidebarSection(stringResource(R.string.call_appearance_background)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip3D(
                        stringResource(R.string.call_appearance_bg_gradient),
                        draft.backgroundType == CallBackgroundType.GRADIENT,
                        true,
                        { viewModel.onBackgroundType(CallBackgroundType.GRADIENT) },
                    )
                    Chip3D(
                        stringResource(R.string.call_appearance_bg_solid),
                        draft.backgroundType == CallBackgroundType.SOLID,
                        true,
                        { viewModel.onBackgroundType(CallBackgroundType.SOLID) },
                    )
                    Chip3D(
                        stringResource(R.string.call_appearance_bg_image),
                        draft.backgroundType == CallBackgroundType.IMAGE,
                        true,
                        { viewModel.onBackgroundType(CallBackgroundType.IMAGE) },
                    )
                }
                Text(
                    stringResource(R.string.call_appearance_color_presets),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    viewModel.colorPresets.forEach { preset ->
                        PresetSwatch(preset, onClick = { viewModel.onPresetSelected(preset) })
                    }
                }
                Secondary3DButton(
                    text = stringResource(R.string.call_appearance_pick_image),
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (draft.backgroundImagePath != null) {
                    Secondary3DButton(
                        text = stringResource(R.string.call_appearance_remove_image),
                        onClick = viewModel::clearImage,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            SidebarSection(stringResource(R.string.call_appearance_effects)) {
                SliderRow(
                    label = stringResource(R.string.call_appearance_blur),
                    value = draft.blurRadiusDp,
                    range = 0f..24f,
                    enabled = draft.backgroundType == CallBackgroundType.IMAGE,
                    hint = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                        stringResource(R.string.call_appearance_blur_unavailable)
                    } else {
                        null
                    },
                    onValueChange = viewModel::onBlurChange,
                )
                SliderRow(
                    label = stringResource(R.string.call_appearance_overlay),
                    value = draft.overlayAlpha,
                    range = 0f..0.75f,
                    valueLabel = "${(draft.overlayAlpha * 100).toInt()}%",
                    onValueChange = viewModel::onOverlayChange,
                )
            }

            SidebarSection(stringResource(R.string.call_appearance_buttons)) {
                Text(
                    stringResource(R.string.call_appearance_drag_buttons_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SliderRow(
                    label = stringResource(R.string.call_appearance_button_size),
                    value = draft.buttonSizeScale,
                    range = 0.7f..1.3f,
                    valueLabel = String.format("%.0f%%", draft.buttonSizeScale * 100),
                    onValueChange = viewModel::onButtonSizeChange,
                )
            }

            SidebarSection(stringResource(R.string.call_appearance_caller_info)) {
                SliderRow(
                    label = stringResource(R.string.call_appearance_name_size),
                    value = draft.callerNameScale,
                    range = 0.75f..1.35f,
                    valueLabel = String.format("%.0f%%", draft.callerNameScale * 100),
                    onValueChange = viewModel::onNameScaleChange,
                )
                SliderRow(
                    label = stringResource(R.string.call_appearance_avatar_size),
                    value = draft.avatarScale,
                    range = 0.75f..1.35f,
                    valueLabel = String.format("%.0f%%", draft.avatarScale * 100),
                    onValueChange = viewModel::onAvatarScaleChange,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.call_appearance_show_mobile))
                    Switch(checked = draft.showMobileLabel, onCheckedChange = viewModel::onShowMobileLabelChange)
                }
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

@Composable
private fun SidebarSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun PresetSwatch(preset: CallColorPreset, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(preset.top)),
        )
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(preset.bottom)),
        )
        Text(preset.label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueLabel: String? = null,
    enabled: Boolean = true,
    hint: String? = null,
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            if (valueLabel != null) {
                Text(valueLabel, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        hint?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range, enabled = enabled)
    }
}

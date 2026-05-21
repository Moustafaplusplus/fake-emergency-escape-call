package com.fakeemergencyescape.call.ui.callappearance

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.CallBackgroundAppearance
import com.fakeemergencyescape.call.domain.model.CallBackgroundType
import com.fakeemergencyescape.call.domain.model.CallColorPreset
import com.fakeemergencyescape.call.ui.components.Chip3D
import com.fakeemergencyescape.call.ui.components.Secondary3DButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallBackgroundEditorSection(
    appearance: CallBackgroundAppearance,
    colorPresets: List<CallColorPreset>,
    onBackgroundType: (CallBackgroundType) -> Unit,
    onPresetSelected: (CallColorPreset) -> Unit,
    onPickImage: () -> Unit,
    onClearImage: () -> Unit,
    onBlurChange: (Float) -> Unit,
    onOverlayChange: (Float) -> Unit,
) {
    AppearanceSidebarSection(stringResource(R.string.call_appearance_background)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip3D(
                stringResource(R.string.call_appearance_bg_gradient),
                appearance.backgroundType == CallBackgroundType.GRADIENT,
                true,
                { onBackgroundType(CallBackgroundType.GRADIENT) },
            )
            Chip3D(
                stringResource(R.string.call_appearance_bg_solid),
                appearance.backgroundType == CallBackgroundType.SOLID,
                true,
                { onBackgroundType(CallBackgroundType.SOLID) },
            )
            Chip3D(
                stringResource(R.string.call_appearance_bg_image),
                appearance.backgroundType == CallBackgroundType.IMAGE,
                true,
                { onBackgroundType(CallBackgroundType.IMAGE) },
            )
        }
        Text(
            stringResource(R.string.call_appearance_color_presets),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            colorPresets.forEach { preset ->
                ColorPresetSwatch(preset, onClick = { onPresetSelected(preset) })
            }
        }
        Secondary3DButton(
            text = stringResource(R.string.call_appearance_pick_image),
            onClick = onPickImage,
            modifier = Modifier.fillMaxWidth(),
        )
        if (appearance.backgroundImagePath != null) {
            Secondary3DButton(
                text = stringResource(R.string.call_appearance_remove_image),
                onClick = onClearImage,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    AppearanceSidebarSection(stringResource(R.string.call_appearance_effects)) {
        AppearanceSliderRow(
            label = stringResource(R.string.call_appearance_blur),
            value = appearance.blurRadiusDp,
            range = 0f..24f,
            enabled = appearance.backgroundType == CallBackgroundType.IMAGE,
            hint = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                stringResource(R.string.call_appearance_blur_unavailable)
            } else {
                null
            },
            onValueChange = onBlurChange,
        )
        AppearanceSliderRow(
            label = stringResource(R.string.call_appearance_overlay),
            value = appearance.overlayAlpha,
            range = 0f..0.75f,
            valueLabel = "${(appearance.overlayAlpha * 100).toInt()}%",
            onValueChange = onOverlayChange,
        )
    }
}

@Composable
fun AppearanceSidebarSection(title: String, content: @Composable () -> Unit) {
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
fun ColorPresetSwatch(preset: CallColorPreset, onClick: () -> Unit) {
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
fun AppearanceSliderRow(
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

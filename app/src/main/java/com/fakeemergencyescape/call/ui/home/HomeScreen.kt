package com.fakeemergencyescape.call.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.domain.model.MessageType
import com.fakeemergencyescape.call.ui.components.AppScreenBackground
import com.fakeemergencyescape.call.ui.components.ElevatedAppCard
import com.fakeemergencyescape.call.ui.components.Fab3D
import com.fakeemergencyescape.call.ui.components.IconButton3D
import com.fakeemergencyescape.call.ui.components.Primary3DButton
import com.fakeemergencyescape.call.ui.components.TextButton3D
import com.fakeemergencyescape.call.ui.components.SectionLabel
import com.fakeemergencyescape.call.ui.components.staggeredEntrance
import com.fakeemergencyescape.call.ui.theme.SecondaryAccent
import com.fakeemergencyescape.call.ui.theme.StatusCompleted
import com.fakeemergencyescape.call.ui.theme.StatusDeclined
import com.fakeemergencyescape.call.ui.theme.TertiaryAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onScheduleCall: () -> Unit,
    onEditCall: (String) -> Unit,
    onDuplicateCall: (String) -> Unit,
    onCallAppearance: () -> Unit,
    onActiveCallAppearance: () -> Unit,
    onPreviewIncoming: () -> Unit,
    onPreviewActive: () -> Unit,
    onSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var callToCancel by remember { mutableStateOf<FakeCall?>(null) }
    var callToDelete by remember { mutableStateOf<FakeCall?>(null) }

    callToCancel?.let { call ->
        AlertDialog(
            onDismissRequest = { callToCancel = null },
            title = { Text(stringResource(R.string.cancel_call_title)) },
            text = { Text(stringResource(R.string.cancel_call_message, call.callerName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelCall(call.id)
                        callToCancel = null
                    },
                ) { Text(stringResource(R.string.cancel_call_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { callToCancel = null }) {
                    Text(stringResource(R.string.nav_back))
                }
            },
        )
    }

    callToDelete?.let { call ->
        AlertDialog(
            onDismissRequest = { callToDelete = null },
            title = { Text(stringResource(R.string.delete_call_title)) },
            text = { Text(stringResource(R.string.delete_call_message, call.callerName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCall(call.id)
                        callToDelete = null
                    },
                ) { Text(stringResource(R.string.delete_call_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { callToDelete = null }) {
                    Text(stringResource(R.string.nav_back))
                }
            },
        )
    }

    AppScreenBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.home_title),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    actions = {
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.menu_settings))
                        }
                    },
                )
            },
            floatingActionButton = {
                Fab3D(
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_schedule_fab),
                    onClick = onScheduleCall,
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CallCustomizationHomeCard(
                            title = stringResource(R.string.home_call_appearance_title),
                            subtitle = stringResource(R.string.home_call_appearance_subtitle),
                            icon = Icons.Default.Palette,
                            iconGradient = listOf(TertiaryAccent, SecondaryAccent),
                            onClick = onCallAppearance,
                            modifier = Modifier.weight(1f),
                        )
                        CallCustomizationHomeCard(
                            title = stringResource(R.string.home_active_appearance_title),
                            subtitle = stringResource(R.string.home_active_appearance_subtitle),
                            icon = Icons.Default.PhoneInTalk,
                            iconGradient = listOf(TertiaryAccent, SecondaryAccent.copy(alpha = 0.85f)),
                            onClick = onActiveCallAppearance,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    CallPreviewHomeCard(
                        onPreviewIncoming = onPreviewIncoming,
                        onPreviewActive = onPreviewActive,
                    )
                }
                if (uiState.isEmpty) {
                    item {
                        HomeEmptyState(onScheduleCall = onScheduleCall)
                    }
                } else {
                    if (uiState.scheduledCalls.isNotEmpty()) {
                        item {
                            SectionLabel(
                                text = stringResource(R.string.section_upcoming),
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        itemsIndexed(uiState.scheduledCalls, key = { _, call -> call.id }) { index, call ->
                            FakeCallCard(
                                modifier = Modifier.staggeredEntrance(index),
                                call = call,
                                statusLabel = viewModel.statusLabel(call.status),
                                scheduledLabel = formatScheduleTime(call.scheduledAtMillis),
                                onClick = { onEditCall(call.id) },
                                onDuplicate = { onDuplicateCall(call.id) },
                                onCancel = { callToCancel = call },
                                onDelete = { callToDelete = call },
                                showCancel = true,
                            )
                        }
                    }
                    if (uiState.pastCalls.isNotEmpty()) {
                        item {
                            SectionLabel(
                                text = stringResource(R.string.section_past),
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                        itemsIndexed(
                            uiState.pastCalls,
                            key = { _, call -> call.id },
                        ) { index, call ->
                            FakeCallCard(
                                modifier = Modifier.staggeredEntrance(
                                    uiState.scheduledCalls.size + index,
                                ),
                                call = call,
                                statusLabel = viewModel.statusLabel(call.status),
                                scheduledLabel = formatScheduleTime(call.scheduledAtMillis),
                                onClick = null,
                                onDuplicate = { onDuplicateCall(call.id) },
                                onCancel = { },
                                onDelete = { callToDelete = call },
                                showCancel = false,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeEmptyState(
    modifier: Modifier = Modifier,
    onScheduleCall: () -> Unit = {},
) {
    val infinite = rememberInfiniteTransition(label = "empty_logo")
    val logoPulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "logo_pulse",
    )
    val enter = remember { Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(700))
    }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .padding(40.dp)
                .graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 40f
                },
        ) {
            Image(
                painter = painterResource(R.drawable.ic_app_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(128.dp)
                    .graphicsLayer {
                        scaleX = logoPulse
                        scaleY = logoPulse
                    }
                    .shadow(20.dp, RoundedCornerShape(28.dp), spotColor = MaterialTheme.colorScheme.primary.copy(0.35f))
                    .clip(RoundedCornerShape(28.dp)),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = stringResource(R.string.home_empty),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.home_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Primary3DButton(
                text = stringResource(R.string.home_schedule_fab),
                onClick = onScheduleCall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun FakeCallCard(
    modifier: Modifier = Modifier,
    call: FakeCall,
    statusLabel: String,
    scheduledLabel: String,
    onClick: (() -> Unit)?,
    onDuplicate: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    showCancel: Boolean,
) {
    val messagePreview = when (call.messageType) {
        MessageType.VOICE -> stringResource(R.string.message_type_voice)
        MessageType.TEXT -> call.message
    }
    val statusColor = when (call.status) {
        CallStatus.COMPLETED -> StatusCompleted
        CallStatus.DECLINED -> StatusDeclined
        CallStatus.MISSED -> StatusDeclined
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val avatarGradient = callAvatarGradient(call.id)
    ElevatedAppCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(avatarGradient)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.callerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = messagePreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = scheduledLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (showCancel && call.status == CallStatus.SCHEDULED) {
                    TextButton3D(
                        text = stringResource(R.string.cancel_call_action),
                        onClick = onCancel,
                        accent = MaterialTheme.colorScheme.error,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton3D(
                        icon = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.duplicate_call_action),
                        onClick = onDuplicate,
                        size = 40.dp,
                    )
                    IconButton3D(
                        icon = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_call_action),
                        onClick = onDelete,
                        tint = MaterialTheme.colorScheme.error,
                        size = 40.dp,
                    )
                }
            }
        }
    }
}

private fun callAvatarGradient(callId: String): List<androidx.compose.ui.graphics.Color> {
    val palettes = listOf(
        listOf(TertiaryAccent, SecondaryAccent),
        listOf(SecondaryAccent, TertiaryAccent),
        listOf(TertiaryAccent, TertiaryAccent.copy(alpha = 0.7f)),
        listOf(SecondaryAccent, SecondaryAccent.copy(alpha = 0.75f)),
    )
    return palettes[callId.hashCode().mod(palettes.size).let { if (it < 0) it + palettes.size else it }]
}

private fun formatScheduleTime(millis: Long): String =
    SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()).format(Date(millis))

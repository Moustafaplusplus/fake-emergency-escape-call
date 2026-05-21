package com.fakeemergencyescape.call.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fakeemergencyescape.call.BuildConfig
import com.fakeemergencyescape.call.R
import com.fakeemergencyescape.call.domain.model.CallStatus
import com.fakeemergencyescape.call.domain.model.FakeCall
import com.fakeemergencyescape.call.ui.components.AppScreenBackground
import com.fakeemergencyescape.call.ui.components.ElevatedAppCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onScheduleCall: () -> Unit,
    onEditCall: (String) -> Unit,
    onSettings: () -> Unit,
    onPreviewIncoming: () -> Unit,
    onPreviewActive: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }
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
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    ),
                    actions = {
                        IconButton(onClick = onSettings) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.menu_settings))
                        }
                        if (BuildConfig.DEBUG) {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu_debug))
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_preview_incoming)) },
                                    onClick = {
                                        menuExpanded = false
                                        onPreviewIncoming()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.menu_preview_active)) },
                                    onClick = {
                                        menuExpanded = false
                                        onPreviewActive()
                                    },
                                )
                            }
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onScheduleCall,
                    shape = CircleShape,
                    modifier = Modifier.shadow(12.dp, CircleShape),
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.home_schedule_fab))
                }
            },
        ) { innerPadding ->
            if (uiState.isEmpty) {
                HomeEmptyState(modifier = Modifier.padding(innerPadding))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.scheduledCalls.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.section_upcoming),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            )
                        }
                        items(uiState.scheduledCalls, key = { it.id }) { call ->
                            FakeCallCard(
                                call = call,
                                statusLabel = viewModel.statusLabel(call.status),
                                scheduledLabel = formatScheduleTime(call.scheduledAtMillis),
                                onClick = { onEditCall(call.id) },
                                onCancel = { callToCancel = call },
                                onDelete = { callToDelete = call },
                                showCancel = true,
                            )
                        }
                    }
                    if (uiState.pastCalls.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.section_past),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                            )
                        }
                        items(uiState.pastCalls, key = { it.id }) { call ->
                            FakeCallCard(
                                call = call,
                                statusLabel = viewModel.statusLabel(call.status),
                                scheduledLabel = formatScheduleTime(call.scheduledAtMillis),
                                onClick = { },
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
private fun HomeEmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(40.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_splash_logo),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .shadow(12.dp, CircleShape),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = stringResource(R.string.home_empty),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.home_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FakeCallCard(
    call: FakeCall,
    statusLabel: String,
    scheduledLabel: String,
    onClick: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    showCancel: Boolean,
) {
    ElevatedAppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = call.callerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = call.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Text(
                        text = scheduledLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showCancel && call.status == CallStatus.SCHEDULED) {
                        TextButton(onClick = onCancel) {
                            Text(stringResource(R.string.cancel_call_action))
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_call_action),
                            modifier = Modifier.size(22.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun formatScheduleTime(millis: Long): String =
    SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()).format(Date(millis))

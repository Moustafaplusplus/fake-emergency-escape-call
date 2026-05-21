package com.fakeemergencyescape.call.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fakeemergencyescape.call.ui.active.ActiveCallScreen
import com.fakeemergencyescape.call.ui.create.CreateFakeCallScreen
import com.fakeemergencyescape.call.ui.create.CreateFakeCallViewModel
import com.fakeemergencyescape.call.ui.home.HomeScreen
import com.fakeemergencyescape.call.ui.home.HomeViewModel
import com.fakeemergencyescape.call.ui.incoming.IncomingCallScreen
import com.fakeemergencyescape.call.ui.incoming.IncomingCallViewModel
import com.fakeemergencyescape.call.ui.incoming.IncomingScreen
import com.fakeemergencyescape.call.ui.incoming.formattedCallDuration
import com.fakeemergencyescape.call.ui.components.AppScreenBackground
import com.fakeemergencyescape.call.ui.components.CallScreenBackground
import com.fakeemergencyescape.call.ui.navigation.AppStartViewModel
import com.fakeemergencyescape.call.ui.onboarding.OnboardingScreen
import com.fakeemergencyescape.call.ui.preview.PreviewCallData
import com.fakeemergencyescape.call.ui.settings.AboutScreen
import com.fakeemergencyescape.call.ui.settings.PrivacyScreen
import com.fakeemergencyescape.call.ui.settings.SettingsScreen
import com.fakeemergencyescape.call.ui.settings.SettingsViewModel
import com.fakeemergencyescape.call.ui.theme.CallScreenTheme

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val appStartViewModel: AppStartViewModel = hiltViewModel()
    val startState by appStartViewModel.uiState.collectAsStateWithLifecycle()

    if (!startState.isReady) {
        AppScreenBackground {
            Box(modifier = Modifier.fillMaxSize())
        }
        return
    }

    val startDestination = if (startState.onboardingCompleted) Routes.HOME else Routes.ONBOARDING

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onScheduleCall = { navController.navigate(Routes.CREATE) },
                onEditCall = { id -> navController.navigate(Routes.edit(id)) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onPreviewIncoming = {
                    navController.navigate(Routes.incoming(PreviewCallData.PREVIEW_ID))
                },
                onPreviewActive = {
                    navController.navigate(Routes.active(PreviewCallData.PREVIEW_ID))
                },
            )
        }

        composable(Routes.CREATE) {
            val viewModel: CreateFakeCallViewModel = hiltViewModel()
            CreateFakeCallScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onScheduled = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(navArgument(Routes.ARG_FAKE_CALL_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val viewModel: CreateFakeCallViewModel = hiltViewModel(backStackEntry)
            CreateFakeCallScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onScheduled = { navController.popBackStack() },
            )
        }

        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onAbout = { navController.navigate(Routes.ABOUT) },
                onPrivacy = { navController.navigate(Routes.PRIVACY) },
            )
        }

        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PRIVACY) {
            PrivacyScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onContinue = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Routes.INCOMING,
            arguments = listOf(navArgument(Routes.ARG_FAKE_CALL_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getString(Routes.ARG_FAKE_CALL_ID).orEmpty()
            val incomingViewModel: IncomingCallViewModel = hiltViewModel(backStackEntry)
            val incomingState by incomingViewModel.uiState.collectAsStateWithLifecycle()
            if (!incomingState.isLoading) {
                when (incomingState.screen) {
                    IncomingScreen.INCOMING -> {
                        IncomingCallScreen(
                            callerName = incomingState.callerName,
                            onAnswer = incomingViewModel::onAnswer,
                            onDecline = {
                                incomingViewModel.onDecline()
                                navController.popBackStack()
                            },
                        )
                    }
                    IncomingScreen.ACTIVE -> {
                        ActiveCallScreen(
                            callerName = incomingState.callerName,
                            callDurationFormatted = incomingState.formattedCallDuration(),
                            speakerOn = incomingState.speakerOn,
                            muted = incomingState.muted,
                            showNoEarpieceHint = incomingState.showNoEarpieceHint,
                            ttsError = incomingState.ttsError,
                            isSpeaking = incomingState.isSpeaking,
                            onToggleSpeaker = incomingViewModel::onToggleSpeaker,
                            onToggleMute = incomingViewModel::onToggleMute,
                            onReplay = incomingViewModel::onReplay,
                            onEndCall = {
                                incomingViewModel.onEndCall()
                                navController.popBackStack(Routes.HOME, inclusive = false)
                            },
                        )
                    }
                }
            }
        }

        composable(
            route = Routes.ACTIVE,
            arguments = listOf(navArgument(Routes.ARG_FAKE_CALL_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val activeViewModel: IncomingCallViewModel = hiltViewModel(backStackEntry)
            val activeState by activeViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(activeState.isLoading) {
                if (!activeState.isLoading) {
                    activeViewModel.showActivePreview()
                }
            }

            when {
                !activeState.isLoading && activeState.screen == IncomingScreen.ACTIVE -> {
                ActiveCallScreen(
                    callerName = activeState.callerName,
                    callDurationFormatted = activeState.formattedCallDuration(),
                    speakerOn = activeState.speakerOn,
                    muted = activeState.muted,
                    showNoEarpieceHint = activeState.showNoEarpieceHint,
                    ttsError = activeState.ttsError,
                    isSpeaking = activeState.isSpeaking,
                    onToggleSpeaker = activeViewModel::onToggleSpeaker,
                    onToggleMute = activeViewModel::onToggleMute,
                    onReplay = activeViewModel::onReplay,
                    onEndCall = {
                        activeViewModel.onEndCall()
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    },
                )
                }
                else -> {
                    CallScreenTheme {
                        CallScreenBackground {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}

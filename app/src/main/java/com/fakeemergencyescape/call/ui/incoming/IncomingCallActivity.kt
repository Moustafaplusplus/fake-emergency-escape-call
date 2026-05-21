package com.fakeemergencyescape.call.ui.incoming

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fakeemergencyescape.call.navigation.Routes
import com.fakeemergencyescape.call.ui.active.ActiveCallScreen
import com.fakeemergencyescape.call.ui.theme.FakeEmergencyEscapeCallTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {

    companion object {
        const val EXTRA_AUTO_ANSWER = "autoAnswer"
        const val EXTRA_DISMISS_TO_HOME = "dismissToHome"

        fun createLaunchIntent(
            context: android.content.Context,
            callId: String,
            autoAnswer: Boolean = false,
            dismissToHome: Boolean = true,
        ): Intent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Routes.ARG_FAKE_CALL_ID, callId)
            putExtra(EXTRA_AUTO_ANSWER, autoAnswer)
            putExtra(EXTRA_DISMISS_TO_HOME, dismissToHome)
        }
    }

    private val viewModel: IncomingCallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent.getStringExtra(Routes.ARG_FAKE_CALL_ID).isNullOrBlank()) {
            finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
        )

        // Back press = Decline (same as tapping Decline).
        onBackPressedDispatcher.addCallback(this) {
            if (viewModel.uiState.value.screen == IncomingScreen.ACTIVE) {
                viewModel.onEndCall()
            } else {
                viewModel.onDecline()
            }
        }

        val autoAnswer = intent.getBooleanExtra(EXTRA_AUTO_ANSWER, false)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            var didAutoAnswer by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                viewModel.finish.collect { dismissCallUi() }
            }

            LaunchedEffect(uiState.isLoading, uiState.screen, autoAnswer) {
                if (
                    autoAnswer &&
                    !didAutoAnswer &&
                    !uiState.isLoading &&
                    uiState.screen == IncomingScreen.INCOMING
                ) {
                    didAutoAnswer = true
                    viewModel.onAnswer()
                }
            }

            FakeEmergencyEscapeCallTheme {
                if (!uiState.isLoading) {
                    when (uiState.screen) {
                        IncomingScreen.INCOMING -> {
                            val appearance by viewModel.callAppearance.collectAsStateWithLifecycle()
                            IncomingCallScreen(
                                callerName = uiState.callerName,
                                onAnswer = viewModel::onAnswer,
                                onDecline = viewModel::onDecline,
                                appearance = appearance,
                            )
                        }
                        IncomingScreen.ACTIVE -> {
                            val activeAppearance by viewModel.activeCallAppearance.collectAsStateWithLifecycle()
                            ActiveCallScreen(
                                callerName = uiState.callerName,
                                callDurationFormatted = uiState.formattedCallDuration(),
                                speakerOn = uiState.speakerOn,
                                muted = uiState.muted,
                                showNoEarpieceHint = uiState.showNoEarpieceHint,
                                ttsError = uiState.ttsError,
                                isSpeaking = uiState.isSpeaking,
                                onToggleSpeaker = viewModel::onToggleSpeaker,
                                onToggleMute = viewModel::onToggleMute,
                                onReplay = viewModel::onReplay,
                                onEndCall = viewModel::onEndCall,
                                appearance = activeAppearance,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun dismissCallUi() {
        finish()
        if (intent.getBooleanExtra(EXTRA_DISMISS_TO_HOME, true)) {
            startActivity(
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
            )
        }
    }
}

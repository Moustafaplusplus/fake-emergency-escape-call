package com.fakeemergencyescape.call.ui.incoming

import android.content.Intent
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
    }

    private val viewModel: IncomingCallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent.getStringExtra(Routes.ARG_FAKE_CALL_ID).isNullOrBlank()) {
            finish()
            return
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
                viewModel.finish.collect { finish() }
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
                            IncomingCallScreen(
                                callerName = uiState.callerName,
                                onAnswer = viewModel::onAnswer,
                                onDecline = viewModel::onDecline,
                            )
                        }
                        IncomingScreen.ACTIVE -> {
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
}

package com.fakeemergencyescape.call

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fakeemergencyescape.call.navigation.AppNavigation
import com.fakeemergencyescape.call.ui.navigation.AppStartViewModel
import com.fakeemergencyescape.call.ui.theme.FakeEmergencyEscapeCallTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appStartViewModel: AppStartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition {
            !appStartViewModel.uiState.value.isReady
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FakeEmergencyEscapeCallTheme {
                AppNavigation()
            }
        }
    }
}

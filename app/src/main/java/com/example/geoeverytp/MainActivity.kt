package com.example.geoeverytp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geoeverytp.data.GeoLogEntity
import com.example.geoeverytp.ui.LogDetailScreen
import com.example.geoeverytp.ui.LogScreen
import com.example.geoeverytp.ui.MainScreen
import com.example.geoeverytp.ui.SettingsScreen
import com.example.geoeverytp.ui.theme.GeoEveryTPTheme
import com.example.geoeverytp.viewmodel.MainViewModel

/** Navigation destinations for the single-activity app. */
sealed class NavScreen {
    data object Home : NavScreen()
    data object Log : NavScreen()
    data class LogDetail(val entity: GeoLogEntity) : NavScreen()
    data object Settings : NavScreen()
}

/**
 * Main activity: Compose UI with Home, Log, LogDetail, Settings.
 * Holds permission launcher and forwards it to [MainScreen] for location refresh.
 */
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        if (map[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModelRef?.refreshLocation {}
        }
    }

    private var viewModelRef: MainViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            LaunchedEffect(Unit) { viewModelRef = vm }
            var currentScreen by remember { mutableStateOf<NavScreen>(NavScreen.Home) }

            GeoEveryTPTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (val screen = currentScreen) {
                        is NavScreen.Home -> MainScreen(
                            viewModel = vm,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= 23 &&
                                    ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            onNavigateToLog = { currentScreen = NavScreen.Log },
                            onNavigateToSettings = { currentScreen = NavScreen.Settings },
                            padding = innerPadding
                        )
                        is NavScreen.Log -> LogScreen(
                            viewModel = vm,
                            padding = innerPadding,
                            onLogClick = { currentScreen = NavScreen.LogDetail(it) },
                            onBack = { currentScreen = NavScreen.Home }
                        )
                        is NavScreen.LogDetail -> LogDetailScreen(
                            entity = screen.entity,
                            viewModel = vm,
                            onBack = { currentScreen = NavScreen.Log },
                            padding = innerPadding
                        )
                        is NavScreen.Settings -> SettingsScreen(
                            viewModel = vm,
                            padding = innerPadding,
                            onBack = { currentScreen = NavScreen.Home },
                            onLanguageChanged = {
                                (application as? GeoApp)?.applySavedLanguage()
                                recreate()
                            }
                        )
                    }
                }
            }
        }
    }
}

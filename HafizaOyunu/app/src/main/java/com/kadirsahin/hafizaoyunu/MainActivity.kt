package com.kadirsahin.hafizaoyunu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kadirsahin.hafizaoyunu.ui.AppViewModel
import com.kadirsahin.hafizaoyunu.ui.MemoryGameApp
import com.kadirsahin.hafizaoyunu.ui.theme.MemoryGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val memoryApplication = application as MemoryApplication
        val appViewModel = ViewModelProvider(
            this,
            AppViewModel.factory(
                memoryApplication.scoreRepository,
                memoryApplication.settingsRepository,
                memoryApplication.profileRepository,
            ),
        )[AppViewModel::class.java]
        // Keep the splash until settings are read, so the first frame already has the right theme.
        splash.setKeepOnScreenCondition { appViewModel.settings.value == null }
        // Warm up the synthesiser early; rendering the effects takes a few milliseconds on a background thread.
        memoryApplication.soundEngine

        setContent {
            val settings = appViewModel.settings.collectAsStateWithLifecycle().value ?: return@setContent
            MemoryGameTheme(settings) {
                MemoryGameApp(memoryApplication, appViewModel, settings)
            }
        }
    }
}

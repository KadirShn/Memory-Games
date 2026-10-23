package com.kadirsahin.hafizaoyunu.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kadirsahin.hafizaoyunu.MemoryApplication
import com.kadirsahin.hafizaoyunu.data.AppSettings
import com.kadirsahin.hafizaoyunu.domain.Campaign
import com.kadirsahin.hafizaoyunu.domain.GameConfig
import com.kadirsahin.hafizaoyunu.ui.components.Feedback
import com.kadirsahin.hafizaoyunu.ui.components.LocalFeedback
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.screens.CampaignScreen
import com.kadirsahin.hafizaoyunu.ui.screens.GameScreen
import com.kadirsahin.hafizaoyunu.ui.screens.HomeScreen
import com.kadirsahin.hafizaoyunu.ui.screens.ModesScreen
import com.kadirsahin.hafizaoyunu.ui.screens.OnboardingScreen
import com.kadirsahin.hafizaoyunu.ui.screens.ProfileScreen
import com.kadirsahin.hafizaoyunu.ui.screens.ScoresScreen
import com.kadirsahin.hafizaoyunu.ui.screens.SettingsScreen
import com.kadirsahin.hafizaoyunu.ui.screens.ShopScreen

private const val HOME = "home"
private const val MODES = "modes"
private const val CAMPAIGN = "campaign"
private const val SCORES = "scores"
private const val PROFILE = "profile"
private const val SHOP = "shop"
private const val SETTINGS = "settings"
private const val ONBOARDING = "onboarding"
private const val GAME_PREFIX = "game:"
private const val SEPARATOR = "\n"

@Composable
fun MemoryGameApp(application: MemoryApplication, appViewModel: AppViewModel, settings: AppSettings) {
    val scores by appViewModel.scores.collectAsStateWithLifecycle()
    val profile by appViewModel.profile.collectAsStateWithLifecycle()

    // The back stack is a newline separated list of routes so it survives process death via rememberSaveable.
    var stackValue by rememberSaveable { mutableStateOf(HOME) }
    var sessionId by rememberSaveable { mutableIntStateOf(0) }
    var forward by remember { mutableStateOf(true) }
    val stack = stackValue.split(SEPARATOR)
    val current = if (!settings.onboardingDone) ONBOARDING else stack.last()

    fun push(route: String) {
        forward = true
        stackValue = (stack + route).joinToString(SEPARATOR)
    }

    fun pop() {
        forward = false
        if (stack.size > 1) stackValue = stack.dropLast(1).joinToString(SEPARATOR)
    }

    fun replaceTop(route: String) {
        forward = true
        stackValue = (stack.dropLast(1) + route).joinToString(SEPARATOR)
    }

    fun gameRoute(config: GameConfig): String {
        sessionId += 1
        return "$GAME_PREFIX${config.encode()}:$sessionId"
    }

    val haptics = LocalHapticFeedback.current
    val feedback = remember(haptics) { Feedback(application.soundEngine, haptics) }
    feedback.soundEnabled = settings.soundEnabled
    feedback.hapticsEnabled = settings.hapticsEnabled

    BackHandler(enabled = stack.size > 1 && !current.startsWith(GAME_PREFIX) && current != ONBOARDING) { pop() }

    CompositionLocalProvider(
        LocalFeedback provides feedback,
        LocalReducedMotion provides settings.reducedMotion,
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ) {
            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    val duration = if (settings.reducedMotion) 120 else 320
                    if (targetState.startsWith(GAME_PREFIX)) {
                        (fadeIn(tween(duration)) + scaleIn(tween(duration), initialScale = 0.92f)) togetherWith fadeOut(tween(duration / 2))
                    } else if (forward) {
                        (slideInHorizontally(tween(duration)) { it / 3 } + fadeIn(tween(duration))) togetherWith
                            (slideOutHorizontally(tween(duration)) { -it / 5 } + fadeOut(tween(duration / 2)))
                    } else {
                        (slideInHorizontally(tween(duration)) { -it / 3 } + fadeIn(tween(duration))) togetherWith
                            (slideOutHorizontally(tween(duration)) { it / 5 } + fadeOut(tween(duration / 2)))
                    }
                },
                label = "screens",
            ) { destination ->
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    when {
                        destination == ONBOARDING -> OnboardingScreen(
                            cardTheme = settings.cardTheme,
                            onDone = {
                                if (!settings.onboardingDone) appViewModel.setOnboardingDone(true) else pop()
                            },
                        )
                        destination == HOME -> HomeScreen(
                            profile = profile,
                            cardTheme = settings.cardTheme,
                            onPlay = { push(MODES) },
                            onDaily = { push(gameRoute(GameConfig.daily())) },
                            onCampaign = { push(CAMPAIGN) },
                            onScores = { push(SCORES) },
                            onProfile = { push(PROFILE) },
                            onShop = { push(SHOP) },
                            onSettings = { push(SETTINGS) },
                        )
                        destination == MODES -> ModesScreen(
                            profile = profile,
                            scores = scores,
                            onBack = ::pop,
                            onCampaign = { push(CAMPAIGN) },
                            onStart = { push(gameRoute(it)) },
                        )
                        destination == CAMPAIGN -> CampaignScreen(
                            profile = profile,
                            onBack = ::pop,
                            onStart = { push(gameRoute(it)) },
                        )
                        destination == SCORES -> ScoresScreen(scores = scores, onBack = ::pop, onClearScores = appViewModel::clearScores)
                        destination == PROFILE -> ProfileScreen(profile = profile, onBack = ::pop)
                        destination == SHOP -> ShopScreen(
                            profile = profile,
                            cardTheme = settings.cardTheme,
                            onBack = ::pop,
                            onBuyBooster = appViewModel::buyBooster,
                            onBuyBack = appViewModel::buyCardBack,
                            onSelectBack = appViewModel::selectCardBack,
                            onSelectTheme = appViewModel::setCardTheme,
                        )
                        destination == SETTINGS -> SettingsScreen(
                            settings = settings,
                            onBack = ::pop,
                            onThemeMode = appViewModel::setThemeMode,
                            onSound = appViewModel::setSoundEnabled,
                            onHaptics = appViewModel::setHapticsEnabled,
                            onDynamicColors = appViewModel::setDynamicColorsEnabled,
                            onReducedMotion = appViewModel::setReducedMotion,
                            onHowToPlay = { push(ONBOARDING) },
                            onResetProgress = appViewModel::resetProgress,
                        )
                        destination.startsWith(GAME_PREFIX) -> {
                            val config = GameConfig.decode(destination.removePrefix(GAME_PREFIX).substringBeforeLast(':'))
                            if (config == null) {
                                LaunchedEffect(destination) { pop() }
                            } else {
                                val gameViewModel: GameViewModel = viewModel(
                                    key = destination,
                                    factory = GameViewModel.factory(config, application.scoreRepository, application.profileRepository),
                                )
                                val state by gameViewModel.uiState.collectAsStateWithLifecycle()
                                GameScreen(
                                    state = state,
                                    viewModel = gameViewModel,
                                    cardTheme = settings.cardTheme,
                                    cardBack = profile.selectedBack,
                                    onExit = {
                                        gameViewModel.shutdown()
                                        pop()
                                    },
                                    onNextLevel = { level ->
                                        gameViewModel.shutdown()
                                        Campaign.level(level)?.let { replaceTop(gameRoute(it.config())) }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

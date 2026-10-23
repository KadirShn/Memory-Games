package com.kadirsahin.hafizaoyunu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kadirsahin.hafizaoyunu.data.AppSettings
import com.kadirsahin.hafizaoyunu.data.ProfileRepository
import com.kadirsahin.hafizaoyunu.data.ScoreEntry
import com.kadirsahin.hafizaoyunu.data.ScoreRepository
import com.kadirsahin.hafizaoyunu.data.SettingsRepository
import com.kadirsahin.hafizaoyunu.data.ThemeMode
import com.kadirsahin.hafizaoyunu.domain.Booster
import com.kadirsahin.hafizaoyunu.domain.CardBack
import com.kadirsahin.hafizaoyunu.domain.CardTheme
import com.kadirsahin.hafizaoyunu.domain.Profile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val scoreRepository: ScoreRepository,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    val scores: StateFlow<List<ScoreEntry>> = scoreRepository.scores

    /** Null until DataStore delivered the stored settings, so the first frame never flashes defaults. */
    val settings: StateFlow<AppSettings?> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null,
    )

    val profile: StateFlow<Profile> = profileRepository.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = Profile(),
    )

    fun setThemeMode(value: ThemeMode) = launch { settingsRepository.setThemeMode(value) }
    fun setHapticsEnabled(value: Boolean) = launch { settingsRepository.setHapticsEnabled(value) }
    fun setSoundEnabled(value: Boolean) = launch { settingsRepository.setSoundEnabled(value) }
    fun setDynamicColorsEnabled(value: Boolean) = launch { settingsRepository.setDynamicColorsEnabled(value) }
    fun setReducedMotion(value: Boolean) = launch { settingsRepository.setReducedMotion(value) }
    fun setCardTheme(value: CardTheme) = launch { settingsRepository.setCardTheme(value) }
    fun setOnboardingDone(value: Boolean) = launch { settingsRepository.setOnboardingDone(value) }
    fun clearScores() = launch { scoreRepository.clearAll() }
    fun resetProgress() = launch { profileRepository.reset() }
    fun selectCardBack(back: CardBack) = launch { profileRepository.selectCardBack(back) }

    fun buyBooster(booster: Booster, onResult: (Boolean) -> Unit) = launch {
        onResult(profileRepository.buyBooster(booster))
    }

    fun buyCardBack(back: CardBack, onResult: (Boolean) -> Unit) = launch {
        onResult(profileRepository.buyCardBack(back))
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    companion object {
        fun factory(
            scoreRepository: ScoreRepository,
            settingsRepository: SettingsRepository,
            profileRepository: ProfileRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AppViewModel(scoreRepository, settingsRepository, profileRepository) as T
        }
    }
}

package com.kadirsahin.hafizaoyunu.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kadirsahin.hafizaoyunu.domain.CardTheme
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val dynamicColorsEnabled: Boolean = false,
    val reducedMotion: Boolean = false,
    val cardTheme: CardTheme = CardTheme.CLASSIC,
    val onboardingDone: Boolean = false,
)

private val Context.settingsDataStore by preferencesDataStore(name = "memory_settings")

class SettingsRepository(private val context: Context) {
    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            AppSettings(
                themeMode = preferences[THEME_MODE]
                    ?.let { stored -> ThemeMode.entries.firstOrNull { it.name == stored } }
                    ?: ThemeMode.SYSTEM,
                hapticsEnabled = preferences[HAPTICS] ?: true,
                soundEnabled = preferences[SOUND] ?: true,
                dynamicColorsEnabled = preferences[DYNAMIC_COLORS] ?: false,
                reducedMotion = preferences[REDUCED_MOTION] ?: false,
                cardTheme = preferences[CARD_THEME]
                    ?.let { stored -> CardTheme.entries.firstOrNull { it.name == stored } }
                    ?: CardTheme.CLASSIC,
                onboardingDone = preferences[ONBOARDING_DONE] ?: false,
            )
        }

    suspend fun setThemeMode(value: ThemeMode) = set(THEME_MODE, value.name)
    suspend fun setHapticsEnabled(value: Boolean) = set(HAPTICS, value)
    suspend fun setSoundEnabled(value: Boolean) = set(SOUND, value)
    suspend fun setDynamicColorsEnabled(value: Boolean) = set(DYNAMIC_COLORS, value)
    suspend fun setReducedMotion(value: Boolean) = set(REDUCED_MOTION, value)
    suspend fun setCardTheme(value: CardTheme) = set(CARD_THEME, value.name)
    suspend fun setOnboardingDone(value: Boolean) = set(ONBOARDING_DONE, value)

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.settingsDataStore.edit { it[key] = value }
    }
}

private val THEME_MODE = stringPreferencesKey("theme_mode")
private val HAPTICS = booleanPreferencesKey("haptics_enabled")
private val SOUND = booleanPreferencesKey("sound_enabled")
private val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors_enabled")
private val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
private val CARD_THEME = stringPreferencesKey("card_theme")
private val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

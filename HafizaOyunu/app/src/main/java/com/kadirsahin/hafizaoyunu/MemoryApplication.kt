package com.kadirsahin.hafizaoyunu

import android.app.Application
import com.kadirsahin.hafizaoyunu.audio.SoundEngine
import com.kadirsahin.hafizaoyunu.data.ProfileRepository
import com.kadirsahin.hafizaoyunu.data.ScoreRepository
import com.kadirsahin.hafizaoyunu.data.SettingsRepository

class MemoryApplication : Application() {
    val scoreRepository by lazy { ScoreRepository(this) }
    val settingsRepository by lazy { SettingsRepository(this) }
    val profileRepository by lazy { ProfileRepository(this) }
    val soundEngine by lazy { SoundEngine(this) }
}

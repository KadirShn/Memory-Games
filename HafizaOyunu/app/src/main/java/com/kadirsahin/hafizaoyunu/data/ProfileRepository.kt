package com.kadirsahin.hafizaoyunu.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kadirsahin.hafizaoyunu.domain.Achievement
import com.kadirsahin.hafizaoyunu.domain.Booster
import com.kadirsahin.hafizaoyunu.domain.CardBack
import com.kadirsahin.hafizaoyunu.domain.GameResult
import com.kadirsahin.hafizaoyunu.domain.Profile
import com.kadirsahin.hafizaoyunu.domain.Progression
import com.kadirsahin.hafizaoyunu.domain.RewardSummary
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "memory_profile")

/** Local only player progression: coins, XP, campaign stars, achievements and inventory. */
class ProfileRepository(private val context: Context) {
    val profile: Flow<Profile> = context.profileDataStore.data
        .catch { error -> if (error is IOException) emit(emptyPreferences()) else throw error }
        .map { it.toProfile() }

    /** Applies a finished game atomically and returns what the player earned. */
    suspend fun applyResult(result: GameResult): RewardSummary {
        lateinit var summary: RewardSummary
        update { profile ->
            val (next, reward) = Progression.apply(profile, result)
            summary = reward
            next
        }
        return summary
    }

    /** Uses one booster if available; returns false when the inventory is empty. */
    suspend fun consumeBooster(booster: Booster): Boolean {
        var consumed = false
        update { profile ->
            val count = profile.boosterCount(booster)
            if (count <= 0) return@update profile
            consumed = true
            profile.copy(boosters = profile.boosters + (booster to count - 1))
        }
        return consumed
    }

    suspend fun buyBooster(booster: Booster): Boolean = purchase(booster.price) { profile ->
        profile.copy(boosters = profile.boosters + (booster to profile.boosterCount(booster) + 1))
    }

    suspend fun buyCardBack(back: CardBack): Boolean = purchase(back.price) { profile ->
        if (back in profile.ownedBacks) null
        else profile.copy(ownedBacks = profile.ownedBacks + back, selectedBack = back)
    }

    suspend fun selectCardBack(back: CardBack) = update { profile ->
        if (back in profile.ownedBacks) profile.copy(selectedBack = back) else profile
    }

    suspend fun reset() {
        context.profileDataStore.edit { it.clear() }
    }

    private suspend fun purchase(price: Int, transform: (Profile) -> Profile?): Boolean {
        var bought = false
        update { profile ->
            if (profile.coins < price) return@update profile
            val next = transform(profile) ?: return@update profile
            bought = true
            next.copy(coins = next.coins - price)
        }
        return bought
    }

    private suspend fun update(transform: (Profile) -> Profile) {
        context.profileDataStore.edit { preferences ->
            val current = preferences.toProfile()
            val next = transform(current)
            if (next != current) next.writeTo(preferences)
        }
    }
}

private fun Preferences.toProfile(): Profile {
    val defaults = Profile()
    return Profile(
        coins = this[COINS] ?: defaults.coins,
        xp = this[XP] ?: 0,
        coinsEarned = this[COINS_EARNED] ?: 0,
        gamesPlayed = this[GAMES_PLAYED] ?: 0,
        gamesWon = this[GAMES_WON] ?: 0,
        totalMatches = this[TOTAL_MATCHES] ?: 0,
        totalMoves = this[TOTAL_MOVES] ?: 0,
        totalSlips = this[TOTAL_SLIPS] ?: 0,
        bestCombo = this[BEST_COMBO] ?: 0,
        playSeconds = this[PLAY_SECONDS] ?: 0L,
        campaignStars = this[CAMPAIGN_STARS].orEmpty()
            .split(';')
            .mapNotNull { entry ->
                val (level, stars) = entry.split('=').takeIf { it.size == 2 } ?: return@mapNotNull null
                val levelNumber = level.toIntOrNull() ?: return@mapNotNull null
                val starCount = stars.toIntOrNull() ?: return@mapNotNull null
                levelNumber to starCount
            }
            .toMap(),
        achievements = this[ACHIEVEMENTS].orEmpty()
            .mapNotNull { name -> Achievement.entries.firstOrNull { it.name == name } }
            .toSet(),
        boosters = Booster.entries.associateWith { booster ->
            this[intPreferencesKey("booster_${booster.name}")] ?: Profile.STARTING_BOOSTERS
        },
        ownedBacks = (this[OWNED_BACKS].orEmpty()
            .mapNotNull { name -> CardBack.entries.firstOrNull { it.name == name } } + CardBack.AURORA)
            .toSet(),
        selectedBack = this[SELECTED_BACK]
            ?.let { name -> CardBack.entries.firstOrNull { it.name == name } }
            ?: CardBack.AURORA,
        dailyLastDay = this[DAILY_LAST_DAY],
        dailyStreak = this[DAILY_STREAK] ?: 0,
        dailyBestStreak = this[DAILY_BEST_STREAK] ?: 0,
        dailyCompleted = this[DAILY_COMPLETED] ?: 0,
        survivalBest = this[SURVIVAL_BEST] ?: 0,
        zenGames = this[ZEN_GAMES] ?: 0,
        duelGames = this[DUEL_GAMES] ?: 0,
        iceCracked = this[ICE_CRACKED] ?: 0,
        goldenMatched = this[GOLDEN_MATCHED] ?: 0,
    )
}

private fun Profile.writeTo(preferences: MutablePreferences) {
    preferences[COINS] = coins
    preferences[XP] = xp
    preferences[COINS_EARNED] = coinsEarned
    preferences[GAMES_PLAYED] = gamesPlayed
    preferences[GAMES_WON] = gamesWon
    preferences[TOTAL_MATCHES] = totalMatches
    preferences[TOTAL_MOVES] = totalMoves
    preferences[TOTAL_SLIPS] = totalSlips
    preferences[BEST_COMBO] = bestCombo
    preferences[PLAY_SECONDS] = playSeconds
    preferences[CAMPAIGN_STARS] = campaignStars.entries.joinToString(";") { "${it.key}=${it.value}" }
    preferences[ACHIEVEMENTS] = achievements.map { it.name }.toSet()
    boosters.forEach { (booster, count) -> preferences[intPreferencesKey("booster_${booster.name}")] = count }
    preferences[OWNED_BACKS] = ownedBacks.map { it.name }.toSet()
    preferences[SELECTED_BACK] = selectedBack.name
    if (dailyLastDay != null) preferences[DAILY_LAST_DAY] = dailyLastDay else preferences.remove(DAILY_LAST_DAY)
    preferences[DAILY_STREAK] = dailyStreak
    preferences[DAILY_BEST_STREAK] = dailyBestStreak
    preferences[DAILY_COMPLETED] = dailyCompleted
    preferences[SURVIVAL_BEST] = survivalBest
    preferences[ZEN_GAMES] = zenGames
    preferences[DUEL_GAMES] = duelGames
    preferences[ICE_CRACKED] = iceCracked
    preferences[GOLDEN_MATCHED] = goldenMatched
}

private val COINS = intPreferencesKey("coins")
private val XP = intPreferencesKey("xp")
private val COINS_EARNED = intPreferencesKey("coins_earned")
private val GAMES_PLAYED = intPreferencesKey("games_played")
private val GAMES_WON = intPreferencesKey("games_won")
private val TOTAL_MATCHES = intPreferencesKey("total_matches")
private val TOTAL_MOVES = intPreferencesKey("total_moves")
private val TOTAL_SLIPS = intPreferencesKey("total_slips")
private val BEST_COMBO = intPreferencesKey("best_combo")
private val PLAY_SECONDS = longPreferencesKey("play_seconds")
private val CAMPAIGN_STARS = stringPreferencesKey("campaign_stars")
private val ACHIEVEMENTS = stringSetPreferencesKey("achievements")
private val OWNED_BACKS = stringSetPreferencesKey("owned_backs")
private val SELECTED_BACK = stringPreferencesKey("selected_back")
private val DAILY_LAST_DAY = stringPreferencesKey("daily_last_day")
private val DAILY_STREAK = intPreferencesKey("daily_streak")
private val DAILY_BEST_STREAK = intPreferencesKey("daily_best_streak")
private val DAILY_COMPLETED = intPreferencesKey("daily_completed")
private val SURVIVAL_BEST = intPreferencesKey("survival_best")
private val ZEN_GAMES = intPreferencesKey("zen_games")
private val DUEL_GAMES = intPreferencesKey("duel_games")
private val ICE_CRACKED = intPreferencesKey("ice_cracked")
private val GOLDEN_MATCHED = intPreferencesKey("golden_matched")

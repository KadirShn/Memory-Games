package com.kadirsahin.hafizaoyunu.domain

import kotlin.math.max

enum class Booster(val price: Int) {
    /** Reveals the whole board for a moment. */
    PEEK(150),

    /** Highlights a matching pair (or the partner of the open card). */
    HINT(100),

    /** Stops the clock for ten seconds. */
    FREEZE(120),
}

/** Purchasable card back designs; all of them are drawn procedurally. */
enum class CardBack(val price: Int) {
    AURORA(0),
    NEON_GRID(250),
    WAVES(300),
    CONSTELLATION(400),
    HEX(450),
    SUNBURST(550),
    ROYAL(800),
}

enum class Achievement(val reward: Int, val goal: Int = 1) {
    FIRST_WIN(50),
    PERFECT(150),
    COMBO_5(100, 5),
    COMBO_8(250, 8),
    SPEEDSTER(150),
    MASTER(200),
    PURIST(300),
    CAMPAIGN_10(150, 10),
    CAMPAIGN_ALL(500, 30),
    STARS_90(1000, 90),
    SURVIVOR_5(150, 5),
    SURVIVOR_10(400, 10),
    DAILY_FIRST(50),
    STREAK_7(300, 7),
    DUELIST(50),
    ZEN_10(150, 10),
    GAMES_50(200, 50),
    GAMES_200(500, 200),
    MATCHES_1000(400, 1000),
    LEVEL_10(300, 10),
    ICE_50(200, 50),
    GOLDEN_25(200, 25),
    NIGHT_OWL(100),
}

data class Profile(
    val coins: Int = STARTING_COINS,
    val xp: Int = 0,
    val coinsEarned: Int = 0,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalMatches: Int = 0,
    val totalMoves: Int = 0,
    val totalSlips: Int = 0,
    val bestCombo: Int = 0,
    val playSeconds: Long = 0,
    val campaignStars: Map<Int, Int> = emptyMap(),
    val achievements: Set<Achievement> = emptySet(),
    val boosters: Map<Booster, Int> = Booster.entries.associateWith { STARTING_BOOSTERS },
    val ownedBacks: Set<CardBack> = setOf(CardBack.AURORA),
    val selectedBack: CardBack = CardBack.AURORA,
    val dailyLastDay: String? = null,
    val dailyStreak: Int = 0,
    val dailyBestStreak: Int = 0,
    val dailyCompleted: Int = 0,
    val survivalBest: Int = 0,
    val zenGames: Int = 0,
    val duelGames: Int = 0,
    val iceCracked: Int = 0,
    val goldenMatched: Int = 0,
) {
    val levelInfo: LevelInfo get() = Leveling.levelFor(xp)
    val totalStars: Int get() = campaignStars.values.sum()
    val levelsCleared: Int get() = campaignStars.count { it.value > 0 }
    val accuracy: Float get() = if (totalMoves == 0) 0f else totalMatches.toFloat() / totalMoves
    val winRate: Float get() = if (gamesPlayed == 0) 0f else gamesWon.toFloat() / gamesPlayed

    fun activeDailyStreak(today: String, yesterday: String): Int =
        if (dailyLastDay == today || dailyLastDay == yesterday) dailyStreak else 0

    fun boosterCount(booster: Booster): Int = boosters[booster] ?: 0

    companion object {
        const val STARTING_COINS = 200
        const val STARTING_BOOSTERS = 3
    }
}

data class LevelInfo(val level: Int, val xpIntoLevel: Int, val xpForNext: Int) {
    val progress: Float get() = xpIntoLevel.toFloat() / xpForNext
}

object Leveling {
    fun xpForNext(level: Int): Int = 150 + (level - 1) * 75

    fun levelFor(totalXp: Int): LevelInfo {
        var level = 1
        var remaining = totalXp
        while (remaining >= xpForNext(level)) {
            remaining -= xpForNext(level)
            level += 1
        }
        return LevelInfo(level, remaining, xpForNext(level))
    }
}

/** Everything that happened in one finished game, as far as progression cares. */
data class GameResult(
    val config: GameConfig,
    val won: Boolean,
    val score: Int,
    val moves: Int,
    val matches: Int,
    val pairCount: Int,
    val slips: Int,
    val bestCombo: Int,
    val elapsedSeconds: Int,
    val stars: Int,
    val roundsCleared: Int,
    val boostersUsed: Int,
    val iceCracked: Int,
    val goldenMatched: Int,
    val hourOfDay: Int,
    val today: String,
    val yesterday: String,
)

data class RewardSummary(
    val coins: Int,
    val xp: Int,
    val levelBefore: Int,
    val levelAfter: Int,
    val achievements: List<Achievement>,
    val previousStars: Int,
    val dailyFirstClear: Boolean,
    val dailyStreak: Int,
    val newSurvivalBest: Boolean,
) {
    val leveledUp: Boolean get() = levelAfter > levelBefore
    val totalCoins: Int get() = coins + achievements.sumOf { it.reward }
}

object Progression {
    fun apply(profile: Profile, result: GameResult): Pair<Profile, RewardSummary> {
        val config = result.config
        val pairs = result.pairCount
        val previousStars = config.level?.let { profile.campaignStars[it] } ?: 0
        val newStars = max(0, result.stars - previousStars)
        val dailyFirst = config.type == GameType.DAILY && result.won && profile.dailyLastDay != result.today

        val coins = when (config.type) {
            GameType.CLASSIC -> if (result.won) 20 + pairs * 2 else 5
            GameType.CAMPAIGN -> if (result.won) 15 + pairs * 2 + newStars * 15 else 5
            GameType.DAILY -> if (result.won) 30 + pairs * 2 + (if (dailyFirst) 120 else 0) else 5
            GameType.SURVIVAL -> 5 + result.roundsCleared * 15
            GameType.ZEN -> if (result.won) 8 + pairs else 0
            GameType.DUEL -> 10
        }
        val xp = when (config.type) {
            GameType.DUEL -> 25
            GameType.ZEN -> if (result.won) 15 + pairs * 2 else 0
            GameType.SURVIVAL -> 15 + result.score / 25
            else -> if (result.won) 20 + result.score / 15 else 10 + result.score / 40
        }

        var streak = profile.dailyStreak
        var lastDay = profile.dailyLastDay
        if (dailyFirst) {
            streak = if (profile.dailyLastDay == result.yesterday) profile.dailyStreak + 1 else 1
            lastDay = result.today
        }

        val levelBefore = profile.levelInfo.level
        var updated = profile.copy(
            coins = profile.coins + coins,
            coinsEarned = profile.coinsEarned + coins,
            xp = profile.xp + xp,
            gamesPlayed = profile.gamesPlayed + 1,
            gamesWon = profile.gamesWon + if (result.won && config.type != GameType.DUEL) 1 else 0,
            totalMatches = profile.totalMatches + result.matches,
            totalMoves = profile.totalMoves + result.moves,
            totalSlips = profile.totalSlips + result.slips,
            bestCombo = max(profile.bestCombo, result.bestCombo),
            playSeconds = profile.playSeconds + result.elapsedSeconds,
            campaignStars = config.level?.let { level ->
                profile.campaignStars + (level to max(previousStars, result.stars))
            } ?: profile.campaignStars,
            dailyLastDay = lastDay,
            dailyStreak = streak,
            dailyBestStreak = max(profile.dailyBestStreak, streak),
            dailyCompleted = profile.dailyCompleted + if (dailyFirst) 1 else 0,
            survivalBest = if (config.type == GameType.SURVIVAL) max(profile.survivalBest, result.roundsCleared)
            else profile.survivalBest,
            zenGames = profile.zenGames + if (config.type == GameType.ZEN && result.won) 1 else 0,
            duelGames = profile.duelGames + if (config.type == GameType.DUEL) 1 else 0,
            iceCracked = profile.iceCracked + result.iceCracked,
            goldenMatched = profile.goldenMatched + result.goldenMatched,
        )

        val unlocked = Achievement.entries.filter { it !in updated.achievements && it.isEarned(updated, result) }
        val bonus = unlocked.sumOf { it.reward }
        updated = updated.copy(
            achievements = updated.achievements + unlocked,
            coins = updated.coins + bonus,
            coinsEarned = updated.coinsEarned + bonus,
        )

        return updated to RewardSummary(
            coins = coins,
            xp = xp,
            levelBefore = levelBefore,
            levelAfter = updated.levelInfo.level,
            achievements = unlocked,
            previousStars = previousStars,
            dailyFirstClear = dailyFirst,
            dailyStreak = streak,
            newSurvivalBest = config.type == GameType.SURVIVAL && result.roundsCleared > profile.survivalBest,
        )
    }
}

private val TIMED_TYPES = setOf(GameType.CLASSIC, GameType.CAMPAIGN, GameType.DAILY)

private fun Achievement.isEarned(p: Profile, r: GameResult): Boolean {
    val type = r.config.type
    val solo = type != GameType.DUEL
    return when (this) {
        Achievement.FIRST_WIN -> p.gamesWon >= 1
        Achievement.PERFECT -> r.won && solo && r.slips == 0 && r.pairCount >= 8
        Achievement.COMBO_5, Achievement.COMBO_8 -> p.bestCombo >= goal
        Achievement.SPEEDSTER -> r.won && type in TIMED_TYPES && r.pairCount >= 8 && r.elapsedSeconds <= 30
        Achievement.MASTER -> r.won && solo && r.pairCount == CARD_ART_COUNT
        Achievement.PURIST -> r.won && type in TIMED_TYPES && r.pairCount == CARD_ART_COUNT && r.boostersUsed == 0
        Achievement.NIGHT_OWL -> r.won && r.hourOfDay in 0..4
        else -> progress(p) >= goal
    }
}

/** Progress towards a counter based achievement; event based ones report all-or-nothing. */
fun Achievement.progress(p: Profile): Int {
    val value = when (this) {
        Achievement.COMBO_5, Achievement.COMBO_8 -> p.bestCombo
        Achievement.CAMPAIGN_10, Achievement.CAMPAIGN_ALL -> p.levelsCleared
        Achievement.STARS_90 -> p.totalStars
        Achievement.SURVIVOR_5, Achievement.SURVIVOR_10 -> p.survivalBest
        Achievement.DAILY_FIRST -> p.dailyCompleted
        Achievement.STREAK_7 -> p.dailyBestStreak
        Achievement.DUELIST -> p.duelGames
        Achievement.ZEN_10 -> p.zenGames
        Achievement.GAMES_50, Achievement.GAMES_200 -> p.gamesPlayed
        Achievement.MATCHES_1000 -> p.totalMatches
        Achievement.LEVEL_10 -> p.levelInfo.level
        Achievement.ICE_50 -> p.iceCracked
        Achievement.GOLDEN_25 -> p.goldenMatched
        Achievement.FIRST_WIN -> p.gamesWon
        else -> if (this in p.achievements) goal else 0
    }
    return value.coerceAtMost(goal)
}

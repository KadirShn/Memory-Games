package com.kadirsahin.hafizaoyunu.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionTest {
    private fun result(
        config: GameConfig = GameConfig.classic(GameMode.EASY),
        won: Boolean = true,
        moves: Int = 10,
        slips: Int = 1,
        stars: Int = 2,
        today: String = "2026-10-23",
        yesterday: String = "2026-10-22",
        hour: Int = 14,
        rounds: Int = 0,
    ) = GameResult(
        config = config,
        won = won,
        score = 1_200,
        moves = moves,
        matches = config.pairCount,
        pairCount = config.pairCount,
        slips = slips,
        bestCombo = 3,
        elapsedSeconds = 45,
        stars = stars,
        roundsCleared = rounds,
        boostersUsed = 0,
        iceCracked = 0,
        goldenMatched = 0,
        hourOfDay = hour,
        today = today,
        yesterday = yesterday,
    )

    @Test
    fun firstWin_grantsCoinsXpAndAchievement() {
        val (profile, reward) = Progression.apply(Profile(), result())
        assertTrue(Achievement.FIRST_WIN in reward.achievements)
        assertEquals(Profile.STARTING_COINS + reward.coins + Achievement.FIRST_WIN.reward, profile.coins)
        assertEquals(reward.xp, profile.xp)
        assertEquals(1, profile.gamesWon)
    }

    @Test
    fun campaignStars_keepTheBestAndOnlyRewardNewStars() {
        val config = Campaign.level(3)!!.config()
        val (afterThree, firstReward) = Progression.apply(Profile(), result(config = config, stars = 3))
        val (afterOne, secondReward) = Progression.apply(afterThree, result(config = config, stars = 1))
        assertEquals(3, afterOne.campaignStars[3])
        assertTrue(firstReward.coins > secondReward.coins)
        assertEquals(3, secondReward.previousStars)
    }

    @Test
    fun dailyStreak_growsOnConsecutiveDays_andResetsAfterAGap() {
        val daily = GameConfig.daily("2026-10-22")
        val (day1, _) = Progression.apply(Profile(), result(config = daily, today = "2026-10-22", yesterday = "2026-10-21"))
        val (day2, reward2) = Progression.apply(day1, result(config = daily, today = "2026-10-23", yesterday = "2026-10-22"))
        assertEquals(2, reward2.dailyStreak)
        val (again, rewardAgain) = Progression.apply(day2, result(config = daily, today = "2026-10-23", yesterday = "2026-10-22"))
        assertFalse("Replaying the same day is not a new clear", rewardAgain.dailyFirstClear)
        assertEquals(2, again.dailyStreak)
        val (day5, reward5) = Progression.apply(again, result(config = daily, today = "2026-10-26", yesterday = "2026-10-25"))
        assertEquals(1, reward5.dailyStreak)
        assertEquals(2, day5.dailyBestStreak)
    }

    @Test
    fun nightOwl_onlyUnlocksAtNight() {
        val (_, day) = Progression.apply(Profile(), result(hour = 14))
        val (_, night) = Progression.apply(Profile(), result(hour = 2))
        assertFalse(Achievement.NIGHT_OWL in day.achievements)
        assertTrue(Achievement.NIGHT_OWL in night.achievements)
    }

    @Test
    fun leveling_carriesOverflowIntoTheNextLevel() {
        assertEquals(1, Leveling.levelFor(0).level)
        assertEquals(2, Leveling.levelFor(Leveling.xpForNext(1)).level)
        val info = Leveling.levelFor(Leveling.xpForNext(1) + 10)
        assertEquals(10, info.xpIntoLevel)
    }

    @Test
    fun configs_roundTripThroughRouteEncoding() {
        listOf(
            GameConfig.classic(GameMode.MASTER),
            GameConfig.daily("2026-10-23"),
            GameConfig.survival(),
            GameConfig.duel(GameMode.FOCUS),
            Campaign.level(30)!!.config(),
        ).forEach { config ->
            val encoded = config.encode()
            assertFalse(':' in encoded)
            assertEquals(config, GameConfig.decode(encoded))
        }
        assertNull(GameConfig.decode("garbage"))
    }

    @Test
    fun campaign_hasThirtyValidLevelsAndUnlocksSequentially() {
        assertEquals(30, Campaign.levels.size)
        Campaign.levels.forEach { assertNotNull(it.config()) }
        assertTrue(Campaign.isUnlocked(1, emptyMap()))
        assertFalse(Campaign.isUnlocked(2, emptyMap()))
        assertTrue(Campaign.isUnlocked(2, mapOf(1 to 1)))
    }

    @Test
    fun survivalRounds_growAndStayWithinArtworkLimits() {
        (1..20).forEach { round ->
            val spec = Survival.round(round)
            assertTrue(spec.rows * spec.columns / 2 <= CARD_ART_COUNT)
            assertTrue(spec.seconds > 0)
        }
        assertEquals(6, Survival.round(1).rows * Survival.round(1).columns / 2)
    }

    @Test
    fun dailyBoard_isTheSameForEveryoneOnTheSameDay() {
        assertEquals(GameConfig.daily("2026-10-23"), GameConfig.daily("2026-10-23"))
    }
}

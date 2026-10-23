package com.kadirsahin.hafizaoyunu.domain

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random

/** Every way the game can be played. [storageKey] is persisted in the score table. */
enum class GameType(val storageKey: String) {
    CLASSIC("classic"),
    CAMPAIGN("campaign"),
    DAILY("daily"),
    SURVIVAL("survival"),
    ZEN("zen"),
    DUEL("duel"),
}

/** Rule modifiers that make a board behave differently. */
enum class Twist {
    /** The board starts hidden, without the memorisation preview. */
    NO_PREVIEW,

    /** Every few mismatches the hidden cards trade places. */
    SHUFFLE,

    /** Some cards are frozen and need an extra tap before they can be flipped. */
    ICE,

    /** One pair is golden and is worth triple points. */
    GOLDEN,

    /** Hourglass pairs add bonus seconds to the clock. */
    HOURGLASS,
}

/** Number of unique card artworks every card theme provides. */
const val CARD_ART_COUNT = 18

data class GameConfig(
    val type: GameType,
    val rows: Int,
    val columns: Int,
    val timeLimitSeconds: Int? = null,
    val lives: Int? = null,
    val twists: Set<Twist> = emptySet(),
    val seed: Long? = null,
    val level: Int? = null,
    val players: Int = 1,
    val dayKey: String? = null,
) {
    init {
        require(rows * columns % 2 == 0) { "A board needs an even number of cards" }
        require(rows * columns / 2 <= CARD_ART_COUNT) { "Not enough artwork for this board" }
    }

    val cardCount: Int get() = rows * columns
    val pairCount: Int get() = cardCount / 2
    val boardLabel: String get() = "${columns}x$rows"
    val hasTimer: Boolean get() = timeLimitSeconds != null || type == GameType.SURVIVAL
    val boostersAllowed: Boolean get() = type != GameType.DUEL && type != GameType.ZEN
    val classicMode: GameMode? get() = GameMode.entries.firstOrNull { it.rows == rows && it.columns == columns }

    /** Compact, colon free representation used inside navigation routes. */
    fun encode(): String = listOf(
        type.name,
        rows,
        columns,
        timeLimitSeconds ?: "",
        lives ?: "",
        twists.joinToString(".") { it.name },
        seed ?: "",
        level ?: "",
        players,
        dayKey ?: "",
    ).joinToString("~")

    companion object {
        fun decode(value: String): GameConfig? = runCatching {
            val parts = value.split("~")
            GameConfig(
                type = GameType.valueOf(parts[0]),
                rows = parts[1].toInt(),
                columns = parts[2].toInt(),
                timeLimitSeconds = parts[3].toIntOrNull(),
                lives = parts[4].toIntOrNull(),
                twists = parts[5].split(".").filter { it.isNotEmpty() }.map { Twist.valueOf(it) }.toSet(),
                seed = parts[6].toLongOrNull(),
                level = parts[7].toIntOrNull(),
                players = parts[8].toInt(),
                dayKey = parts[9].ifEmpty { null },
            )
        }.getOrNull()

        fun classic(mode: GameMode) = GameConfig(
            type = GameType.CLASSIC,
            rows = mode.rows,
            columns = mode.columns,
            timeLimitSeconds = mode.durationSeconds,
        )

        fun zen(mode: GameMode) = GameConfig(type = GameType.ZEN, rows = mode.rows, columns = mode.columns)

        fun duel(mode: GameMode) = GameConfig(
            type = GameType.DUEL,
            rows = mode.rows,
            columns = mode.columns,
            players = 2,
        )

        fun survival(): GameConfig {
            val first = Survival.round(1)
            return GameConfig(
                type = GameType.SURVIVAL,
                rows = first.rows,
                columns = first.columns,
                lives = Survival.STARTING_LIVES,
            )
        }

        fun daily(dayKey: String = DayKey.today()): GameConfig {
            val seed = dayKey.filter { it.isDigit() }.toLong()
            val random = Random(seed)
            val (columns, rows) = listOf(4 to 4, 4 to 5, 4 to 6, 5 to 6).random(random)
            val pool = listOf(Twist.SHUFFLE, Twist.ICE, Twist.GOLDEN, Twist.HOURGLASS, Twist.NO_PREVIEW)
            val twists = pool.shuffled(random).take(1 + random.nextInt(2)).toSet()
            val pairs = rows * columns / 2
            val extra = (if (Twist.NO_PREVIEW in twists) 20 else 0) + (if (Twist.ICE in twists) 10 else 0)
            return GameConfig(
                type = GameType.DAILY,
                rows = rows,
                columns = columns,
                timeLimitSeconds = pairs * 8 + 25 + extra,
                twists = twists,
                seed = seed,
                dayKey = dayKey,
            )
        }
    }
}

/** Survival is an endless run of boards that grow in size; lives are lost only on memory slips. */
object Survival {
    const val STARTING_LIVES = 3
    const val MAX_LIVES = 5

    data class Round(val columns: Int, val rows: Int, val twists: Set<Twist>, val seconds: Int)

    fun round(number: Int): Round {
        val (columns, rows) = when (number) {
            1 -> 3 to 4
            2 -> 4 to 4
            3 -> 4 to 5
            4 -> 4 to 6
            5 -> 5 to 6
            else -> 6 to 6
        }
        val twists = buildSet {
            if (number >= 4) add(Twist.GOLDEN)
            if (number >= 5) add(Twist.HOURGLASS)
            if (number >= 6) add(Twist.SHUFFLE)
            if (number >= 7) add(Twist.ICE)
            if (number >= 9) add(Twist.NO_PREVIEW)
        }
        val pairs = rows * columns / 2
        val seconds = (pairs * 7 + 20 - (number - 6).coerceAtLeast(0) * 6).coerceAtLeast(pairs * 5)
        return Round(columns, rows, twists, seconds)
    }
}

/** Day keys are plain `yyyy-MM-dd` strings; Calendar keeps this compatible with API 24 without desugaring. */
object DayKey {
    fun today(now: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): String =
        format(Calendar.getInstance(zone).apply { timeInMillis = now })

    fun yesterday(now: Long = System.currentTimeMillis(), zone: TimeZone = TimeZone.getDefault()): String =
        format(Calendar.getInstance(zone).apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
        })

    private fun format(calendar: Calendar): String = String.format(
        Locale.US,
        "%04d-%02d-%02d",
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH),
    )
}

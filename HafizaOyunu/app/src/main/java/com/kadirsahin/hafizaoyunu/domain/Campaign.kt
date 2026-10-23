package com.kadirsahin.hafizaoyunu.domain

import kotlin.math.ceil

data class LevelSpec(
    val number: Int,
    val chapter: Int,
    val columns: Int,
    val rows: Int,
    val seconds: Int,
    val twists: Set<Twist> = emptySet(),
) {
    val pairCount: Int get() = rows * columns / 2
    val threeStarMoves: Int get() = StarRating.threeStarMoves(pairCount, twists)
    val twoStarMoves: Int get() = StarRating.twoStarMoves(pairCount, twists)

    fun config() = GameConfig(
        type = GameType.CAMPAIGN,
        rows = rows,
        columns = columns,
        timeLimitSeconds = seconds,
        twists = twists,
        level = number,
    )
}

/** Stars reward efficient play: the fewer pair attempts, the more stars. Twists loosen the limits. */
object StarRating {
    fun threeStarMoves(pairs: Int, twists: Set<Twist>): Int = ceil(pairs * tolerance(1.3, twists)).toInt()
    fun twoStarMoves(pairs: Int, twists: Set<Twist>): Int = ceil(pairs * tolerance(1.75, twists)).toInt()

    fun stars(pairs: Int, twists: Set<Twist>, won: Boolean, moves: Int): Int = when {
        !won -> 0
        moves <= threeStarMoves(pairs, twists) -> 3
        moves <= twoStarMoves(pairs, twists) -> 2
        else -> 1
    }

    private fun tolerance(base: Double, twists: Set<Twist>): Double {
        var value = base
        if (Twist.NO_PREVIEW in twists) value += 0.4
        if (Twist.SHUFFLE in twists) value += 0.15
        return value
    }
}

/** Thirty hand tuned levels in five chapters, each chapter introducing a new twist. */
object Campaign {
    const val CHAPTER_COUNT = 5
    const val LEVELS_PER_CHAPTER = 6

    private val NP = Twist.NO_PREVIEW
    private val SH = Twist.SHUFFLE
    private val ICE = Twist.ICE
    private val GO = Twist.GOLDEN
    private val HG = Twist.HOURGLASS

    val levels: List<LevelSpec> = listOf(
        // 1 · Awakening
        spec(3, 4, 60),
        spec(4, 4, 75),
        spec(4, 4, 65, GO),
        spec(4, 5, 90),
        spec(4, 5, 80, HG),
        spec(4, 6, 110, GO),
        // 2 · Storm
        spec(4, 4, 75, SH),
        spec(4, 5, 95, SH),
        spec(4, 5, 90, SH, GO),
        spec(4, 6, 115, SH),
        spec(4, 6, 105, SH, HG),
        spec(5, 6, 145, SH),
        // 3 · Frostland
        spec(4, 4, 80, ICE),
        spec(4, 5, 100, ICE),
        spec(4, 5, 95, ICE, GO),
        spec(4, 6, 125, ICE, SH),
        spec(5, 6, 155, ICE, HG),
        spec(5, 6, 150, ICE, SH),
        // 4 · Darkness
        spec(4, 4, 85, NP),
        spec(4, 5, 105, NP),
        spec(4, 5, 100, NP, HG),
        spec(4, 6, 130, NP, GO),
        spec(5, 6, 165, NP, ICE),
        spec(5, 6, 160, NP, SH),
        // 5 · Legend
        spec(6, 6, 180),
        spec(6, 6, 175, GO, HG),
        spec(6, 6, 180, SH),
        spec(6, 6, 185, ICE),
        spec(6, 6, 195, NP, HG),
        spec(6, 6, 200, NP, SH, ICE, GO),
    ).mapIndexed { index, level -> level.copy(number = index + 1, chapter = index / LEVELS_PER_CHAPTER + 1) }

    val maxStars: Int get() = levels.size * 3

    fun level(number: Int): LevelSpec? = levels.getOrNull(number - 1)

    fun isUnlocked(number: Int, stars: Map<Int, Int>): Boolean =
        number == 1 || (stars[number - 1] ?: 0) > 0

    private fun spec(columns: Int, rows: Int, seconds: Int, vararg twists: Twist) =
        LevelSpec(number = 0, chapter = 0, columns = columns, rows = rows, seconds = seconds, twists = twists.toSet())
}

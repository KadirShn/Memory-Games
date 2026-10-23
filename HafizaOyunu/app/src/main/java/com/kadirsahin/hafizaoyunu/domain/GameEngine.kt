package com.kadirsahin.hafizaoyunu.domain

import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.random.Random

enum class SpecialKind { GOLDEN, HOURGLASS }

data class MemoryCard(
    val id: Int,
    val pairId: Int,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false,
    val special: SpecialKind? = null,
    /** Remaining ice layers; a frozen card cannot be flipped until this reaches zero. */
    val ice: Int = 0,
    /** Index of the player who matched this card, or -1. */
    val owner: Int = -1,
)

data class GameSnapshot(
    val cards: List<MemoryCard>,
    val moves: Int,
    val matches: Int,
    val score: Int,
    val combo: Int,
    val bestCombo: Int,
    val slips: Int,
    val inputLocked: Boolean,
    val currentPlayer: Int,
    val playerPairs: List<Int>,
    val iceCracked: Int,
    val goldenMatched: Int,
)

enum class SelectionResult { IGNORED, ICE_CRACKED, FIRST_CARD, MATCH, MISMATCH, WON }

data class SelectionOutcome(
    val result: SelectionResult,
    val indices: List<Int> = emptyList(),
    val points: Int = 0,
    val multiplier: Float = 1f,
    val special: SpecialKind? = null,
    /** True when the mismatch was a memory error: the player had already seen the information needed. */
    val slip: Boolean = false,
)

/**
 * Pure Kotlin game rules. Timers, animation and persistence intentionally live outside this class so the
 * rules stay deterministic and unit testable.
 */
class GameEngine(
    val rows: Int,
    val columns: Int,
    deck: List<Int>,
    specials: Map<Int, SpecialKind> = emptyMap(),
    icedIndices: Set<Int> = emptySet(),
    val players: Int = 1,
    private val shuffleEvery: Int = 0,
    private val random: Random = Random.Default,
) {
    val pairCount: Int = rows * columns / 2

    private var cards = deck.mapIndexed { index, pairId ->
        MemoryCard(
            id = index,
            pairId = pairId,
            special = specials[pairId],
            ice = if (index in icedIndices) 1 else 0,
        )
    }
    private var firstSelection: Int? = null
    private var mismatchPair: Pair<Int, Int>? = null
    private val seenCardIds = mutableSetOf<Int>()
    private val playerPairs = IntArray(players)
    private var moves = 0
    private var matches = 0
    private var score = 0
    private var combo = 0
    private var bestCombo = 0
    private var slips = 0
    private var mismatches = 0
    private var currentPlayer = 0
    private var iceCracked = 0
    private var goldenMatched = 0

    /** Set after enough mismatches with the [Twist.SHUFFLE] twist; consumed by [shuffleHidden]. */
    var pendingShuffle: Boolean = false
        private set

    init {
        require(deck.size == rows * columns) { "Deck size must match the board" }
        require(deck.groupingBy { it }.eachCount().values.all { it == 2 }) {
            "Every pair id must occur exactly twice"
        }
        require(players >= 1)
    }

    fun snapshot(): GameSnapshot = GameSnapshot(
        cards = cards,
        moves = moves,
        matches = matches,
        score = score,
        combo = combo,
        bestCombo = bestCombo,
        slips = slips,
        inputLocked = mismatchPair != null,
        currentPlayer = currentPlayer,
        playerPairs = playerPairs.toList(),
        iceCracked = iceCracked,
        goldenMatched = goldenMatched,
    )

    fun selectCard(index: Int): SelectionOutcome {
        val card = cards.getOrNull(index) ?: return IGNORED
        if (mismatchPair != null || card.isMatched || card.isFaceUp) return IGNORED

        if (card.ice > 0) {
            cards = cards.replace(index, card.copy(ice = card.ice - 1))
            iceCracked += 1
            return SelectionOutcome(SelectionResult.ICE_CRACKED, listOf(index))
        }

        cards = cards.replace(index, card.copy(isFaceUp = true))
        val first = firstSelection
        if (first == null) {
            firstSelection = index
            return SelectionOutcome(SelectionResult.FIRST_CARD, listOf(index))
        }

        moves += 1
        firstSelection = null
        val a = cards[first]
        val b = cards[index]

        if (a.pairId != b.pairId) {
            val partnerOfA = cards.first { it.pairId == a.pairId && it.id != a.id }
            val slip = partnerOfA.id in seenCardIds || b.id in seenCardIds
            if (slip) {
                slips += 1
                score = (score - SLIP_PENALTY).coerceAtLeast(0)
            }
            seenCardIds += a.id
            seenCardIds += b.id
            combo = 0
            mismatches += 1
            mismatchPair = first to index
            if (shuffleEvery > 0 && mismatches % shuffleEvery == 0) pendingShuffle = true
            return SelectionOutcome(SelectionResult.MISMATCH, listOf(first, index), slip = slip)
        }

        combo += 1
        bestCombo = max(bestCombo, combo)
        matches += 1
        val multiplier = comboMultiplier(combo)
        var points = (MATCH_POINTS * multiplier).roundToInt()
        if (a.special == SpecialKind.GOLDEN) {
            points *= GOLDEN_FACTOR
            goldenMatched += 1
        }
        score += points
        cards = cards
            .replace(first, a.copy(isMatched = true, owner = currentPlayer))
            .replace(index, b.copy(isMatched = true, owner = currentPlayer))
        playerPairs[currentPlayer] += 1
        seenCardIds += a.id
        seenCardIds += b.id
        return SelectionOutcome(
            result = if (matches == pairCount) SelectionResult.WON else SelectionResult.MATCH,
            indices = listOf(first, index),
            points = points,
            multiplier = multiplier,
            special = a.special,
        )
    }

    /** Turns the two mismatched cards back over and hands the turn to the next player. */
    fun resolveMismatch() {
        val (first, second) = mismatchPair ?: return
        cards = cards
            .replace(first, cards[first].copy(isFaceUp = false))
            .replace(second, cards[second].copy(isFaceUp = false))
        mismatchPair = null
        currentPlayer = (currentPlayer + 1) % players
    }

    /** Rearranges all hidden cards. Returns true when the board actually changed. */
    fun shuffleHidden(): Boolean {
        if (!pendingShuffle || firstSelection != null || mismatchPair != null) return false
        pendingShuffle = false
        val slots = cards.indices.filter { !cards[it].isMatched && !cards[it].isFaceUp }
        if (slots.size < 3) return false
        val moved = slots.map { cards[it] }.shuffled(random)
        cards = cards.toMutableList().also { list -> slots.forEachIndexed { i, slot -> list[slot] = moved[i] } }
        // The player can no longer know where these cards are, so forgetting them keeps slip detection fair.
        seenCardIds.removeAll(moved.map { it.id }.toSet())
        return true
    }

    /** Indices worth highlighting: the open card's partner, or a random hidden pair. */
    fun hint(): List<Int> {
        firstSelection?.let { first ->
            val partner = cards.indexOfFirst { it.pairId == cards[first].pairId && it.id != cards[first].id }
            return listOf(partner)
        }
        return cards.indices
            .filter { !cards[it].isMatched }
            .groupBy { cards[it].pairId }
            .values
            .filter { it.size == 2 }
            .randomOrNull(random)
            .orEmpty()
    }

    companion object {
        const val MATCH_POINTS = 100
        const val SLIP_PENALTY = 25
        const val GOLDEN_FACTOR = 3
        private val IGNORED = SelectionOutcome(SelectionResult.IGNORED)

        fun comboMultiplier(combo: Int): Float = 1f + (combo - 1).coerceIn(0, 6) * 0.5f

        /** Builds a fully randomised engine for a board; a seeded [random] yields identical boards. */
        fun create(
            rows: Int,
            columns: Int,
            twists: Set<Twist>,
            players: Int = 1,
            random: Random = Random.Default,
        ): GameEngine {
            val pairCount = rows * columns / 2
            val artwork = (0 until CARD_ART_COUNT).shuffled(random).take(pairCount)
            val deck = artwork.flatMap { listOf(it, it) }.shuffled(random)
            val specials = buildMap {
                var cursor = 0
                if (Twist.GOLDEN in twists) put(artwork[cursor++], SpecialKind.GOLDEN)
                if (Twist.HOURGLASS in twists) {
                    repeat(if (pairCount >= 12) 2 else 1) {
                        if (cursor < artwork.size) put(artwork[cursor++], SpecialKind.HOURGLASS)
                    }
                }
            }
            val iced = if (Twist.ICE in twists) {
                deck.indices.shuffled(random).take(deck.size / 4).toSet()
            } else {
                emptySet()
            }
            return GameEngine(
                rows = rows,
                columns = columns,
                deck = deck,
                specials = specials,
                icedIndices = iced,
                players = players,
                shuffleEvery = if (Twist.SHUFFLE in twists) SHUFFLE_EVERY else 0,
                random = random,
            )
        }

        const val SHUFFLE_EVERY = 3
    }
}

private fun <T> List<T>.replace(index: Int, value: T): List<T> =
    toMutableList().also { it[index] = value }

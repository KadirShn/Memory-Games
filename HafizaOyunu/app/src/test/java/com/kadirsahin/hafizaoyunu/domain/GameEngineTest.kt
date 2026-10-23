package com.kadirsahin.hafizaoyunu.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {
    /** Pairs sit next to each other: indices (0,1), (2,3), … */
    private val pairedDeck = List(8) { pair -> listOf(pair, pair) }.flatten()

    private fun engine(
        deck: List<Int> = pairedDeck,
        specials: Map<Int, SpecialKind> = emptyMap(),
        iced: Set<Int> = emptySet(),
        players: Int = 1,
        shuffleEvery: Int = 0,
    ) = GameEngine(4, 4, deck, specials, iced, players, shuffleEvery, Random(1))

    @Test
    fun matchingPair_updatesScoreMatchAndCombo() {
        val engine = engine()

        assertEquals(SelectionResult.FIRST_CARD, engine.selectCard(0).result)
        val outcome = engine.selectCard(1)
        assertEquals(SelectionResult.MATCH, outcome.result)
        assertEquals(GameEngine.MATCH_POINTS, outcome.points)

        val state = engine.snapshot()
        assertEquals(GameEngine.MATCH_POINTS, state.score)
        assertEquals(1, state.matches)
        assertEquals(1, state.moves)
        assertEquals(1, state.combo)
        assertTrue(state.cards[0].isMatched && state.cards[1].isMatched)
    }

    @Test
    fun consecutiveMatches_growTheComboMultiplier() {
        val engine = engine()
        engine.selectCard(0); engine.selectCard(1)
        engine.selectCard(2)
        val second = engine.selectCard(3)
        engine.selectCard(4)
        val third = engine.selectCard(5)

        assertEquals(1.5f, second.multiplier)
        assertEquals(150, second.points)
        assertEquals(2f, third.multiplier)
        assertEquals(100 + 150 + 200, engine.snapshot().score)
        assertEquals(4f, GameEngine.comboMultiplier(20))
    }

    @Test
    fun mismatch_locksInputUntilResolved_andResetsCombo() {
        val engine = engine()
        engine.selectCard(0); engine.selectCard(1)
        engine.selectCard(2)
        assertEquals(SelectionResult.MISMATCH, engine.selectCard(4).result)
        assertTrue(engine.snapshot().inputLocked)
        assertEquals(0, engine.snapshot().combo)
        assertEquals(SelectionResult.IGNORED, engine.selectCard(6).result)

        engine.resolveMismatch()

        val state = engine.snapshot()
        assertFalse(state.inputLocked)
        assertFalse(state.cards[2].isFaceUp)
        assertFalse(state.cards[4].isFaceUp)
    }

    @Test
    fun exploringNewCards_isNotASlip_butForgettingASeenCardIs() {
        val engine = engine()
        val first = engine.selectCard(0).let { engine.selectCard(2) }
        assertFalse("Two never seen cards cannot be a memory slip", first.slip)
        engine.resolveMismatch()

        // Two more unseen cards are still exploration…
        engine.selectCard(4)
        val explore = engine.selectCard(6)
        assertFalse(explore.slip)
        engine.resolveMismatch()

        // …but opening 0 and then the already seen card 2 again is a slip.
        engine.selectCard(0)
        val slip = engine.selectCard(2)
        assertTrue(slip.slip)
        assertEquals(1, engine.snapshot().slips)
    }

    @Test
    fun finalPair_returnsWon() {
        val engine = engine()
        var result = SelectionResult.IGNORED
        pairedDeck.indices.chunked(2).forEach { (a, b) ->
            engine.selectCard(a)
            result = engine.selectCard(b).result
        }
        assertEquals(SelectionResult.WON, result)
        assertEquals(8, engine.snapshot().moves)
        assertEquals(8, engine.snapshot().bestCombo)
    }

    @Test
    fun icedCard_needsATapToCrackBeforeFlipping() {
        val engine = engine(iced = setOf(0))
        assertEquals(SelectionResult.ICE_CRACKED, engine.selectCard(0).result)
        assertFalse(engine.snapshot().cards[0].isFaceUp)
        assertEquals(SelectionResult.FIRST_CARD, engine.selectCard(0).result)
        assertEquals(1, engine.snapshot().iceCracked)
    }

    @Test
    fun goldenPair_isWorthTriplePoints() {
        val engine = engine(specials = mapOf(0 to SpecialKind.GOLDEN))
        engine.selectCard(0)
        val outcome = engine.selectCard(1)
        assertEquals(SpecialKind.GOLDEN, outcome.special)
        assertEquals(GameEngine.MATCH_POINTS * GameEngine.GOLDEN_FACTOR, outcome.points)
        assertEquals(1, engine.snapshot().goldenMatched)
    }

    @Test
    fun duel_mismatchPassesTurn_matchKeepsIt() {
        val engine = engine(players = 2)
        engine.selectCard(0); engine.selectCard(1)
        assertEquals(0, engine.snapshot().currentPlayer)
        assertEquals(listOf(1, 0), engine.snapshot().playerPairs)

        engine.selectCard(2); engine.selectCard(4)
        engine.resolveMismatch()
        assertEquals(1, engine.snapshot().currentPlayer)
        engine.selectCard(2); engine.selectCard(3)
        assertEquals(listOf(1, 1), engine.snapshot().playerPairs)
        assertEquals(1, engine.snapshot().cards[2].owner)
    }

    @Test
    fun shuffleTwist_movesOnlyHiddenCards() {
        val engine = engine(shuffleEvery = 1)
        engine.selectCard(0); engine.selectCard(1)
        val matchedBefore = engine.snapshot().cards.take(2)
        val hiddenBefore = engine.snapshot().cards.drop(2).map { it.id }
        engine.selectCard(2); engine.selectCard(4)
        assertTrue(engine.pendingShuffle)
        assertFalse("Cannot shuffle while the mismatch is visible", engine.shuffleHidden())
        engine.resolveMismatch()
        assertTrue(engine.shuffleHidden())

        val after = engine.snapshot().cards
        assertEquals(matchedBefore, after.take(2))
        assertEquals(hiddenBefore.toSet(), after.drop(2).map { it.id }.toSet())
        assertNotEquals(hiddenBefore, after.drop(2).map { it.id })
    }

    @Test
    fun hint_pointsAtThePartnerOfTheOpenCard() {
        val engine = engine()
        engine.selectCard(6)
        assertEquals(listOf(7), engine.hint())
    }

    @Test
    fun seededCreation_isDeterministicForDailyBoards() {
        val a = GameEngine.create(5, 6, setOf(Twist.GOLDEN, Twist.ICE), random = Random(20261023))
        val b = GameEngine.create(5, 6, setOf(Twist.GOLDEN, Twist.ICE), random = Random(20261023))
        assertEquals(a.snapshot().cards, b.snapshot().cards)
        assertEquals(30, a.snapshot().cards.size)
        assertEquals(1, a.snapshot().cards.map { it.special }.count { it == SpecialKind.GOLDEN } / 2)
    }
}

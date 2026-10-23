package com.kadirsahin.hafizaoyunu.ui

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kadirsahin.hafizaoyunu.data.Leaderboard
import com.kadirsahin.hafizaoyunu.data.ProfileRepository
import com.kadirsahin.hafizaoyunu.data.ScoreRepository
import com.kadirsahin.hafizaoyunu.domain.Booster
import com.kadirsahin.hafizaoyunu.domain.Campaign
import com.kadirsahin.hafizaoyunu.domain.DayKey
import com.kadirsahin.hafizaoyunu.domain.GameConfig
import com.kadirsahin.hafizaoyunu.domain.GameEngine
import com.kadirsahin.hafizaoyunu.domain.GameMode
import com.kadirsahin.hafizaoyunu.domain.GameResult
import com.kadirsahin.hafizaoyunu.domain.GameSnapshot
import com.kadirsahin.hafizaoyunu.domain.GameType
import com.kadirsahin.hafizaoyunu.domain.MemoryCard
import com.kadirsahin.hafizaoyunu.domain.RewardSummary
import com.kadirsahin.hafizaoyunu.domain.SelectionResult
import com.kadirsahin.hafizaoyunu.domain.SpecialKind
import com.kadirsahin.hafizaoyunu.domain.StarRating
import com.kadirsahin.hafizaoyunu.domain.Survival
import com.kadirsahin.hafizaoyunu.domain.Twist
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class GamePhase { PREVIEW, RUNNING, PAUSED, ROUND_CLEAR, WON, LOST }

/** One-shot moments the UI turns into sound, haptics, particles and floating labels. */
sealed interface GameEvent {
    data class Flip(val index: Int) : GameEvent
    data class Match(
        val indices: List<Int>,
        val points: Int,
        val multiplier: Float,
        val combo: Int,
        val special: SpecialKind?,
    ) : GameEvent
    data class Mismatch(val indices: List<Int>, val slip: Boolean) : GameEvent
    data class IceCrack(val index: Int) : GameEvent
    data object Shuffle : GameEvent
    data class TimeBonus(val index: Int, val seconds: Int) : GameEvent
    data class BoosterUsed(val booster: Booster) : GameEvent
    data class LifeLost(val livesLeft: Int) : GameEvent
    data class RoundClear(val round: Int, val bonus: Int) : GameEvent
    data class Tick(val secondsLeft: Int) : GameEvent
    data object Won : GameEvent
    data object Lost : GameEvent
    data class Rewards(val summary: RewardSummary) : GameEvent
}

data class ScoreBreakdown(val base: Int, val timeBonus: Int, val perfectBonus: Int) {
    val total: Int get() = base + timeBonus + perfectBonus
}

data class GameOutcome(
    val won: Boolean,
    val breakdown: ScoreBreakdown,
    val stars: Int,
    val elapsedSeconds: Int,
    val moves: Int,
    val slips: Int,
    val bestCombo: Int,
    val roundsCleared: Int,
    /** Duel only: index of the winning player, -1 for a draw. */
    val winner: Int?,
    val nextLevel: Int?,
    val isNewBest: Boolean = false,
    val reward: RewardSummary? = null,
)

data class GameUiState(
    val config: GameConfig,
    val boardId: Int = 0,
    val rows: Int = config.rows,
    val columns: Int = config.columns,
    val twists: Set<Twist> = config.twists,
    val cards: List<MemoryCard> = emptyList(),
    val moves: Int = 0,
    val matches: Int = 0,
    val pairCount: Int = config.pairCount,
    val score: Int = 0,
    val combo: Int = 0,
    val slips: Int = 0,
    val remainingMillis: Long? = null,
    val totalMillis: Long? = null,
    val frozenMillis: Long = 0,
    val lives: Int? = config.lives,
    val round: Int = 1,
    val phase: GamePhase = GamePhase.PREVIEW,
    val previewMillis: Long = 0,
    val peeking: Boolean = false,
    val inputLocked: Boolean = true,
    val hints: Set<Int> = emptySet(),
    val currentPlayer: Int = 0,
    val playerPairs: List<Int> = List(config.players) { 0 },
    val boosters: Map<Booster, Int> = emptyMap(),
    val outcome: GameOutcome? = null,
) {
    val timeProgress: Float
        get() {
            val remaining = remainingMillis ?: return 1f
            val total = totalMillis ?: return 1f
            return (remaining.toFloat() / total).coerceIn(0f, 1f)
        }
    val isFinished: Boolean get() = phase == GamePhase.WON || phase == GamePhase.LOST
}

class GameViewModel(
    private val config: GameConfig,
    private val scoreRepository: ScoreRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {
    private val random: Random = config.seed?.let { Random(it) } ?: Random.Default

    private lateinit var engine: GameEngine
    private var boardTwists: Set<Twist> = config.twists
    private var boardId = 0
    private var round = 1
    private var engineCarried = false

    private var carriedScore = 0
    private var carriedMoves = 0
    private var carriedMatches = 0
    private var carriedSlips = 0
    private var carriedIce = 0
    private var carriedGolden = 0
    private var bestComboRun = 0

    private var lives: Int? = config.lives
    private var remaining: Long? = null
    private var total: Long? = null
    private var frozenMillis = 0L
    private var activeMillis = 0L
    private var previewMillis = 0L
    private var phase = GamePhase.PREVIEW
    private var pauseRequested = false
    private var peeking = false
    private var hints = emptySet<Int>()
    private var boostersUsed = 0
    private var outcome: GameOutcome? = null

    private var previewJob: Job? = null
    private var mismatchJob: Job? = null
    private var timerJob: Job? = null
    private var peekJob: Job? = null
    private var hintJob: Job? = null
    private var roundJob: Job? = null

    private val _events = MutableSharedFlow<GameEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private val inventory: StateFlow<Map<Booster, Int>> = profileRepository.profile
        .map { it.boosters }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val _uiState = MutableStateFlow(GameUiState(config = config))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { inventory.collect { publish() } }
        restart()
    }

    fun restart() {
        cancelJobs()
        round = 1
        carriedScore = 0
        carriedMoves = 0
        carriedMatches = 0
        carriedSlips = 0
        carriedIce = 0
        carriedGolden = 0
        bestComboRun = 0
        lives = config.lives
        boostersUsed = 0
        activeMillis = 0
        outcome = null
        startBoard()
        startTimer()
    }

    fun selectCard(index: Int) {
        if (phase != GamePhase.RUNNING || peeking) return
        val selection = engine.selectCard(index)
        when (selection.result) {
            SelectionResult.IGNORED -> return
            SelectionResult.ICE_CRACKED -> emit(GameEvent.IceCrack(index))
            SelectionResult.FIRST_CARD -> {
                hints = hints - index
                emit(GameEvent.Flip(index))
            }
            SelectionResult.MATCH, SelectionResult.WON -> {
                hints = emptySet()
                val snapshot = engine.snapshot()
                bestComboRun = max(bestComboRun, snapshot.combo)
                emit(GameEvent.Flip(index))
                emit(
                    GameEvent.Match(
                        indices = selection.indices,
                        points = selection.points,
                        multiplier = selection.multiplier,
                        combo = snapshot.combo,
                        special = selection.special,
                    ),
                )
                if (selection.special == SpecialKind.HOURGLASS && remaining != null) {
                    remaining = remaining!! + HOURGLASS_BONUS_SECONDS * 1_000L
                    emit(GameEvent.TimeBonus(index, HOURGLASS_BONUS_SECONDS))
                }
                if (selection.result == SelectionResult.WON) {
                    boardCleared()
                    return
                }
            }
            SelectionResult.MISMATCH -> {
                hints = hints - index
                emit(GameEvent.Flip(index))
                emit(GameEvent.Mismatch(selection.indices, selection.slip))
                val currentLives = lives
                if (selection.slip && currentLives != null) {
                    lives = currentLives - 1
                    emit(GameEvent.LifeLost(currentLives - 1))
                    if (currentLives - 1 <= 0) {
                        publish()
                        mismatchJob = viewModelScope.launch {
                            delay(MISMATCH_VISIBLE_MILLIS)
                            finish(won = false)
                        }
                        return
                    }
                }
                mismatchJob = viewModelScope.launch {
                    delay(if (config.players > 1) DUEL_MISMATCH_MILLIS else MISMATCH_VISIBLE_MILLIS)
                    engine.resolveMismatch()
                    if (engine.shuffleHidden()) emit(GameEvent.Shuffle)
                    publish()
                }
            }
        }
        publish()
    }

    fun useBooster(booster: Booster) {
        if (!config.boostersAllowed || phase != GamePhase.RUNNING || peeking) return
        if ((inventory.value[booster] ?: 0) <= 0) return
        when (booster) {
            Booster.FREEZE -> if (remaining == null || frozenMillis > 0) return
            Booster.HINT -> if (hints.isNotEmpty()) return
            Booster.PEEK -> Unit
        }
        viewModelScope.launch {
            if (!profileRepository.consumeBooster(booster)) return@launch
            if (phase != GamePhase.RUNNING) return@launch
            boostersUsed += 1
            emit(GameEvent.BoosterUsed(booster))
            when (booster) {
                Booster.PEEK -> {
                    peeking = true
                    peekJob?.cancel()
                    peekJob = viewModelScope.launch {
                        delay(PEEK_MILLIS)
                        peeking = false
                        publish()
                    }
                }
                Booster.HINT -> {
                    hints = engine.hint().toSet()
                    hintJob?.cancel()
                    hintJob = viewModelScope.launch {
                        delay(HINT_MILLIS)
                        hints = emptySet()
                        publish()
                    }
                }
                Booster.FREEZE -> frozenMillis = FREEZE_MILLIS
            }
            publish()
        }
    }

    fun pause() {
        when (phase) {
            GamePhase.RUNNING -> {
                phase = GamePhase.PAUSED
                publish()
            }
            GamePhase.PREVIEW -> pauseRequested = true
            else -> Unit
        }
    }

    fun resume() {
        if (phase == GamePhase.PAUSED) {
            phase = GamePhase.RUNNING
            publish()
        }
    }

    /** Stops every running job; called when the player leaves the game screen. */
    fun shutdown() {
        cancelJobs()
    }

    override fun onCleared() {
        cancelJobs()
    }

    private fun startBoard() {
        val rows: Int
        val columns: Int
        if (config.type == GameType.SURVIVAL) {
            val spec = Survival.round(round)
            rows = spec.rows
            columns = spec.columns
            boardTwists = spec.twists
            remaining = spec.seconds * 1_000L
        } else {
            rows = config.rows
            columns = config.columns
            boardTwists = config.twists
            remaining = config.timeLimitSeconds?.times(1_000L)
        }
        total = remaining
        engine = GameEngine.create(rows, columns, boardTwists, config.players, random)
        engineCarried = false
        boardId += 1
        frozenMillis = 0
        hints = emptySet()
        peeking = Twist.NO_PREVIEW !in boardTwists
        val dealMillis = DEAL_BASE_MILLIS + rows * columns * DEAL_STEP_MILLIS
        previewMillis = if (peeking) PREVIEW_BASE_MILLIS + engine.pairCount * PREVIEW_PER_PAIR_MILLIS else 0
        phase = GamePhase.PREVIEW
        publish()
        previewJob?.cancel()
        previewJob = viewModelScope.launch {
            delay(dealMillis + previewMillis)
            peeking = false
            phase = if (pauseRequested) GamePhase.PAUSED else GamePhase.RUNNING
            pauseRequested = false
            publish()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var last = SystemClock.elapsedRealtime()
            var lastWarning = -1
            while (isActive) {
                delay(TIMER_RESOLUTION_MILLIS)
                val now = SystemClock.elapsedRealtime()
                val elapsed = now - last
                last = now
                if (phase != GamePhase.RUNNING) continue
                activeMillis += elapsed
                val current = remaining ?: continue
                if (frozenMillis > 0) {
                    frozenMillis = max(0, frozenMillis - elapsed)
                    publish()
                    continue
                }
                val next = max(0, current - elapsed)
                remaining = next
                val secondsLeft = ((next + 999) / 1_000).toInt()
                if (secondsLeft in 1..LOW_TIME_SECONDS && secondsLeft != lastWarning) {
                    lastWarning = secondsLeft
                    emit(GameEvent.Tick(secondsLeft))
                }
                if (next == 0L) {
                    finish(won = false)
                    break
                }
                publish()
            }
        }
    }

    private fun boardCleared() {
        if (config.type != GameType.SURVIVAL) {
            finish(won = true)
            return
        }
        val snapshot = engine.snapshot()
        val bonus = ROUND_BONUS * round + ((remaining ?: 0) / 1_000).toInt() * 10
        carry(snapshot)
        carriedScore += bonus
        lives = min(Survival.MAX_LIVES, (lives ?: 0) + 1)
        phase = GamePhase.ROUND_CLEAR
        publish()
        emit(GameEvent.RoundClear(round, bonus))
        roundJob = viewModelScope.launch {
            delay(ROUND_CLEAR_MILLIS)
            round += 1
            startBoard()
        }
    }

    private fun carry(snapshot: GameSnapshot) {
        if (engineCarried) return
        engineCarried = true
        carriedScore += snapshot.score
        carriedMoves += snapshot.moves
        carriedMatches += snapshot.matches
        carriedSlips += snapshot.slips
        carriedIce += snapshot.iceCracked
        carriedGolden += snapshot.goldenMatched
    }

    private fun finish(won: Boolean) {
        if (outcome != null) return
        timerJob?.cancel()
        mismatchJob?.cancel()
        peekJob?.cancel()
        hintJob?.cancel()
        peeking = false
        hints = emptySet()
        carry(engine.snapshot())

        val elapsedSeconds = max(1, (activeMillis / 1_000).toInt())
        val timeBonus = if (won && config.type != GameType.SURVIVAL) ((remaining ?: 0) / 1_000).toInt() * TIME_BONUS_PER_SECOND else 0
        val solo = config.type != GameType.DUEL
        val perfectBonus = if (won && solo && carriedSlips == 0) engine.pairCount * PERFECT_BONUS_PER_PAIR else 0
        val breakdown = ScoreBreakdown(carriedScore, timeBonus, perfectBonus)
        val stars = if (solo) StarRating.stars(engine.pairCount, boardTwists, won, carriedMoves) else 0
        val roundsCleared = if (config.type == GameType.SURVIVAL) round - 1 else 0
        val pairs = engine.snapshot().playerPairs
        val winner = if (config.players > 1) {
            val best = pairs.max()
            if (pairs.count { it == best } > 1) -1 else pairs.indexOf(best)
        } else {
            null
        }
        val nextLevel = config.level?.takeIf { won && it < Campaign.levels.size }?.plus(1)
        val result = GameOutcome(
            won = won,
            breakdown = breakdown,
            stars = stars,
            elapsedSeconds = elapsedSeconds,
            moves = carriedMoves,
            slips = carriedSlips,
            bestCombo = bestComboRun,
            roundsCleared = roundsCleared,
            winner = winner,
            nextLevel = nextLevel,
        )
        outcome = result
        phase = if (won) GamePhase.WON else GamePhase.LOST
        publish()
        emit(if (won) GameEvent.Won else GameEvent.Lost)

        viewModelScope.launch {
            withContext(NonCancellable) {
                val board = leaderboard()
                val qualifies = won || (config.type == GameType.SURVIVAL && breakdown.total > 0)
                val previousBest = scoreRepository.scores.value.filter { it.board == board }.maxOfOrNull { it.points }
                val isNewBest = board != null && qualifies && breakdown.total > (previousBest ?: 0)
                if (board != null && qualifies) {
                    runCatching { scoreRepository.record(board, breakdown.total, elapsedSeconds, carriedMoves) }
                }
                val reward = profileRepository.applyResult(
                    GameResult(
                        config = config,
                        won = won,
                        score = breakdown.total,
                        moves = carriedMoves,
                        matches = carriedMatches,
                        pairCount = engine.pairCount,
                        slips = carriedSlips,
                        bestCombo = bestComboRun,
                        elapsedSeconds = elapsedSeconds,
                        stars = stars,
                        roundsCleared = roundsCleared,
                        boostersUsed = boostersUsed,
                        iceCracked = carriedIce,
                        goldenMatched = carriedGolden,
                        hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        today = config.dayKey ?: DayKey.today(),
                        yesterday = DayKey.yesterday(),
                    ),
                )
                outcome = outcome?.copy(isNewBest = isNewBest, reward = reward)
                publish()
                emit(GameEvent.Rewards(reward))
            }
        }
    }

    private fun leaderboard(): Leaderboard? = when (config.type) {
        GameType.CLASSIC -> when (config.classicMode) {
            GameMode.EASY -> Leaderboard.EASY
            GameMode.FOCUS -> Leaderboard.FOCUS
            GameMode.MASTER -> Leaderboard.MASTER
            null -> null
        }
        GameType.SURVIVAL -> Leaderboard.SURVIVAL
        GameType.DAILY -> Leaderboard.DAILY
        else -> null
    }

    private fun publish() {
        if (!::engine.isInitialized) return
        val snapshot = engine.snapshot()
        val live = !engineCarried
        _uiState.value = GameUiState(
            config = config,
            boardId = boardId,
            rows = engine.rows,
            columns = engine.columns,
            twists = boardTwists,
            cards = snapshot.cards,
            moves = carriedMoves + if (live) snapshot.moves else 0,
            matches = snapshot.matches,
            pairCount = engine.pairCount,
            score = carriedScore + if (live) snapshot.score else 0,
            combo = snapshot.combo,
            slips = carriedSlips + if (live) snapshot.slips else 0,
            remainingMillis = remaining,
            totalMillis = total,
            frozenMillis = frozenMillis,
            lives = lives,
            round = round,
            phase = phase,
            previewMillis = previewMillis,
            peeking = peeking,
            inputLocked = snapshot.inputLocked || peeking || phase != GamePhase.RUNNING,
            hints = hints,
            currentPlayer = snapshot.currentPlayer,
            playerPairs = snapshot.playerPairs,
            boosters = inventory.value,
            outcome = outcome,
        )
    }

    private fun emit(event: GameEvent) {
        _events.tryEmit(event)
    }

    private fun cancelJobs() {
        listOf(previewJob, mismatchJob, timerJob, peekJob, hintJob, roundJob).forEach { it?.cancel() }
    }

    companion object {
        const val DEAL_BASE_MILLIS = 450L
        const val DEAL_STEP_MILLIS = 18L
        private const val PREVIEW_BASE_MILLIS = 900L
        private const val PREVIEW_PER_PAIR_MILLIS = 95L
        private const val MISMATCH_VISIBLE_MILLIS = 720L
        private const val DUEL_MISMATCH_MILLIS = 950L
        private const val TIMER_RESOLUTION_MILLIS = 100L
        private const val PEEK_MILLIS = 1_300L
        private const val HINT_MILLIS = 2_200L
        private const val FREEZE_MILLIS = 10_000L
        private const val ROUND_CLEAR_MILLIS = 1_900L
        private const val LOW_TIME_SECONDS = 10
        private const val HOURGLASS_BONUS_SECONDS = 8
        private const val TIME_BONUS_PER_SECOND = 15
        private const val PERFECT_BONUS_PER_PAIR = 40
        private const val ROUND_BONUS = 150

        fun factory(
            config: GameConfig,
            scoreRepository: ScoreRepository,
            profileRepository: ProfileRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                GameViewModel(config, scoreRepository, profileRepository) as T
        }
    }
}

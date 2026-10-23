package com.kadirsahin.hafizaoyunu.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.audio.Sfx
import com.kadirsahin.hafizaoyunu.domain.Booster
import com.kadirsahin.hafizaoyunu.domain.CardBack
import com.kadirsahin.hafizaoyunu.domain.CardTheme
import com.kadirsahin.hafizaoyunu.domain.GameType
import com.kadirsahin.hafizaoyunu.domain.MemoryCard
import com.kadirsahin.hafizaoyunu.domain.SpecialKind
import com.kadirsahin.hafizaoyunu.domain.Survival
import com.kadirsahin.hafizaoyunu.ui.GameEvent
import com.kadirsahin.hafizaoyunu.ui.GameOutcome
import com.kadirsahin.hafizaoyunu.ui.GamePhase
import com.kadirsahin.hafizaoyunu.ui.GameUiState
import com.kadirsahin.hafizaoyunu.ui.GameViewModel
import com.kadirsahin.hafizaoyunu.ui.components.CardBackArt
import com.kadirsahin.hafizaoyunu.ui.components.CardFace
import com.kadirsahin.hafizaoyunu.ui.components.FloatingLabelsOverlay
import com.kadirsahin.hafizaoyunu.ui.components.GameDialog
import com.kadirsahin.hafizaoyunu.ui.components.GlowButton
import com.kadirsahin.hafizaoyunu.ui.components.Glyph
import com.kadirsahin.hafizaoyunu.ui.components.GlyphIcon
import com.kadirsahin.hafizaoyunu.ui.components.GlyphMedallion
import com.kadirsahin.hafizaoyunu.ui.components.LocalFeedback
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.components.ParticleOverlay
import com.kadirsahin.hafizaoyunu.ui.components.Pill
import com.kadirsahin.hafizaoyunu.ui.components.RoundIconButton
import com.kadirsahin.hafizaoyunu.ui.components.SoftButton
import com.kadirsahin.hafizaoyunu.ui.components.formatClock
import com.kadirsahin.hafizaoyunu.ui.components.formatThousands
import com.kadirsahin.hafizaoyunu.ui.components.glyph
import com.kadirsahin.hafizaoyunu.ui.components.gradient
import com.kadirsahin.hafizaoyunu.ui.components.pressable
import com.kadirsahin.hafizaoyunu.ui.components.uppercaseForUi
import com.kadirsahin.hafizaoyunu.ui.components.rememberFloatingLabels
import com.kadirsahin.hafizaoyunu.ui.components.rememberParticleField
import com.kadirsahin.hafizaoyunu.ui.components.titleRes
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Board geometry captured during layout so events can spawn effects exactly over a card. */
private class BoardGeometry {
    var origin = Offset.Zero
    var cell = 0f
    var gap = 0f
    var columns = 1

    fun center(index: Int): Offset {
        val column = index % columns
        val row = index / columns
        return origin + Offset(column * (cell + gap) + cell / 2, row * (cell + gap) + cell / 2)
    }
}

private data class Banner(val id: Int, val title: String, val subtitle: String?, val colors: List<Color>)

@Composable
fun GameScreen(
    state: GameUiState,
    viewModel: GameViewModel,
    cardTheme: CardTheme,
    cardBack: CardBack,
    onExit: () -> Unit,
    onNextLevel: (Int) -> Unit,
) {
    val feedback = LocalFeedback.current
    val reducedMotion = LocalReducedMotion.current
    val gameColors = GameTheme.colors
    val scheme = MaterialTheme.colorScheme
    val particles = rememberParticleField()
    val labels = rememberFloatingLabels()
    val geometry = remember { BoardGeometry() }
    val boardShake = remember { Animatable(0f) }
    val heartShake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    var rootWidth by remember { mutableFloatStateOf(0f) }
    var banner by remember { mutableStateOf<Banner?>(null) }
    var bannerCounter by remember { mutableIntStateOf(0) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    val comboWords = listOf(
        stringResource(R.string.combo_nice),
        stringResource(R.string.combo_great),
        stringResource(R.string.combo_super),
        stringResource(R.string.combo_insane),
    )
    val comboFormat = stringResource(R.string.combo_format)
    val shuffleText = stringResource(R.string.shuffle_banner)
    val goldText = stringResource(R.string.golden_bonus)
    val roundClearFormat = stringResource(R.string.round_clear)
    val roundBonusFormat = stringResource(R.string.round_bonus_format)
    val boosterNames = Booster.entries.associateWith { stringResource(it.titleRes()) }
    val celebration = listOf(scheme.primary, scheme.secondary, scheme.tertiary, gameColors.gold, gameColors.success)

    fun showBanner(title: String, subtitle: String?, colors: List<Color>) {
        bannerCounter += 1
        banner = Banner(bannerCounter, title, subtitle, colors)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) viewModel.pause() }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is GameEvent.Flip -> {
                    feedback.play(Sfx.FLIP, rate = 0.94f + Random.nextFloat() * 0.12f, volume = 0.8f)
                    feedback.haptic(HapticFeedbackType.SegmentTick)
                }
                is GameEvent.Match -> {
                    val rate = 1f + min(event.combo - 1, 6) * 0.06f
                    feedback.play(if (event.combo >= 3) Sfx.COMBO else Sfx.MATCH, rate = rate)
                    feedback.haptic(HapticFeedbackType.Confirm)
                    val golden = event.special == SpecialKind.GOLDEN
                    val colors = if (golden) listOf(gameColors.gold, Color.White, Color(0xFFFFE08A)) else celebration
                    event.indices.forEach { particles.burst(geometry.center(it) - rootOrigin, colors, count = if (golden) 44 else 26) }
                    // Labels rise from the card the player just tapped, which also keeps them clear of the centre banner.
                    val mid = geometry.center(event.indices[1]) - rootOrigin
                    val multiplier = if (event.multiplier > 1f) " ×${formatMultiplier(event.multiplier)}" else ""
                    labels.add("+${event.points}$multiplier", mid, if (golden) gameColors.gold else Color.White, big = golden || event.combo >= 3)
                    if (golden) {
                        showBanner(goldText, null, listOf(gameColors.gold, Color(0xFFE5861C)))
                    } else if (event.combo >= 2 && state.config.type != GameType.DUEL) {
                        val word = comboWords[(event.combo - 2).coerceIn(0, comboWords.lastIndex)]
                        showBanner(comboFormat.format(event.combo), word, listOf(scheme.tertiary, scheme.primary))
                    }
                }
                is GameEvent.Mismatch -> {
                    feedback.play(Sfx.MISMATCH, volume = 0.8f)
                    feedback.haptic(HapticFeedbackType.Reject)
                    if (event.slip) {
                        val mid = geometry.center(event.indices[1]) - rootOrigin
                        if (state.config.type != GameType.DUEL && state.config.type != GameType.ZEN) {
                            labels.add("−25", mid, gameColors.danger)
                        }
                        if (!reducedMotion) scope.launch { shake(boardShake, 14f) }
                    }
                }
                is GameEvent.IceCrack -> {
                    feedback.play(Sfx.ICE)
                    feedback.haptic(HapticFeedbackType.ContextClick)
                    particles.burst(geometry.center(event.index) - rootOrigin, listOf(Color.White, gameColors.ice, Color(0xFFD8F6FF)), count = 20, speed = 650f, size = 8f)
                }
                GameEvent.Shuffle -> {
                    feedback.play(Sfx.SHUFFLE)
                    showBanner(shuffleText, null, listOf(Color(0xFF2F6BFF), Color(0xFF8E44FF)))
                }
                is GameEvent.TimeBonus -> {
                    labels.add("+${event.seconds}s", geometry.center(event.index) - rootOrigin + Offset(0f, -40f), gameColors.ice, big = true)
                }
                is GameEvent.BoosterUsed -> {
                    feedback.play(Sfx.POWER_UP)
                    feedback.haptic(HapticFeedbackType.Confirm)
                    showBanner(boosterNames.getValue(event.booster), null, event.booster.gradient())
                }
                is GameEvent.LifeLost -> {
                    feedback.haptic(HapticFeedbackType.LongPress)
                    scope.launch { shake(heartShake, 10f) }
                }
                is GameEvent.RoundClear -> {
                    feedback.play(Sfx.ROUND)
                    feedback.haptic(HapticFeedbackType.Confirm)
                    particles.confetti(rootWidth, celebration, count = 70)
                    showBanner(roundClearFormat.format(event.round), roundBonusFormat.format(event.bonus), listOf(gameColors.success, scheme.secondary))
                }
                is GameEvent.Tick -> feedback.play(Sfx.TICK, rate = if (event.secondsLeft <= 3) 1.25f else 1f)
                GameEvent.Won -> {
                    feedback.play(Sfx.WIN)
                    feedback.haptic(HapticFeedbackType.LongPress)
                    particles.confetti(rootWidth, celebration)
                }
                GameEvent.Lost -> feedback.play(Sfx.LOSE)
                is GameEvent.Rewards -> {
                    delay(900)
                    feedback.play(Sfx.COIN)
                    if (event.summary.achievements.isNotEmpty() || event.summary.leveledUp) {
                        delay(500)
                        feedback.play(Sfx.ACHIEVEMENT)
                    }
                }
            }
        }
    }

    BackHandler {
        when {
            state.isFinished -> onExit()
            state.phase == GamePhase.PAUSED -> viewModel.resume()
            else -> showExitConfirmation = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                rootOrigin = it.positionInRoot()
                rootWidth = it.size.width.toFloat()
            },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GameHeader(
                state = state,
                onExit = { if (state.isFinished) onExit() else showExitConfirmation = true },
                onPause = viewModel::pause,
            )
            if (state.config.players > 1) {
                DuelPanel(state)
            } else {
                Hud(state, heartShake.value)
            }
            StatusStrip(state)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                val gap = if (state.columns >= 6) 5.dp else if (state.columns == 5) 6.dp else 8.dp
                val cell = min(
                    ((maxWidth - gap * (state.columns - 1)) / state.columns).value,
                    ((maxHeight - gap * (state.rows - 1)) / state.rows).value,
                ).dp
                val boardWidth = cell * state.columns + gap * (state.columns - 1)
                val boardHeight = cell * state.rows + gap * (state.rows - 1)
                val density = LocalDensity.current
                Board(
                    state = state,
                    cell = cell,
                    gap = gap,
                    cardTheme = cardTheme,
                    cardBack = cardBack,
                    onSelect = viewModel::selectCard,
                    modifier = Modifier
                        .size(boardWidth, boardHeight)
                        .offset { IntOffset(boardShake.value.roundToInt(), 0) }
                        .onGloballyPositioned {
                            geometry.origin = it.positionInRoot()
                            geometry.cell = with(density) { cell.toPx() }
                            geometry.gap = with(density) { gap.toPx() }
                            geometry.columns = state.columns
                        },
                )
                BannerOverlay(banner)
            }
            if (state.config.boostersAllowed) {
                BoosterBar(state, onUse = viewModel::useBooster)
            } else {
                Spacer(Modifier.height(16.dp))
            }
        }

        ParticleOverlay(particles, Modifier.fillMaxSize())
        FloatingLabelsOverlay(labels)

        GameDialog(visible = state.phase == GamePhase.PAUSED, onDismiss = viewModel::resume) {
            GlyphIcon(Glyph.PAUSE, tint = scheme.primary, size = 40.dp)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.paused_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.paused_message),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(20.dp))
            GlowButton(stringResource(R.string.resume), viewModel::resume, Modifier.fillMaxWidth(), glyph = Glyph.PLAY)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SoftButton(stringResource(R.string.restart), viewModel::restart, Modifier.weight(1f), glyph = Glyph.REPLAY)
                SoftButton(stringResource(R.string.leave), onExit, Modifier.weight(1f), glyph = Glyph.HOME)
            }
        }

        GameDialog(visible = showExitConfirmation, onDismiss = { showExitConfirmation = false }) {
            GlyphIcon(Glyph.INFO, tint = scheme.tertiary, size = 40.dp)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.quit_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.quit_message), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            GlowButton(stringResource(R.string.keep_playing), { showExitConfirmation = false }, Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            SoftButton(stringResource(R.string.leave), onExit, Modifier.fillMaxWidth(), glyph = Glyph.HOME)
        }

        ResultOverlay(
            state = state,
            onRestart = viewModel::restart,
            onExit = onExit,
            onNextLevel = onNextLevel,
        )
    }
}

private suspend fun shake(animatable: Animatable<Float, *>, amplitude: Float) {
    for (step in listOf(1f, -0.8f, 0.6f, -0.4f, 0.2f, 0f)) {
        animatable.animateTo(amplitude * step, tween(45))
    }
}

private fun formatMultiplier(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else value.toString()

@Composable
private fun GameHeader(state: GameUiState, onExit: () -> Unit, onPause: () -> Unit) {
    val config = state.config
    val title = when (config.type) {
        GameType.CAMPAIGN -> stringResource(R.string.level_format, config.level ?: 1)
        GameType.SURVIVAL -> stringResource(R.string.round_format, state.round)
        else -> stringResource(config.type.titleRes())
    }
    val subtitle = when (config.type) {
        GameType.CLASSIC, GameType.ZEN, GameType.DUEL -> config.classicMode?.let { "${stringResource(it.titleRes())} · ${state.columns}×${state.rows}" }
        GameType.CAMPAIGN -> stringResource(config.type.titleRes()) + " · ${state.columns}×${state.rows}"
        GameType.SURVIVAL -> stringResource(R.string.survival) + " · ${state.columns}×${state.rows}"
        GameType.DAILY -> (config.dayKey ?: "") + " · ${state.columns}×${state.rows}"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RoundIconButton(Glyph.CLOSE, onExit, contentDescription = stringResource(R.string.leave), size = 42.dp)
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
        RoundIconButton(
            Glyph.PAUSE,
            onPause,
            contentDescription = stringResource(R.string.cd_pause),
            enabled = state.phase == GamePhase.RUNNING || state.phase == GamePhase.PREVIEW,
            size = 42.dp,
        )
    }
}

@Composable
private fun Hud(state: GameUiState, heartShake: Float) {
    val animatedScore by animateIntAsState(state.score, tween(450), label = "score")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HudTile(stringResource(R.string.score), animatedScore.formatThousands(), Modifier.weight(1f))
        if (state.remainingMillis != null) {
            TimerRing(state)
        } else {
            HudTile(stringResource(R.string.moves), state.moves.toString(), Modifier.width(92.dp), highlight = true)
        }
        if (state.lives != null) {
            HeartsTile(state.lives, Modifier.weight(1f).offset { IntOffset(heartShake.roundToInt(), 0) })
        } else {
            HudTile(stringResource(R.string.pairs), "${state.matches}/${state.pairCount}", Modifier.weight(1f))
        }
    }
}

@Composable
private fun HudTile(label: String, value: String, modifier: Modifier = Modifier, highlight: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (highlight) scheme.primaryContainer.copy(alpha = 0.7f) else scheme.surfaceContainer.copy(alpha = 0.85f))
            .border(1.dp, Color.White.copy(alpha = if (GameTheme.colors.isDark) 0.07f else 0.5f), RoundedCornerShape(18.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        Text(label.uppercaseForUi(), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun TimerRing(state: GameUiState) {
    val gameColors = GameTheme.colors
    val progress by animateFloatAsState(state.timeProgress, tween(120, easing = LinearEasing), label = "timer")
    val frozen = state.frozenMillis > 0
    val seconds = (((state.remainingMillis ?: 0) + 999) / 1_000).toInt()
    val urgent = seconds <= 10 && !frozen
    val ringColor by animateColorAsState(
        when {
            frozen -> gameColors.ice
            progress < 0.2f -> gameColors.danger
            progress < 0.45f -> gameColors.gold
            else -> gameColors.success
        },
        label = "ringColor",
    )
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(1f, 1.08f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "pulseScale")
    val track = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val reducedMotion = LocalReducedMotion.current
    Box(
        modifier = Modifier
            .size(92.dp)
            .graphicsLayer {
                val s = if (urgent && !reducedMotion) pulseScale else 1f
                scaleX = s
                scaleY = s
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val strokeWidth = size.minDimension * 0.1f
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            drawCircle(ringColor.copy(alpha = 0.12f), size.minDimension / 2 - strokeWidth)
            drawArc(track, -90f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(strokeWidth))
            drawArc(ringColor, -90f, 360f * progress, false, Offset(inset, inset), arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (frozen) {
                GlyphIcon(Glyph.SNOWFLAKE, tint = gameColors.ice, size = 18.dp)
            }
            Text(
                seconds.formatClock(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = if (urgent) gameColors.danger else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun HeartsTile(lives: Int, modifier: Modifier = Modifier) {
    val heart = GameTheme.colors.heart
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
            .padding(vertical = 10.dp, horizontal = 6.dp)
            .semantics { contentDescription = "$lives" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            repeat(Survival.MAX_LIVES) { index ->
                val alive = index < lives
                val scale by animateFloatAsState(if (alive) 1f else 0.75f, spring(dampingRatio = 0.4f), label = "heart")
                GlyphIcon(
                    Glyph.HEART,
                    tint = if (alive) heart else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                    size = 17.dp,
                    filled = alive,
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                )
            }
        }
        Text(stringResource(R.string.lives).uppercaseForUi(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DuelPanel(state: GameUiState) {
    val colors = listOf(GameTheme.colors.playerOne, GameTheme.colors.playerTwo)
    val names = listOf(stringResource(R.string.player_1), stringResource(R.string.player_2))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(2) { player ->
            val active = state.currentPlayer == player && !state.isFinished
            val glow by animateFloatAsState(if (active) 1f else 0f, label = "turn")
            Row(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        val s = 0.95f + 0.05f * glow
                        scaleX = s
                        scaleY = s
                    }
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(colors[player].copy(alpha = 0.12f + 0.3f * glow), MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)),
                        ),
                    )
                    .border(2.dp, colors[player].copy(alpha = 0.15f + 0.85f * glow), RoundedCornerShape(20.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(colors[player]),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${player + 1}", style = MaterialTheme.typography.titleMedium, color = Color.Black.copy(alpha = 0.75f))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(names[player], style = MaterialTheme.typography.labelLarge, maxLines = 1)
                    Text(
                        stringResource(R.string.pairs_format, state.playerPairs.getOrElse(player) { 0 }),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (active) GlyphIcon(Glyph.PLAY, tint = colors[player], size = 16.dp)
            }
        }
    }
}

@Composable
private fun StatusStrip(state: GameUiState) {
    val scheme = MaterialTheme.colorScheme
    val previewProgress = remember(state.boardId) { Animatable(1f) }
    val dealMillis = GameViewModel.DEAL_BASE_MILLIS + state.rows * state.columns * GameViewModel.DEAL_STEP_MILLIS
    LaunchedEffect(state.boardId) {
        if (state.previewMillis > 0) {
            delay(dealMillis)
            previewProgress.animateTo(0f, tween(state.previewMillis.toInt(), easing = LinearEasing))
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        when {
            state.phase == GamePhase.PREVIEW -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (state.peeking) stringResource(R.string.memorize) else stringResource(R.string.get_ready),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                    color = scheme.secondary,
                )
                if (state.peeking) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(140.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(scheme.onSurface.copy(alpha = 0.1f)),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(previewProgress.value)
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Brush.horizontalGradient(listOf(scheme.secondary, scheme.primary))),
                        )
                    }
                }
            }
            state.twists.isNotEmpty() -> Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                state.twists.forEach { twist ->
                    Pill(
                        stringResource(twist.titleRes()),
                        glyph = twist.glyph(),
                        color = scheme.surfaceContainerHigh,
                        contentColor = scheme.onSurfaceVariant,
                        glyphTint = scheme.secondary,
                    )
                }
            }
            state.config.type != GameType.DUEL && state.combo >= 2 -> Text(
                stringResource(R.string.combo_format, state.combo),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black),
                color = scheme.tertiary,
            )
            else -> Unit
        }
    }
}

@Composable
private fun Board(
    state: GameUiState,
    cell: Dp,
    gap: Dp,
    cardTheme: CardTheme,
    cardBack: CardBack,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val cellPx = with(density) { cell.roundToPx() }
    val gapPx = with(density) { gap.roundToPx() }
    val faceUpUnmatched = state.cards.indices.filter { state.cards[it].isFaceUp && !state.cards[it].isMatched }
    val mismatched = if (state.inputLocked && !state.peeking && state.phase == GamePhase.RUNNING && faceUpUnmatched.size == 2) {
        faceUpUnmatched.toSet()
    } else {
        emptySet()
    }
    val corner = if (state.columns >= 6) 10.dp else if (state.columns == 5) 12.dp else 16.dp
    Box(modifier = modifier) {
        state.cards.forEachIndexed { index, card ->
            key(state.boardId, card.id) {
                val column = index % state.columns
                val row = index / state.columns
                val target = IntOffset(column * (cellPx + gapPx), row * (cellPx + gapPx))
                val position by animateIntOffsetAsState(target, spring(dampingRatio = 0.78f, stiffness = 260f), label = "slot")
                BoardCard(
                    card = card,
                    number = index + 1,
                    faceVisible = card.isFaceUp || card.isMatched || state.peeking,
                    hinted = index in state.hints,
                    mismatched = index in mismatched,
                    theme = cardTheme,
                    back = cardBack,
                    corner = corner,
                    enabled = !state.inputLocked,
                    dealDelay = index * GameViewModel.DEAL_STEP_MILLIS + 120L,
                    duel = state.config.players > 1,
                    onClick = { onSelect(index) },
                    modifier = Modifier
                        .offset { position }
                        .size(cell),
                )
            }
        }
    }
}

@Composable
private fun BoardCard(
    card: MemoryCard,
    number: Int,
    faceVisible: Boolean,
    hinted: Boolean,
    mismatched: Boolean,
    theme: CardTheme,
    back: CardBack,
    corner: Dp,
    enabled: Boolean,
    dealDelay: Long,
    duel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalReducedMotion.current
    val gameColors = GameTheme.colors
    val scheme = MaterialTheme.colorScheme
    val rotation by animateFloatAsState(
        targetValue = if (faceVisible) 180f else 0f,
        animationSpec = if (reducedMotion) tween(120) else spring(dampingRatio = 0.72f, stiffness = 480f),
        label = "flip",
    )
    val deal = remember { Animatable(if (reducedMotion) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!reducedMotion) {
            delay(dealDelay)
            deal.animateTo(1f, spring(dampingRatio = 0.68f, stiffness = 320f))
        }
    }
    val pop = remember { Animatable(1f) }
    LaunchedEffect(card.isMatched) {
        if (card.isMatched && !reducedMotion) {
            pop.animateTo(1.16f, tween(110))
            pop.animateTo(0.94f, spring(dampingRatio = 0.4f, stiffness = 320f))
        } else if (card.isMatched) {
            pop.snapTo(0.94f)
        } else {
            pop.snapTo(1f)
        }
    }
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(mismatched) {
        if (mismatched && !reducedMotion) {
            delay(180)
            shake(shakeX, 7f)
        }
    }
    val crack = remember { Animatable(0f) }
    LaunchedEffect(card.ice) {
        if (card.ice == 0) crack.animateTo(1f, tween(260)) else crack.snapTo(0f)
    }
    val matchedAlpha by animateFloatAsState(if (card.isMatched) 0.6f else 1f, tween(500, delayMillis = 350), label = "matchedAlpha")

    val borderColor = when {
        mismatched -> gameColors.danger
        card.isMatched && duel -> if (card.owner == 0) gameColors.playerOne else gameColors.playerTwo
        card.isMatched && card.special == SpecialKind.GOLDEN -> gameColors.gold
        card.isMatched -> gameColors.success.copy(alpha = 0.8f)
        faceVisible && card.special == SpecialKind.GOLDEN -> gameColors.gold
        else -> Color.Transparent
    }
    val description = when {
        card.ice > 0 -> stringResource(R.string.card_frozen, number)
        card.isMatched -> stringResource(R.string.card_matched, number)
        card.isFaceUp -> stringResource(R.string.card_revealed, number)
        else -> stringResource(R.string.card_hidden, number)
    }
    val shape = RoundedCornerShape(corner)
    Box(
        modifier = modifier
            .graphicsLayer {
                val d = deal.value
                val s = pop.value * (0.35f + 0.65f * d)
                scaleX = s
                scaleY = s
                alpha = d.coerceIn(0f, 1f) * matchedAlpha
                translationY = (1f - d) * size.height * 0.9f
                translationX = shakeX.value
                rotationY = rotation
                rotationZ = (1f - d) * 18f * if (number % 2 == 0) 1f else -1f
                cameraDistance = 14f * density
            }
            .shadow(if (card.isMatched) 0.dp else 5.dp, shape, clip = false)
            .clip(shape)
            .pressable(onClick = onClick, enabled = enabled && !card.isMatched && !card.isFaceUp, pressedScale = 0.93f, sound = false)
            .semantics { contentDescription = description },
    ) {
        if (rotation > 90f) {
            Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                CardFace(theme = theme, pairId = card.pairId)
                if (card.special != null) SpecialBadge(card.special, Modifier.align(Alignment.TopEnd))
            }
        } else {
            CardBackArt(back, theme)
            if (card.ice > 0 || crack.value < 1f) IceOverlay(crack.value)
        }
        val shade = abs(sin(rotation * PI / 180)).toFloat() * 0.45f
        if (shade > 0.01f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = shade)))
        if (hinted) {
            HintBorder(shape)
        } else if (borderColor != Color.Transparent) {
            Box(Modifier.fillMaxSize().border(if (mismatched) 3.dp else 2.dp, borderColor, shape))
        }
    }
}

/** Pulsing highlight; kept in its own composable so only hinted cards run an infinite animation. */
@Composable
private fun HintBorder(shape: RoundedCornerShape) {
    val color = MaterialTheme.colorScheme.secondary
    val pulse = rememberInfiniteTransition(label = "hint")
    val glow by pulse.animateFloat(0.3f, 1f, infiniteRepeatable(tween(420), RepeatMode.Reverse), label = "glow")
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = glow }
            .border(3.5.dp, color, shape)
            .background(color.copy(alpha = 0.18f)),
    )
}

@Composable
private fun SpecialBadge(kind: SpecialKind, modifier: Modifier = Modifier) {
    val gameColors = GameTheme.colors
    Box(
        modifier = modifier
            .padding(4.dp)
            .size(20.dp)
            .clip(CircleShape)
            .background(if (kind == SpecialKind.GOLDEN) gameColors.gold else gameColors.ice),
        contentAlignment = Alignment.Center,
    ) {
        GlyphIcon(if (kind == SpecialKind.GOLDEN) Glyph.CROWN else Glyph.HOURGLASS, tint = Color(0xFF1B1730), size = 13.dp, filled = true)
    }
}

@Composable
private fun IceOverlay(crack: Float) {
    val ice = GameTheme.colors.ice
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = 1f - crack }
            .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.85f), ice.copy(alpha = 0.7f), Color.White.copy(alpha = 0.6f)))),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.025f
            val c = center
            listOf(Offset(-0.4f, -0.35f), Offset(0.38f, -0.3f), Offset(0.3f, 0.42f), Offset(-0.35f, 0.3f)).forEach { direction ->
                drawLine(Color.White, c, c + Offset(direction.x * size.width, direction.y * size.height), strokeWidth = stroke)
            }
        }
        GlyphIcon(Glyph.SNOWFLAKE, tint = Color(0xFF2F8BFF), size = 26.dp)
    }
}

@Composable
private fun BannerOverlay(banner: Banner?) {
    val current = banner ?: return
    key(current.id) {
        val progress = remember { Animatable(0f) }
        LaunchedEffect(Unit) { progress.animateTo(1f, tween(1250, easing = LinearEasing)) }
        val p = progress.value
        if (p < 1f) BannerCard(current, p)
    }
}

@Composable
private fun BannerCard(current: Banner, p: Float) {
    val scale = when {
        p < 0.14f -> 0.4f + p / 0.14f * 0.75f
        p < 0.24f -> 1.15f - (p - 0.14f) / 0.1f * 0.15f
        else -> 1f
    }
    val alpha = if (p > 0.78f) 1f - (p - 0.78f) / 0.22f else 1f
    Column(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                rotationZ = -4f
            }
            .shadow(16.dp, RoundedCornerShape(22.dp), spotColor = current.colors.first())
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(current.colors))
            .border(1.5.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            current.title,
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                shadow = Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 3f), 6f),
            ),
        )
        if (current.subtitle != null) {
            Text(current.subtitle, style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.92f))
        }
    }
}

@Composable
private fun BoosterBar(state: GameUiState, onUse: (Booster) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Booster.entries.forEach { booster ->
            val count = state.boosters[booster] ?: 0
            val usable = state.phase == GamePhase.RUNNING && !state.peeking && count > 0 &&
                (booster != Booster.FREEZE || (state.remainingMillis != null && state.frozenMillis == 0L)) &&
                (booster != Booster.HINT || state.hints.isEmpty())
            BoosterButton(booster, count, usable, Modifier.weight(1f)) { onUse(booster) }
        }
    }
}

@Composable
private fun RowScope.BoosterButton(booster: Booster, count: Int, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .graphicsLayer { alpha = if (enabled) 1f else 0.45f }
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.surfaceContainer.copy(alpha = 0.9f))
            .border(1.dp, booster.gradient().first().copy(alpha = if (enabled) 0.6f else 0.15f), RoundedCornerShape(18.dp))
            .pressable(onClick = onClick, enabled = enabled, sound = false)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Brush.linearGradient(booster.gradient())),
            contentAlignment = Alignment.Center,
        ) {
            GlyphIcon(booster.glyph(), tint = Color.White, size = 20.dp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(booster.titleRes()), style = MaterialTheme.typography.labelMedium, maxLines = 1)
            Text("×$count", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ResultOverlay(
    state: GameUiState,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    onNextLevel: (Int) -> Unit,
) {
    val outcome = state.outcome
    AnimatedVisibility(
        visible = state.isFinished && outcome != null,
        enter = fadeIn(tween(350, delayMillis = 450)) + slideInVertically(tween(450, delayMillis = 450)) { it / 4 },
        exit = fadeOut() + slideOutVertically { it / 4 },
    ) {
        if (outcome != null) {
            ResultContent(state, onRestart, onExit, onNextLevel)
        }
    }
}

@Composable
private fun ResultContent(
    state: GameUiState,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    onNextLevel: (Int) -> Unit,
) {
    val outcome = state.outcome ?: return
    val scheme = MaterialTheme.colorScheme
    val gameColors = GameTheme.colors
    val config = state.config
    val names = listOf(stringResource(R.string.player_1), stringResource(R.string.player_2))
    val title = when {
        config.type == GameType.DUEL -> if (outcome.winner == -1) stringResource(R.string.result_duel_draw)
        else stringResource(R.string.result_duel_winner, names[outcome.winner ?: 0])
        config.type == GameType.SURVIVAL -> stringResource(R.string.result_survival_over)
        outcome.won -> stringResource(R.string.result_win)
        else -> stringResource(R.string.result_lose)
    }
    val message = when {
        config.type == GameType.DUEL -> stringResource(R.string.result_duel_score, state.playerPairs.getOrElse(0) { 0 }, state.playerPairs.getOrElse(1) { 0 })
        config.type == GameType.SURVIVAL -> stringResource(R.string.result_survival_message, outcome.roundsCleared)
        outcome.won -> null
        else -> stringResource(R.string.result_lose_message)
    }
    val animatedTotal by animateIntAsState(
        targetValue = outcome.breakdown.total,
        animationSpec = tween(1200, delayMillis = 700),
        label = "total",
    )
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        // On 16:9 phones the whole card, buttons included, should fit without scrolling.
        val compact = maxHeight < 760.dp
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = if (compact) 10.dp else 18.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(scheme.surfaceContainerHigh)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(32.dp))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = if (compact) 16.dp else 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val headerGlyph = when {
                config.type == GameType.DUEL -> Glyph.CROWN
                outcome.won -> Glyph.TROPHY
                config.type == GameType.SURVIVAL -> Glyph.HEART
                else -> Glyph.HOURGLASS
            }
            val headerColors = if (outcome.won || config.type == GameType.DUEL) listOf(gameColors.gold, Color(0xFFE5861C))
            else listOf(scheme.primary, scheme.tertiary)
            Box(
                modifier = Modifier
                    .size(if (compact) 56.dp else 72.dp)
                    .shadow(20.dp, CircleShape, spotColor = headerColors.first())
                    .clip(CircleShape)
                    .background(Brush.linearGradient(headerColors)),
                contentAlignment = Alignment.Center,
            ) {
                GlyphIcon(headerGlyph, tint = Color.White, size = if (compact) 30.dp else 38.dp, filled = true)
            }
            Spacer(Modifier.height(if (compact) 8.dp else 12.dp))
            Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            if (message != null) {
                Spacer(Modifier.height(4.dp))
                Text(message, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            if (config.type != GameType.DUEL && outcome.won) {
                Spacer(Modifier.height(if (compact) 8.dp else 14.dp))
                AnimatedStars(outcome.stars)
            }
            if (config.type != GameType.DUEL) {
                Spacer(Modifier.height(if (compact) 6.dp else 14.dp))
                Text(
                    animatedTotal.formatThousands(),
                    style = (if (compact) MaterialTheme.typography.displaySmall else MaterialTheme.typography.displayMedium).copy(
                        brush = Brush.horizontalGradient(listOf(scheme.primary, scheme.tertiary)),
                    ),
                )
                if (outcome.isNewBest) {
                    Pill(stringResource(R.string.new_record), glyph = Glyph.CROWN, color = gameColors.gold, contentColor = Color(0xFF2A1B00))
                }
                Spacer(Modifier.height(if (compact) 6.dp else 12.dp))
                BreakdownRow(stringResource(R.string.breakdown_base), outcome.breakdown.base)
                if (outcome.breakdown.timeBonus > 0) BreakdownRow(stringResource(R.string.breakdown_time), outcome.breakdown.timeBonus)
                if (outcome.breakdown.perfectBonus > 0) BreakdownRow(stringResource(R.string.breakdown_perfect), outcome.breakdown.perfectBonus)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    ResultStat(stringResource(R.string.moves), outcome.moves.toString(), Modifier.weight(1f))
                    ResultStat(stringResource(R.string.stat_slips), outcome.slips.toString(), Modifier.weight(1f))
                    ResultStat(stringResource(R.string.stat_best_combo), "${outcome.bestCombo}×", Modifier.weight(1f))
                    ResultStat(stringResource(R.string.time), outcome.elapsedSeconds.formatClock(), Modifier.weight(1f))
                }
            }
            RewardsSection(outcome)
            Spacer(Modifier.height(if (compact) 12.dp else 18.dp))
            val next = outcome.nextLevel
            if (next != null) {
                GlowButton(stringResource(R.string.next_level), { onNextLevel(next) }, Modifier.fillMaxWidth(), glyph = Glyph.NEXT)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SoftButton(stringResource(R.string.restart), onRestart, Modifier.weight(1f), glyph = Glyph.REPLAY)
                    SoftButton(stringResource(R.string.home), onExit, Modifier.weight(1f), glyph = Glyph.HOME)
                }
            } else {
                GlowButton(stringResource(R.string.restart), onRestart, Modifier.fillMaxWidth(), glyph = Glyph.REPLAY)
                Spacer(Modifier.height(10.dp))
                SoftButton(stringResource(R.string.home), onExit, Modifier.fillMaxWidth(), glyph = Glyph.HOME)
            }
        }
    }
}

@Composable
private fun AnimatedStars(stars: Int) {
    val gold = GameTheme.colors.gold
    val feedback = LocalFeedback.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
        repeat(3) { index ->
            val scale = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                delay(650L + index * 280L)
                if (index < stars) feedback.play(Sfx.MATCH, rate = 1f + index * 0.12f, volume = 0.6f)
                scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = 380f))
            }
            val earned = index < stars
            GlyphIcon(
                Glyph.STAR,
                tint = if (earned) gold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                size = if (index == 1) 52.dp else 42.dp,
                filled = earned,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = (1f - scale.value) * 90f
                },
            )
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("+${value.formatThousands()}", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ResultStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun RewardsSection(outcome: GameOutcome) {
    val reward = outcome.reward
    val gameColors = GameTheme.colors
    val scheme = MaterialTheme.colorScheme
    Spacer(Modifier.height(14.dp))
    if (reward == null) {
        Text(stringResource(R.string.saving), style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Pill("+${reward.totalCoins}", glyph = Glyph.COIN, color = gameColors.gold.copy(alpha = 0.2f), contentColor = scheme.onSurface, glyphTint = gameColors.gold)
        Pill("+${reward.xp} XP", glyph = Glyph.SPARK, color = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer)
        if (reward.dailyFirstClear) {
            Pill(stringResource(R.string.streak_format, reward.dailyStreak), glyph = Glyph.FLAME, color = scheme.tertiaryContainer, contentColor = scheme.onTertiaryContainer)
        }
    }
    if (reward.leveledUp) {
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.level_up_format, reward.levelAfter),
            style = MaterialTheme.typography.titleSmall,
            color = scheme.secondary,
            textAlign = TextAlign.Center,
        )
    }
    reward.achievements.forEach { achievement ->
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(gameColors.gold.copy(alpha = 0.25f), Color.Transparent)))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GlyphMedallion(achievement.glyph(), listOf(gameColors.gold, Color(0xFFE5861C)), size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.achievement_unlocked), style = MaterialTheme.typography.labelSmall, color = gameColors.gold)
                Text(stringResource(achievement.titleRes()), style = MaterialTheme.typography.titleSmall)
            }
            Pill("+${achievement.reward}", glyph = Glyph.COIN, color = scheme.surfaceContainer, contentColor = scheme.onSurface, glyphTint = gameColors.gold)
        }
    }
}

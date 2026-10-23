package com.kadirsahin.hafizaoyunu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.data.Leaderboard
import com.kadirsahin.hafizaoyunu.data.ScoreEntry
import com.kadirsahin.hafizaoyunu.domain.Campaign
import com.kadirsahin.hafizaoyunu.domain.DayKey
import com.kadirsahin.hafizaoyunu.domain.GameConfig
import com.kadirsahin.hafizaoyunu.domain.GameMode
import com.kadirsahin.hafizaoyunu.domain.GameType
import com.kadirsahin.hafizaoyunu.domain.Profile
import com.kadirsahin.hafizaoyunu.ui.components.AnimatedBackdrop
import com.kadirsahin.hafizaoyunu.ui.components.GameDialog
import com.kadirsahin.hafizaoyunu.ui.components.GameTopBar
import com.kadirsahin.hafizaoyunu.ui.components.Glyph
import com.kadirsahin.hafizaoyunu.ui.components.GlyphIcon
import com.kadirsahin.hafizaoyunu.ui.components.GlyphMedallion
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.components.Pill
import com.kadirsahin.hafizaoyunu.ui.components.descriptionRes
import com.kadirsahin.hafizaoyunu.ui.components.formatThousands
import com.kadirsahin.hafizaoyunu.ui.components.glyph
import com.kadirsahin.hafizaoyunu.ui.components.gradient
import com.kadirsahin.hafizaoyunu.ui.components.pressable
import com.kadirsahin.hafizaoyunu.ui.components.titleRes
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme

@Composable
fun ModesScreen(
    profile: Profile,
    scores: List<ScoreEntry>,
    onBack: () -> Unit,
    onCampaign: () -> Unit,
    onStart: (GameConfig) -> Unit,
) {
    var boardPickerFor by rememberSaveable { mutableStateOf<GameType?>(null) }
    val today = DayKey.today()
    AnimatedBackdrop(reducedMotion = LocalReducedMotion.current) {
        Column(Modifier.fillMaxSize()) {
            GameTopBar(stringResource(R.string.modes_title), onBack, subtitle = stringResource(R.string.modes_subtitle))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 320.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp, top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(MODE_ORDER) { type ->
                    val badge = when (type) {
                        GameType.CAMPAIGN -> stringResource(R.string.campaign_progress, profile.totalStars, Campaign.maxStars)
                        GameType.DAILY -> if (profile.dailyLastDay == today) stringResource(R.string.daily_done_short)
                        else stringResource(R.string.new_badge)
                        GameType.SURVIVAL -> stringResource(R.string.survival_best_format, profile.survivalBest)
                        GameType.CLASSIC -> scores.filter { it.board == Leaderboard.MASTER || it.board == Leaderboard.EASY || it.board == Leaderboard.FOCUS }
                            .maxOfOrNull { it.points }
                            ?.let { stringResource(R.string.best_format, it.formatThousands()) }
                        GameType.ZEN -> null
                        GameType.DUEL -> stringResource(R.string.two_players)
                    }
                    ModeCard(type, badge) {
                        when (type) {
                            GameType.CAMPAIGN -> onCampaign()
                            GameType.DAILY -> onStart(GameConfig.daily())
                            GameType.SURVIVAL -> onStart(GameConfig.survival())
                            else -> boardPickerFor = type
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { Spacer(Modifier.height(4.dp)) }
            }
        }
        BoardPicker(
            type = boardPickerFor,
            scores = scores,
            onDismiss = { boardPickerFor = null },
            onPick = { type, mode ->
                boardPickerFor = null
                onStart(
                    when (type) {
                        GameType.ZEN -> GameConfig.zen(mode)
                        GameType.DUEL -> GameConfig.duel(mode)
                        else -> GameConfig.classic(mode)
                    },
                )
            },
        )
    }
}

private val MODE_ORDER = listOf(GameType.CAMPAIGN, GameType.CLASSIC, GameType.DAILY, GameType.SURVIVAL, GameType.ZEN, GameType.DUEL)

@Composable
private fun ModeCard(type: GameType, badge: String?, onClick: () -> Unit) {
    val colors = type.gradient()
    val shape = RoundedCornerShape(26.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(colors.first().copy(alpha = 0.32f), MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f))))
            .border(1.dp, colors.first().copy(alpha = 0.45f), shape)
            .pressable(onClick = onClick, pressedScale = 0.97f)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GlyphMedallion(type.glyph(), colors, size = 60.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(type.titleRes()), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(type.descriptionRes()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (badge != null) {
                Spacer(Modifier.height(8.dp))
                Pill(badge, color = colors.first().copy(alpha = 0.22f), contentColor = MaterialTheme.colorScheme.onSurface)
            }
        }
        GlyphIcon(Glyph.NEXT, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 20.dp)
    }
}

@Composable
private fun BoardPicker(
    type: GameType?,
    scores: List<ScoreEntry>,
    onDismiss: () -> Unit,
    onPick: (GameType, GameMode) -> Unit,
) {
    GameDialog(visible = type != null, onDismiss = onDismiss) {
        val current = type ?: GameType.CLASSIC
        GlyphMedallion(current.glyph(), current.gradient(), size = 56.dp)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(current.titleRes()), style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.choose_board), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        GameMode.entries.forEach { mode ->
            val board = when (mode) {
                GameMode.EASY -> Leaderboard.EASY
                GameMode.FOCUS -> Leaderboard.FOCUS
                GameMode.MASTER -> Leaderboard.MASTER
            }
            val best = scores.filter { it.board == board }.maxOfOrNull { it.points }
            BoardOption(
                mode = mode,
                showTime = current == GameType.CLASSIC,
                best = if (current == GameType.CLASSIC) best else null,
                onClick = { onPick(current, mode) },
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun BoardOption(mode: GameMode, showTime: Boolean, best: Int?, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainer)
            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .pressable(onClick = onClick, pressedScale = 0.97f)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        MiniGrid(mode.columns, mode.rows)
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(mode.titleRes()), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${mode.columns}×${mode.rows} · " + stringResource(R.string.pairs_format, mode.pairCount) +
                        if (showTime) " · " + stringResource(R.string.seconds_short, mode.durationSeconds) else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
        }
        if (best != null) {
            Pill(best.formatThousands(), glyph = Glyph.TROPHY, color = GameTheme.colors.gold.copy(alpha = 0.18f), contentColor = scheme.onSurface, glyphTint = GameTheme.colors.gold)
        }
    }
}

@Composable
private fun MiniGrid(columns: Int, rows: Int) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(scheme.primary.copy(alpha = 0.12f))
            .padding(5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.5.dp)) {
            repeat(rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                    repeat(columns) {
                        Box(
                            Modifier
                                .size((32 / maxOf(columns, rows)).dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(Brush.linearGradient(listOf(scheme.primary, scheme.tertiary))),
                        )
                    }
                }
            }
        }
    }
}

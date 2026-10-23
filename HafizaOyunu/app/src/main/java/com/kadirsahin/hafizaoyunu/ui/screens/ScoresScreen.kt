package com.kadirsahin.hafizaoyunu.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.data.Leaderboard
import com.kadirsahin.hafizaoyunu.data.ScoreEntry
import com.kadirsahin.hafizaoyunu.ui.components.AnimatedBackdrop
import com.kadirsahin.hafizaoyunu.ui.components.GameDialog
import com.kadirsahin.hafizaoyunu.ui.components.GameTopBar
import com.kadirsahin.hafizaoyunu.ui.components.GlowButton
import com.kadirsahin.hafizaoyunu.ui.components.Glyph
import com.kadirsahin.hafizaoyunu.ui.components.GlyphIcon
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.components.SoftButton
import com.kadirsahin.hafizaoyunu.ui.components.formatClock
import com.kadirsahin.hafizaoyunu.ui.components.formatThousands
import com.kadirsahin.hafizaoyunu.ui.components.pressable
import com.kadirsahin.hafizaoyunu.ui.components.titleRes
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme
import java.util.Date

@Composable
fun ScoresScreen(scores: List<ScoreEntry>, onBack: () -> Unit, onClearScores: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf(Leaderboard.EASY) }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    val filtered = scores.filter { it.board == selected }
        .sortedWith(compareByDescending<ScoreEntry> { it.points }.thenBy { it.durationSeconds })
        .take(50)

    AnimatedBackdrop(reducedMotion = LocalReducedMotion.current) {
        Column(Modifier.fillMaxSize()) {
            GameTopBar(stringResource(R.string.scores_title), onBack, subtitle = stringResource(R.string.scores_subtitle))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Leaderboard.entries.forEach { board ->
                    BoardTab(board, board == selected) { selected = board }
                }
            }
            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(36.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        GlyphIcon(Glyph.TROPHY, tint = MaterialTheme.colorScheme.primary, size = 56.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.empty_scores_title), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        Text(
                            stringResource(R.string.empty_scores_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item { Podium(filtered.take(3)) }
                    itemsIndexed(filtered.drop(3), key = { _, entry -> entry.id }) { index, entry ->
                        ScoreRow(rank = index + 4, entry = entry)
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        SoftButton(
                            stringResource(R.string.clear_scores),
                            { showClearDialog = true },
                            Modifier.fillMaxWidth(),
                            contentColor = GameTheme.colors.danger,
                        )
                    }
                }
            }
        }
        GameDialog(visible = showClearDialog, onDismiss = { showClearDialog = false }) {
            GlyphIcon(Glyph.INFO, tint = GameTheme.colors.danger, size = 40.dp)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.clear_scores_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Text(stringResource(R.string.clear_scores_message), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            GlowButton(
                stringResource(R.string.clear),
                {
                    onClearScores()
                    showClearDialog = false
                },
                Modifier.fillMaxWidth(),
                colors = listOf(GameTheme.colors.danger, Color(0xFF9B1C5C)),
            )
            Spacer(Modifier.height(10.dp))
            SoftButton(stringResource(R.string.cancel), { showClearDialog = false }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun BoardTab(board: Leaderboard, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val label = stringResource(board.titleRes()) + if (board.key.contains('x')) " · ${board.key}" else ""
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) scheme.onPrimary else scheme.onSurface,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (selected) Brush.horizontalGradient(listOf(scheme.primary, scheme.tertiary)) else Brush.horizontalGradient(listOf(scheme.surfaceContainerHigh, scheme.surfaceContainerHigh)))
            .pressable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun Podium(top: List<ScoreEntry>) {
    val gameColors = GameTheme.colors
    val medal = listOf(gameColors.gold, Color(0xFFC9D1E0), Color(0xFFD9915A))
    val order = listOf(1, 0, 2).filter { it < top.size }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
    ) {
        order.forEach { rank ->
            val entry = top[rank]
            val height: Dp = when (rank) {
                0 -> 132.dp
                1 -> 104.dp
                else -> 88.dp
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                GlyphIcon(if (rank == 0) Glyph.CROWN else Glyph.MEDAL, tint = medal[rank], size = if (rank == 0) 34.dp else 26.dp, filled = true)
                Spacer(Modifier.height(4.dp))
                Text(entry.points.formatThousands(), style = MaterialTheme.typography.titleMedium)
                Text(entry.durationSeconds.formatClock(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                        .background(Brush.verticalGradient(listOf(medal[rank].copy(alpha = 0.75f), medal[rank].copy(alpha = 0.15f))))
                        .border(1.dp, medal[rank].copy(alpha = 0.6f), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 8.dp, bottomEnd = 8.dp)),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Text("${rank + 1}", style = MaterialTheme.typography.displaySmall, color = Color.White, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun ScoreRow(rank: Int, entry: ScoreEntry) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val date = if (entry.createdAt > 0) DateFormat.getMediumDateFormat(context).format(Date(entry.createdAt)) else null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(scheme.surfaceContainer.copy(alpha = 0.9f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(scheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(rank.toString(), style = MaterialTheme.typography.labelLarge, color = scheme.primary)
        }
        Column(Modifier.weight(1f)) {
            Text(entry.points.formatThousands(), style = MaterialTheme.typography.titleMedium)
            val details = buildList {
                add(entry.durationSeconds.formatClock())
                if (entry.moves > 0) add(stringResource(R.string.moves_format, entry.moves))
                if (date != null) add(date)
            }
            Text(details.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
    }
}

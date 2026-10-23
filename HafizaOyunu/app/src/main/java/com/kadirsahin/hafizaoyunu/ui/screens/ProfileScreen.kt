package com.kadirsahin.hafizaoyunu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.domain.Achievement
import com.kadirsahin.hafizaoyunu.domain.Profile
import com.kadirsahin.hafizaoyunu.domain.progress
import com.kadirsahin.hafizaoyunu.ui.components.AnimatedBackdrop
import com.kadirsahin.hafizaoyunu.ui.components.GameTopBar
import com.kadirsahin.hafizaoyunu.ui.components.Glyph
import com.kadirsahin.hafizaoyunu.ui.components.GlyphIcon
import com.kadirsahin.hafizaoyunu.ui.components.GlyphMedallion
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.components.Pill
import com.kadirsahin.hafizaoyunu.ui.components.ProgressBar
import com.kadirsahin.hafizaoyunu.ui.components.SectionHeader
import com.kadirsahin.hafizaoyunu.ui.components.descriptionRes
import com.kadirsahin.hafizaoyunu.ui.components.formatThousands
import com.kadirsahin.hafizaoyunu.ui.components.glyph
import com.kadirsahin.hafizaoyunu.ui.components.rankTitleRes
import com.kadirsahin.hafizaoyunu.ui.components.titleRes
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(profile: Profile, onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val info = profile.levelInfo
    val hours = profile.playSeconds / 3600
    val minutes = (profile.playSeconds % 3600) / 60
    val stats = listOf(
        Triple(Glyph.CARDS, stringResource(R.string.stat_games), profile.gamesPlayed.formatThousands()),
        Triple(Glyph.TROPHY, stringResource(R.string.stat_wins), "${(profile.winRate * 100).roundToInt()}%"),
        Triple(Glyph.EYE, stringResource(R.string.stat_accuracy), "${(profile.accuracy * 100).roundToInt()}%"),
        Triple(Glyph.FLAME, stringResource(R.string.stat_best_combo), "${profile.bestCombo}×"),
        Triple(Glyph.CHECK, stringResource(R.string.stat_matches), profile.totalMatches.formatThousands()),
        Triple(Glyph.CLOCK, stringResource(R.string.stat_playtime), stringResource(R.string.duration_hm, hours.toInt(), minutes.toInt())),
        Triple(Glyph.CALENDAR, stringResource(R.string.stat_streak), profile.dailyBestStreak.toString()),
        Triple(Glyph.HEART, stringResource(R.string.stat_survival), profile.survivalBest.toString()),
        Triple(Glyph.STAR, stringResource(R.string.stat_stars), profile.totalStars.toString()),
    )
    AnimatedBackdrop(reducedMotion = LocalReducedMotion.current) {
        Column(Modifier.fillMaxSize()) {
            GameTopBar(stringResource(R.string.profile_title), onBack)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 104.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Brush.linearGradient(listOf(scheme.primary.copy(alpha = 0.45f), scheme.tertiary.copy(alpha = 0.3f))))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(26.dp))
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        LevelRing(info.level, info.progress, 76.dp)
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(rankTitleRes(info.level)), style = MaterialTheme.typography.headlineSmall)
                            Text(stringResource(R.string.level_format, info.level), style = MaterialTheme.typography.labelLarge, color = scheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            ProgressBar(info.progress, Modifier.fillMaxWidth(), listOf(scheme.secondary, scheme.primary))
                            Spacer(Modifier.height(4.dp))
                            Text(stringResource(R.string.xp_format, info.xpIntoLevel, info.xpForNext), style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                        }
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader(stringResource(R.string.stats_title), Modifier.padding(top = 8.dp))
                }
                items(stats) { (glyph, label, value) ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(scheme.surfaceContainer.copy(alpha = 0.9f))
                            .padding(vertical = 14.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        GlyphIcon(glyph, tint = scheme.primary, size = 22.dp, filled = true)
                        Spacer(Modifier.height(6.dp))
                        Text(value, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                        Text(label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant, maxLines = 1, textAlign = TextAlign.Center)
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader(
                        stringResource(R.string.achievements_title),
                        Modifier.padding(top = 8.dp),
                        subtitle = stringResource(R.string.achievements_count, profile.achievements.size, Achievement.entries.size),
                    )
                }
                val sorted = Achievement.entries.sortedByDescending { it in profile.achievements }
                items(sorted, span = { GridItemSpan(maxLineSpan) }) { achievement ->
                    AchievementRow(achievement, profile)
                }
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement, profile: Profile) {
    val scheme = MaterialTheme.colorScheme
    val gameColors = GameTheme.colors
    val unlocked = achievement in profile.achievements
    val progress = achievement.progress(profile)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (unlocked) Brush.horizontalGradient(listOf(gameColors.gold.copy(alpha = 0.22f), scheme.surfaceContainer.copy(alpha = 0.9f)))
                else Brush.horizontalGradient(listOf(scheme.surfaceContainer.copy(alpha = 0.9f), scheme.surfaceContainer.copy(alpha = 0.9f))),
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlyphMedallion(
            achievement.glyph(),
            if (unlocked) listOf(gameColors.gold, Color(0xFFE5861C)) else listOf(scheme.surfaceContainerHighest, scheme.surfaceContainerHigh),
            size = 46.dp,
            tint = if (unlocked) Color.White else scheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer { alpha = if (unlocked) 1f else 0.7f },
        )
        Column(Modifier.weight(1f)) {
            Text(stringResource(achievement.titleRes()), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(achievement.descriptionRes()), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            if (!unlocked && achievement.goal > 1) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProgressBar(progress / achievement.goal.toFloat(), Modifier.weight(1f), listOf(scheme.secondary, scheme.primary), height = 6.dp)
                    Text("$progress/${achievement.goal}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                }
            }
        }
        if (unlocked) {
            GlyphIcon(Glyph.CHECK, tint = gameColors.success)
        } else {
            Pill("+${achievement.reward}", glyph = Glyph.COIN, color = scheme.surfaceContainerHigh, contentColor = scheme.onSurface, glyphTint = gameColors.gold)
        }
    }
}

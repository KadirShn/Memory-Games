package com.kadirsahin.hafizaoyunu.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.domain.Campaign
import com.kadirsahin.hafizaoyunu.domain.GameConfig
import com.kadirsahin.hafizaoyunu.domain.LevelSpec
import com.kadirsahin.hafizaoyunu.domain.Profile
import com.kadirsahin.hafizaoyunu.ui.components.AnimatedBackdrop
import com.kadirsahin.hafizaoyunu.ui.components.GameDialog
import com.kadirsahin.hafizaoyunu.ui.components.GameTopBar
import com.kadirsahin.hafizaoyunu.ui.components.GlowButton
import com.kadirsahin.hafizaoyunu.ui.components.Glyph
import com.kadirsahin.hafizaoyunu.ui.components.GlyphIcon
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.components.Pill
import com.kadirsahin.hafizaoyunu.ui.components.StarRow
import com.kadirsahin.hafizaoyunu.ui.components.chapterGradient
import com.kadirsahin.hafizaoyunu.ui.components.chapterTitleRes
import com.kadirsahin.hafizaoyunu.ui.components.descriptionRes
import com.kadirsahin.hafizaoyunu.ui.components.formatClock
import com.kadirsahin.hafizaoyunu.ui.components.glyph
import com.kadirsahin.hafizaoyunu.ui.components.pressable
import com.kadirsahin.hafizaoyunu.ui.components.uppercaseForUi
import com.kadirsahin.hafizaoyunu.ui.components.titleRes
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme

private val NODE_PATTERN = listOf(0.5f, 0.78f, 0.5f, 0.22f, 0.5f, 0.78f)
private val NODE_SPACING = 104.dp

@Composable
fun CampaignScreen(profile: Profile, onBack: () -> Unit, onStart: (GameConfig) -> Unit) {
    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    val stars = profile.campaignStars
    val current = Campaign.levels.firstOrNull { Campaign.isUnlocked(it.number, stars) && (stars[it.number] ?: 0) == 0 }?.number
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
        val chapter = ((current ?: 1) - 1) / Campaign.LEVELS_PER_CHAPTER
        if (chapter > 0) listState.scrollToItem(chapter)
    }
    AnimatedBackdrop(reducedMotion = LocalReducedMotion.current) {
        Column(Modifier.fillMaxSize()) {
            GameTopBar(
                title = stringResource(R.string.campaign_title),
                onBack = onBack,
                subtitle = stringResource(R.string.campaign_progress, profile.totalStars, Campaign.maxStars),
                trailing = {
                    Pill(
                        "${profile.totalStars}",
                        glyph = Glyph.STAR,
                        color = GameTheme.colors.gold.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        glyphTint = GameTheme.colors.gold,
                    )
                },
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items((1..Campaign.CHAPTER_COUNT).toList()) { chapter ->
                    ChapterSection(
                        chapter = chapter,
                        levels = Campaign.levels.filter { it.chapter == chapter },
                        stars = stars,
                        current = current,
                        onLevel = { selected = it },
                    )
                }
            }
        }
        val level = selected?.let { Campaign.level(it) }
        LevelDialog(
            level = level,
            stars = level?.let { stars[it.number] } ?: 0,
            unlocked = level?.let { Campaign.isUnlocked(it.number, stars) } ?: false,
            onDismiss = { selected = null },
            onStart = {
                selected = null
                if (level != null) onStart(level.config())
            },
        )
    }
}

@Composable
private fun ChapterSection(
    chapter: Int,
    levels: List<LevelSpec>,
    stars: Map<Int, Int>,
    current: Int?,
    onLevel: (Int) -> Unit,
) {
    val colors = chapterGradient(chapter)
    val earned = levels.sumOf { stars[it.number] ?: 0 }
    val unlocked = Campaign.isUnlocked(levels.first().number, stars)
    Column(modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth().padding(top = 14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.horizontalGradient(colors))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.chapter_format, chapter).uppercaseForUi(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                )
                Text(stringResource(chapterTitleRes(chapter)), style = MaterialTheme.typography.headlineSmall, color = Color.White)
            }
            if (unlocked) {
                Pill("$earned / ${levels.size * 3}", glyph = Glyph.STAR, color = Color.Black.copy(alpha = 0.25f), contentColor = Color.White, glyphTint = GameTheme.colors.gold)
            } else {
                GlyphIcon(Glyph.LOCK, tint = Color.White, size = 26.dp)
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(NODE_SPACING * levels.size),
        ) {
            val width = maxWidth
            val lineColor = colors.first().copy(alpha = 0.55f)
            Canvas(Modifier.fillMaxSize()) {
                val points = levels.indices.map { i ->
                    Offset(size.width * NODE_PATTERN[i], NODE_SPACING.toPx() * (i + 0.5f))
                }
                points.zipWithNext().forEach { (a, b) ->
                    drawLine(
                        lineColor,
                        a,
                        b,
                        strokeWidth = 6.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 14.dp.toPx())),
                    )
                }
            }
            levels.forEachIndexed { i, level ->
                val nodeSize = 76.dp
                LevelNode(
                    level = level,
                    stars = stars[level.number] ?: 0,
                    unlocked = Campaign.isUnlocked(level.number, stars),
                    isCurrent = level.number == current,
                    colors = colors,
                    onClick = { onLevel(level.number) },
                    modifier = Modifier.offset(
                        x = width * NODE_PATTERN[i] - nodeSize / 2,
                        y = NODE_SPACING * i + (NODE_SPACING - nodeSize) / 2 - 8.dp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun LevelNode(
    level: LevelSpec,
    stars: Int,
    unlocked: Boolean,
    isCurrent: Boolean,
    colors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val reducedMotion = LocalReducedMotion.current
    val pulse = rememberInfiniteTransition(label = "node")
    val ring by pulse.animateFloat(1f, 1.25f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "ring")
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            if (isCurrent) {
                Box(
                    Modifier
                        .size(76.dp)
                        .graphicsLayer {
                            val s = if (reducedMotion) 1.1f else ring
                            scaleX = s
                            scaleY = s
                            alpha = if (reducedMotion) 0.5f else 1.3f - ring
                        }
                        .clip(CircleShape)
                        .background(colors.first().copy(alpha = 0.5f)),
                )
            }
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .shadow(if (unlocked) 12.dp else 0.dp, CircleShape, spotColor = colors.first())
                    .clip(CircleShape)
                    .background(
                        if (unlocked) Brush.linearGradient(colors)
                        else Brush.linearGradient(listOf(scheme.surfaceContainerHighest, scheme.surfaceContainerHigh)),
                    )
                    .border(3.dp, Color.White.copy(alpha = if (unlocked) 0.5f else 0.1f), CircleShape)
                    .pressable(onClick = onClick),
                contentAlignment = Alignment.Center,
            ) {
                if (unlocked) {
                    Text(level.number.toString(), style = MaterialTheme.typography.headlineSmall, color = Color.White)
                } else {
                    GlyphIcon(Glyph.LOCK, tint = scheme.onSurfaceVariant, size = 24.dp)
                }
            }
        }
        if (unlocked) {
            Spacer(Modifier.height(2.dp))
            StarRow(stars, size = 15.dp)
        }
    }
}

@Composable
private fun LevelDialog(level: LevelSpec?, stars: Int, unlocked: Boolean, onDismiss: () -> Unit, onStart: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    GameDialog(visible = level != null, onDismiss = onDismiss) {
        if (level == null) return@GameDialog
        val colors = chapterGradient(level.chapter)
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center,
        ) {
            Text(level.number.toString(), style = MaterialTheme.typography.headlineMedium, color = Color.White)
        }
        Spacer(Modifier.height(10.dp))
        Text(stringResource(R.string.level_format, level.number), style = MaterialTheme.typography.headlineSmall)
        Text(
            stringResource(R.string.chapter_format, level.chapter) + " · " + stringResource(chapterTitleRes(level.chapter)),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        StarRow(stars, size = 26.dp, spacing = 6.dp)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill("${level.columns}×${level.rows}", glyph = Glyph.CARDS)
            Pill(stringResource(R.string.pairs_format, level.pairCount), glyph = Glyph.SPARK)
            Pill(level.seconds.formatClock(), glyph = Glyph.CLOCK)
        }
        if (level.twists.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.twists_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth())
            level.twists.forEach { twist ->
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(scheme.surfaceContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    GlyphIcon(twist.glyph(), tint = scheme.secondary, size = 24.dp)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(twist.titleRes()), style = MaterialTheme.typography.labelLarge)
                        Text(stringResource(twist.descriptionRes()), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.star_goals), style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        GoalRow(3, stringResource(R.string.star_goal_moves, level.threeStarMoves))
        GoalRow(2, stringResource(R.string.star_goal_moves, level.twoStarMoves))
        GoalRow(1, stringResource(R.string.star_goal_clear))
        Spacer(Modifier.height(20.dp))
        if (unlocked) {
            GlowButton(stringResource(R.string.start), onStart, Modifier.fillMaxWidth(), glyph = Glyph.PLAY, colors = colors)
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlyphIcon(Glyph.LOCK, tint = scheme.onSurfaceVariant, size = 18.dp)
                Text(stringResource(R.string.level_locked), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun GoalRow(stars: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StarRow(stars, size = 14.dp)
        Text(text, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start)
    }
}

package com.kadirsahin.hafizaoyunu.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.domain.Campaign
import com.kadirsahin.hafizaoyunu.domain.CardBack
import com.kadirsahin.hafizaoyunu.domain.CardTheme
import com.kadirsahin.hafizaoyunu.domain.DayKey
import com.kadirsahin.hafizaoyunu.domain.GameType
import com.kadirsahin.hafizaoyunu.domain.Profile
import com.kadirsahin.hafizaoyunu.ui.components.AnimatedBackdrop
import com.kadirsahin.hafizaoyunu.ui.components.CardBackArt
import com.kadirsahin.hafizaoyunu.ui.components.CardFace
import com.kadirsahin.hafizaoyunu.ui.components.CoinPill
import com.kadirsahin.hafizaoyunu.ui.components.GlassCard
import com.kadirsahin.hafizaoyunu.ui.components.GlowButton
import com.kadirsahin.hafizaoyunu.ui.components.Glyph
import com.kadirsahin.hafizaoyunu.ui.components.GlyphIcon
import com.kadirsahin.hafizaoyunu.ui.components.GlyphMedallion
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.components.Pill
import com.kadirsahin.hafizaoyunu.ui.components.ProgressBar
import com.kadirsahin.hafizaoyunu.ui.components.gradient
import com.kadirsahin.hafizaoyunu.ui.components.pressable
import com.kadirsahin.hafizaoyunu.ui.components.rankTitleRes
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    profile: Profile,
    cardTheme: CardTheme,
    onPlay: () -> Unit,
    onDaily: () -> Unit,
    onCampaign: () -> Unit,
    onScores: () -> Unit,
    onProfile: () -> Unit,
    onShop: () -> Unit,
    onSettings: () -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    val scheme = MaterialTheme.colorScheme
    AnimatedBackdrop(reducedMotion = reducedMotion) {
        // 16:9 phones have roughly 100dp less height; tighten the hero so the whole menu fits without scrolling.
        BoxWithConstraints(Modifier.fillMaxSize()) {
        val compact = maxHeight < 720.dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = if (compact) 8.dp else 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                LevelBadge(profile, onProfile, Modifier.weight(1f))
                Spacer(Modifier.size(10.dp))
                CoinPill(profile.coins, onClick = onShop)
            }
            Spacer(Modifier.height(if (compact) 4.dp else 18.dp))
            HeroFan(cardTheme, profile.selectedBack, compact)
            Spacer(Modifier.height(if (compact) 4.dp else 14.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = (if (compact) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displaySmall).copy(
                    brush = Brush.linearGradient(listOf(scheme.primary, scheme.tertiary, scheme.secondary)),
                ),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.titleSmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(if (compact) 14.dp else 22.dp))
            PlayButton(onPlay, if (compact) 58.dp else 66.dp)
            Spacer(Modifier.height(if (compact) 10.dp else 16.dp))
            Column(modifier = Modifier.widthIn(max = 560.dp), verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)) {
                DailyCard(profile, onDaily)
                CampaignCard(profile, onCampaign)
            }
            Spacer(Modifier.height(if (compact) 10.dp else 18.dp))
            Row(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                NavTile(Glyph.TROPHY, stringResource(R.string.nav_scores), onScores, Modifier.weight(1f), compact)
                NavTile(Glyph.USER, stringResource(R.string.nav_profile), onProfile, Modifier.weight(1f), compact)
                NavTile(Glyph.SHOP, stringResource(R.string.nav_shop), onShop, Modifier.weight(1f), compact)
                NavTile(Glyph.SETTINGS, stringResource(R.string.nav_settings), onSettings, Modifier.weight(1f), compact)
            }
            Spacer(Modifier.height(12.dp))
        }
        }
    }
}

@Composable
private fun LevelBadge(profile: Profile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val info = profile.levelInfo
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(scheme.surfaceContainerHigh.copy(alpha = 0.85f))
            .border(1.dp, Color.White.copy(alpha = if (GameTheme.colors.isDark) 0.08f else 0.5f), RoundedCornerShape(100.dp))
            .pressable(onClick = onClick)
            .padding(start = 5.dp, end = 14.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LevelRing(info.level, info.progress, 42.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(rankTitleRes(info.level)), style = MaterialTheme.typography.labelLarge, maxLines = 1)
            Spacer(Modifier.height(3.dp))
            ProgressBar(info.progress, Modifier.fillMaxWidth(), listOf(scheme.secondary, scheme.primary), height = 6.dp)
        }
    }
}

@Composable
fun LevelRing(level: Int, progress: Float, size: Dp) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = this.size.minDimension * 0.1f
            drawCircle(Brush.linearGradient(listOf(scheme.primary, scheme.tertiary)), this.size.minDimension / 2 - stroke)
            drawArc(Color.White.copy(alpha = 0.18f), -90f, 360f, false, style = Stroke(stroke), topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2), size = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke))
            drawArc(scheme.secondary, -90f, 360f * progress, false, style = Stroke(stroke, cap = StrokeCap.Round), topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2), size = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke))
        }
        Text(level.toString(), style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

/** Three floating cards; the middle one keeps flipping to tease the selected card pack. */
@Composable
private fun HeroFan(theme: CardTheme, back: CardBack, compact: Boolean) {
    val reducedMotion = LocalReducedMotion.current
    val transition = rememberInfiniteTransition(label = "hero")
    val bob by transition.animateFloat(-1f, 1f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "bob")
    var faceIndex by remember { mutableIntStateOf(5) }
    val flip = remember { Animatable(180f) }
    LaunchedEffect(reducedMotion) {
        if (reducedMotion) return@LaunchedEffect
        val faces = listOf(5, 3, 11, 8, 14, 0, 16, 2)
        var i = 0
        while (true) {
            delay(2400)
            flip.animateTo(0f, tween(320))
            i = (i + 1) % faces.size
            faceIndex = faces[i]
            delay(260)
            flip.animateTo(180f, spring(dampingRatio = 0.6f, stiffness = 260f))
        }
    }
    val b = if (reducedMotion) 0f else bob
    val heroScale = if (compact) 0.78f else 1f
    Box(
        modifier = Modifier
            .size(width = 250.dp, height = if (compact) 118.dp else 150.dp)
            .graphicsLayer {
                scaleX = heroScale
                scaleY = heroScale
            },
        contentAlignment = Alignment.Center,
    ) {
        HeroCard(Modifier.graphicsLayer {
            translationX = -84.dp.toPx()
            translationY = 14.dp.toPx() + b * 5.dp.toPx()
            rotationZ = -14f + b * 2f
        }) { CardBackArt(back, theme) }
        HeroCard(Modifier.graphicsLayer {
            translationX = 84.dp.toPx()
            translationY = 14.dp.toPx() - b * 5.dp.toPx()
            rotationZ = 14f - b * 2f
        }) { CardFace(theme, 17) }
        HeroCard(
            Modifier.graphicsLayer {
                translationY = b * -7.dp.toPx()
                rotationY = flip.value
                cameraDistance = 14f * density
                val s = 1.12f
                scaleX = s
                scaleY = s
            },
        ) {
            if (flip.value > 90f) {
                Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) { CardFace(theme, faceIndex) }
            } else {
                CardBackArt(back, theme)
            }
        }
    }
}

@Composable
private fun HeroCard(modifier: Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .size(104.dp)
            .shadow(18.dp, RoundedCornerShape(24.dp), spotColor = MaterialTheme.colorScheme.primary)
            .clip(RoundedCornerShape(24.dp))
            .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
    ) { content() }
}

@Composable
private fun PlayButton(onPlay: () -> Unit, height: Dp) {
    val reducedMotion = LocalReducedMotion.current
    val transition = rememberInfiniteTransition(label = "play")
    val glow by transition.animateFloat(0.96f, 1.03f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "playGlow")
    GlowButton(
        text = stringResource(R.string.play),
        onClick = onPlay,
        glyph = Glyph.PLAY,
        height = height,
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .graphicsLayer {
                val s = if (reducedMotion) 1f else glow
                scaleX = s
                scaleY = s
            },
    )
}

@Composable
private fun DailyCard(profile: Profile, onDaily: () -> Unit) {
    val today = DayKey.today()
    val done = profile.dailyLastDay == today
    val streak = profile.activeDailyStreak(today, DayKey.yesterday())
    val scheme = MaterialTheme.colorScheme
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onDaily, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            GlyphMedallion(Glyph.CALENDAR, GameType.DAILY.gradient(), size = 52.dp, filled = false)
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.daily_challenge), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(if (done) R.string.daily_done else R.string.daily_ready),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            if (streak > 0) {
                Pill(
                    streak.toString(),
                    glyph = Glyph.FLAME,
                    color = GameTheme.colors.danger.copy(alpha = 0.18f),
                    contentColor = scheme.onSurface,
                    glyphTint = Color(0xFFFF7A3D),
                )
            } else if (done) {
                GlyphIcon(Glyph.CHECK, tint = GameTheme.colors.success)
            } else {
                GlyphIcon(Glyph.NEXT, tint = scheme.onSurfaceVariant, size = 20.dp)
            }
        }
    }
}

@Composable
private fun CampaignCard(profile: Profile, onCampaign: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onCampaign, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            GlyphMedallion(Glyph.FLAG, GameType.CAMPAIGN.gradient(), size = 52.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.campaign), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                ProgressBar(
                    profile.totalStars / Campaign.maxStars.toFloat(),
                    Modifier.fillMaxWidth(),
                    listOf(GameTheme.colors.gold, Color(0xFFE5376B)),
                    height = 7.dp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.campaign_progress, profile.totalStars, Campaign.maxStars),
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            GlyphIcon(Glyph.NEXT, tint = scheme.onSurfaceVariant, size = 20.dp)
        }
    }
}

@Composable
private fun NavTile(glyph: Glyph, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainer.copy(alpha = 0.8f))
            .border(1.dp, Color.White.copy(alpha = if (GameTheme.colors.isDark) 0.07f else 0.5f), RoundedCornerShape(20.dp))
            .pressable(onClick = onClick)
            .padding(vertical = if (compact) 8.dp else 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(scheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            GlyphIcon(glyph, tint = scheme.primary, size = 21.dp)
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

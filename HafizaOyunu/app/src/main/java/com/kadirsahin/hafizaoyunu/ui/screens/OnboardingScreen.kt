package com.kadirsahin.hafizaoyunu.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.domain.CardBack
import com.kadirsahin.hafizaoyunu.domain.CardTheme
import com.kadirsahin.hafizaoyunu.ui.components.AnimatedBackdrop
import com.kadirsahin.hafizaoyunu.ui.components.CardBackArt
import com.kadirsahin.hafizaoyunu.ui.components.CardFace
import com.kadirsahin.hafizaoyunu.ui.components.GlowButton
import com.kadirsahin.hafizaoyunu.ui.components.Glyph
import com.kadirsahin.hafizaoyunu.ui.components.GlyphIcon
import com.kadirsahin.hafizaoyunu.ui.components.GlyphMedallion
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.components.pressable
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(cardTheme: CardTheme, onDone: () -> Unit) {
    val pager = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val pages = listOf(
        R.string.onb_title_1 to R.string.onb_body_1,
        R.string.onb_title_2 to R.string.onb_body_2,
        R.string.onb_title_3 to R.string.onb_body_3,
    )
    AnimatedBackdrop(reducedMotion = LocalReducedMotion.current) {
        Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    stringResource(R.string.onb_skip),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .pressable(onClick = onDone)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    when (page) {
                        0 -> FlipIllustration(cardTheme)
                        1 -> SlipIllustration()
                        else -> RewardIllustration()
                    }
                    Spacer(Modifier.height(36.dp))
                    Text(
                        stringResource(pages[page].first),
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(pages[page].second),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 420.dp),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 20.dp)) {
                repeat(3) { index ->
                    val width by animateDpAsState(if (pager.currentPage == index) 28.dp else 8.dp, label = "dot")
                    Box(
                        Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(RoundedCornerShape(50))
                            .background(if (pager.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                    )
                }
            }
            val last = pager.currentPage == 2
            GlowButton(
                text = stringResource(if (last) R.string.onb_start else R.string.onb_next),
                onClick = { if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                glyph = if (last) Glyph.PLAY else Glyph.NEXT,
                modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FlipIllustration(theme: CardTheme) {
    val transition = rememberInfiniteTransition(label = "onbFlip")
    val rotation by transition.animateFloat(0f, 180f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "rot")
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(2) {
            Box(
                Modifier
                    .size(110.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 14f * density
                    }
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            ) {
                if (rotation > 90f) {
                    Box(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) { CardFace(theme, 5) }
                } else {
                    CardBackArt(CardBack.AURORA, theme)
                }
            }
        }
    }
}

@Composable
private fun SlipIllustration() {
    val gameColors = GameTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
        GlyphMedallion(Glyph.EYE, listOf(gameColors.success, Color(0xFF138A72)), size = 96.dp, filled = false)
        GlyphIcon(Glyph.NEXT, tint = MaterialTheme.colorScheme.onSurfaceVariant, size = 28.dp)
        GlyphMedallion(Glyph.HEART, listOf(gameColors.heart, Color(0xFF9B1C5C)), size = 96.dp)
    }
}

@Composable
private fun RewardIllustration() {
    val gameColors = GameTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
        GlyphMedallion(Glyph.COIN, listOf(gameColors.gold, Color(0xFFE5861C)), size = 76.dp)
        GlyphMedallion(Glyph.TROPHY, listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary), size = 108.dp)
        GlyphMedallion(Glyph.STAR, listOf(Color(0xFF22C9E8), Color(0xFF2F6BFF)), size = 76.dp)
    }
}

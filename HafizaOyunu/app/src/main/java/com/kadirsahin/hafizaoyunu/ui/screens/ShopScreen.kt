package com.kadirsahin.hafizaoyunu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.audio.Sfx
import com.kadirsahin.hafizaoyunu.domain.Booster
import com.kadirsahin.hafizaoyunu.domain.CardBack
import com.kadirsahin.hafizaoyunu.domain.CardTheme
import com.kadirsahin.hafizaoyunu.domain.Profile
import com.kadirsahin.hafizaoyunu.ui.components.AnimatedBackdrop
import com.kadirsahin.hafizaoyunu.ui.components.CardBackArt
import com.kadirsahin.hafizaoyunu.ui.components.CardFace
import com.kadirsahin.hafizaoyunu.ui.components.CoinPill
import com.kadirsahin.hafizaoyunu.ui.components.GameTopBar
import com.kadirsahin.hafizaoyunu.ui.components.Glyph
import com.kadirsahin.hafizaoyunu.ui.components.GlyphIcon
import com.kadirsahin.hafizaoyunu.ui.components.GlyphMedallion
import com.kadirsahin.hafizaoyunu.ui.components.LocalFeedback
import com.kadirsahin.hafizaoyunu.ui.components.LocalReducedMotion
import com.kadirsahin.hafizaoyunu.ui.components.Pill
import com.kadirsahin.hafizaoyunu.ui.components.SectionHeader
import com.kadirsahin.hafizaoyunu.ui.components.descriptionRes
import com.kadirsahin.hafizaoyunu.ui.components.glyph
import com.kadirsahin.hafizaoyunu.ui.components.gradient
import com.kadirsahin.hafizaoyunu.ui.components.pressable
import com.kadirsahin.hafizaoyunu.ui.components.titleRes
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme
import kotlinx.coroutines.delay

@Composable
fun ShopScreen(
    profile: Profile,
    cardTheme: CardTheme,
    onBack: () -> Unit,
    onBuyBooster: (Booster, (Boolean) -> Unit) -> Unit,
    onBuyBack: (CardBack, (Boolean) -> Unit) -> Unit,
    onSelectBack: (CardBack) -> Unit,
    onSelectTheme: (CardTheme) -> Unit,
) {
    val feedback = LocalFeedback.current
    var toast by remember { mutableStateOf<Pair<Int, Boolean>?>(null) }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(1600)
            toast = null
        }
    }
    val onResult: (Boolean) -> Unit = { success ->
        if (success) {
            feedback.play(Sfx.COIN)
            feedback.haptic(HapticFeedbackType.Confirm)
        } else {
            feedback.haptic(HapticFeedbackType.Reject)
        }
        toast = (if (success) R.string.purchased else R.string.not_enough_coins) to success
    }
    AnimatedBackdrop(reducedMotion = LocalReducedMotion.current) {
        Column(Modifier.fillMaxSize()) {
            GameTopBar(
                stringResource(R.string.shop_title),
                onBack,
                trailing = { CoinPill(profile.coins) },
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        stringResource(R.string.shop_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader(stringResource(R.string.boosters_title), Modifier.padding(top = 6.dp)) }
                items(Booster.entries, span = { GridItemSpan(maxLineSpan) }) { booster ->
                    BoosterRow(booster, profile.boosterCount(booster), profile.coins >= booster.price) {
                        onBuyBooster(booster, onResult)
                    }
                }
                item(span = { GridItemSpan(maxLineSpan) }) { SectionHeader(stringResource(R.string.card_backs_title), Modifier.padding(top = 8.dp)) }
                items(CardBack.entries) { back ->
                    BackTile(
                        back = back,
                        theme = cardTheme,
                        owned = back in profile.ownedBacks,
                        selected = back == profile.selectedBack,
                        affordable = profile.coins >= back.price,
                        onClick = {
                            if (back in profile.ownedBacks) onSelectBack(back) else onBuyBack(back, onResult)
                        },
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader(stringResource(R.string.card_themes_title), Modifier.padding(top = 8.dp), subtitle = stringResource(R.string.card_themes_description))
                }
                items(CardTheme.entries, span = { GridItemSpan(maxLineSpan) }) { theme ->
                    ThemeRow(theme, theme == cardTheme) { onSelectTheme(theme) }
                }
            }
        }
        val current = toast
        if (current != null) {
            Box(Modifier.fillMaxSize().padding(bottom = 32.dp), contentAlignment = Alignment.BottomCenter) {
                Pill(
                    stringResource(current.first),
                    glyph = if (current.second) Glyph.CHECK else Glyph.COIN,
                    color = if (current.second) GameTheme.colors.success else GameTheme.colors.danger,
                    contentColor = Color.White,
                )
            }
        }
    }
}

@Composable
private fun BoosterRow(booster: Booster, owned: Int, affordable: Boolean, onBuy: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(scheme.surfaceContainer.copy(alpha = 0.9f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlyphMedallion(booster.glyph(), booster.gradient(), size = 50.dp, filled = false)
        Column(Modifier.weight(1f)) {
            Text(stringResource(booster.titleRes()), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(booster.descriptionRes()), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            Text(stringResource(R.string.owned_format, owned), style = MaterialTheme.typography.labelSmall, color = scheme.secondary)
        }
        PriceButton(booster.price, affordable, onBuy)
    }
}

@Composable
private fun PriceButton(price: Int, affordable: Boolean, onClick: () -> Unit) {
    val gold = GameTheme.colors.gold
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (affordable) Brush.horizontalGradient(listOf(gold, Color(0xFFE5861C))) else Brush.horizontalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.3f))))
            .pressable(onClick = onClick, sound = false)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        GlyphIcon(Glyph.COIN, tint = if (affordable) Color(0xFFFFF4C2) else gold, size = 16.dp)
        Text(price.toString(), style = MaterialTheme.typography.labelLarge, color = if (affordable) Color(0xFF3A2400) else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BackTile(back: CardBack, theme: CardTheme, owned: Boolean, selected: Boolean, affordable: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val gameColors = GameTheme.colors
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(scheme.surfaceContainer.copy(alpha = 0.9f))
            .border(2.dp, if (selected) gameColors.success else Color.Transparent, RoundedCornerShape(20.dp))
            .pressable(onClick = onClick, sound = owned)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp)),
        ) {
            CardBackArt(back, theme)
            if (!owned) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)), contentAlignment = Alignment.Center) {
                    GlyphIcon(Glyph.LOCK, tint = Color.White, size = 26.dp)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(stringResource(back.titleRes()), style = MaterialTheme.typography.labelMedium, maxLines = 1, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        when {
            selected -> Pill(stringResource(R.string.equipped), glyph = Glyph.CHECK, color = gameColors.success.copy(alpha = 0.2f), contentColor = scheme.onSurface, glyphTint = gameColors.success)
            owned -> Pill(stringResource(R.string.equip), color = scheme.surfaceContainerHigh, contentColor = scheme.onSurface)
            else -> PriceButton(back.price, affordable, onClick)
        }
    }
}

@Composable
private fun ThemeRow(theme: CardTheme, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val gameColors = GameTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (selected) scheme.primaryContainer.copy(alpha = 0.8f) else scheme.surfaceContainer.copy(alpha = 0.9f))
            .border(2.dp, if (selected) scheme.primary else Color.Transparent, RoundedCornerShape(22.dp))
            .pressable(onClick = onClick, pressedScale = 0.97f)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy((-14).dp)) {
            listOf(2, 9, 5).forEach { pairId ->
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                ) { CardFace(theme, pairId) }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(stringResource(theme.titleRes()), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(theme.descriptionRes()), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        if (selected) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(gameColors.success), contentAlignment = Alignment.Center) {
                GlyphIcon(Glyph.CHECK, tint = Color.White, size = 18.dp)
            }
        } else {
            Pill(stringResource(R.string.free), color = scheme.surfaceContainerHigh, contentColor = scheme.onSurfaceVariant)
        }
    }
}

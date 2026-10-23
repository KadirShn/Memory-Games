package com.kadirsahin.hafizaoyunu.ui.components

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kadirsahin.hafizaoyunu.audio.Sfx
import com.kadirsahin.hafizaoyunu.audio.SoundEngine
import com.kadirsahin.hafizaoyunu.ui.theme.GameTheme
import java.util.Locale

/** Sound and haptics behind one object so every screen gives consistent, setting-aware feedback. */
@Stable
class Feedback(private val sound: SoundEngine?, private val haptics: HapticFeedback?) {
    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    fun play(sfx: Sfx, rate: Float = 1f, volume: Float = 1f) {
        if (soundEnabled) sound?.play(sfx, rate, volume)
    }

    private var lastHapticAt = 0L

    fun haptic(type: HapticFeedbackType) {
        if (!hapticsEnabled) return
        // Overlapping very short vibrations can crash the vibrator service on some Android 14 builds,
        // and they feel muddy anyway, so keep at least a small gap between pulses.
        val now = SystemClock.uptimeMillis()
        if (now - lastHapticAt < MIN_HAPTIC_GAP_MILLIS) return
        lastHapticAt = now
        haptics?.performHapticFeedback(type)
    }

    fun click() {
        play(Sfx.CLICK, volume = 0.7f)
        haptic(HapticFeedbackType.ContextClick)
    }
}

private const val MIN_HAPTIC_GAP_MILLIS = 80L

val LocalFeedback = staticCompositionLocalOf { Feedback(null, null) }
val LocalReducedMotion = staticCompositionLocalOf { false }

/** Scales the element down slightly while pressed — the tactile "button" feel of mobile games. */
@Composable
fun Modifier.pressable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    sound: Boolean = true,
    role: Role = Role.Button,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) pressedScale else 1f, spring(dampingRatio = 0.5f, stiffness = 900f), label = "press")
    val feedback = LocalFeedback.current
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = role) {
            if (sound) feedback.click()
            onClick()
        }
}

@Composable
fun GameTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RoundIconButton(Glyph.BACK, onBack, contentDescription = null)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.invoke(this)
    }
}

@Composable
fun RoundIconButton(
    glyph: Glyph,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    size: Dp = 46.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f),
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(container)
            .border(1.dp, Color.White.copy(alpha = if (GameTheme.colors.isDark) 0.08f else 0.4f), CircleShape)
            .pressable(onClick = onClick, enabled = enabled),
        contentAlignment = Alignment.Center,
    ) {
        GlyphIcon(glyph, tint = tint.copy(alpha = if (enabled) 1f else 0.4f), size = size * 0.46f, contentDescription = contentDescription)
    }
}

/** Saturated enough for white text in both light and dark themes. */
val BrandGradient = listOf(Color(0xFF6A3DE0), Color(0xFFD02E7F))

/** The big, glowing, gradient call to action. */
@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: Glyph? = null,
    enabled: Boolean = true,
    colors: List<Color> = BrandGradient,
    contentColor: Color = Color.White,
    height: Dp = 60.dp,
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .heightIn(min = height)
            .shadow(if (enabled) 18.dp else 0.dp, shape, ambientColor = colors.first(), spotColor = colors.first())
            .clip(shape)
            .background(Brush.horizontalGradient(if (enabled) colors else colors.map { it.copy(alpha = 0.35f) }))
            .border(1.dp, Color.White.copy(alpha = 0.25f), shape)
            .pressable(onClick = onClick, enabled = enabled)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (glyph != null) GlyphIcon(glyph, tint = contentColor, size = 22.dp, filled = true)
            Text(text, style = MaterialTheme.typography.titleMedium, color = contentColor, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    glyph: Glyph? = null,
    enabled: Boolean = true,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(container.copy(alpha = if (enabled) 1f else 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), shape)
            .pressable(onClick = onClick, enabled = enabled)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (glyph != null) GlyphIcon(glyph, tint = contentColor, size = 20.dp)
            Text(text, style = MaterialTheme.typography.labelLarge, color = contentColor.copy(alpha = if (enabled) 1f else 0.5f))
        }
    }
}

/** Frosted surface used for most panels. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    color: Color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.82f),
    borderColor: Color = Color.White.copy(alpha = if (GameTheme.colors.isDark) 0.08f else 0.6f),
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(color)
            .border(1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.pressable(onClick = onClick, pressedScale = 0.97f) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun Pill(
    text: String,
    modifier: Modifier = Modifier,
    glyph: Glyph? = null,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    glyphTint: Color = contentColor,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        if (glyph != null) GlyphIcon(glyph, tint = glyphTint, size = 14.dp, filled = true)
        Text(text, style = MaterialTheme.typography.labelMedium, color = contentColor, maxLines = 1)
    }
}

@Composable
fun CoinPill(coins: Int, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val gold = GameTheme.colors.gold
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f))
            .border(1.dp, gold.copy(alpha = 0.45f), RoundedCornerShape(100.dp))
            .then(if (onClick != null) Modifier.pressable(onClick = onClick) else Modifier)
            .padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        GlyphIcon(Glyph.COIN, tint = gold, size = 22.dp)
        Text(coins.formatThousands(), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun StarRow(stars: Int, modifier: Modifier = Modifier, max: Int = 3, size: Dp = 18.dp, spacing: Dp = 2.dp) {
    val gold = GameTheme.colors.gold
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(spacing)) {
        repeat(max) { index ->
            GlyphIcon(
                Glyph.STAR,
                tint = if (index < stars) gold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                size = size,
                filled = index < stars,
            )
        }
    }
}

/** A themed modal panel with scrim, entrance animation and back handling. */
@Composable
fun GameDialog(
    visible: Boolean,
    onDismiss: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (visible) BackHandler(enabled = onDismiss != null) { onDismiss?.invoke() }
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.62f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss?.invoke() },
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(spring(dampingRatio = 0.62f, stiffness = 420f), initialScale = 0.82f) + fadeIn(),
                exit = scaleOut(targetScale = 0.9f) + fadeOut(),
            ) {
                Column(
                    modifier = modifier
                        .padding(20.dp)
                        .widthIn(max = 460.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(30.dp))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    content = content,
                )
            }
        }
    }
}

/** Large circular medallion with a glyph, used for mode and achievement art. */
@Composable
fun GlyphMedallion(
    glyph: Glyph,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    tint: Color = Color.White,
    filled: Boolean = true,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(Brush.linearGradient(colors))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(size * 0.32f)),
        contentAlignment = Alignment.Center,
    ) {
        GlyphIcon(glyph, tint = tint, size = size * 0.5f, filled = filled)
    }
}

@Composable
fun ProgressBar(progress: Float, modifier: Modifier = Modifier, colors: List<Color>, height: Dp = 10.dp) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), label = "bar")
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(100.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(RoundedCornerShape(100.dp))
                .background(Brush.horizontalGradient(colors)),
        )
    }
}

/** Upper-cases with the UI language's rules, so Turkish "i" becomes "İ" rather than "I". */
@Composable
fun String.uppercaseForUi(): String = uppercase(LocalConfiguration.current.locales[0])

fun Int.formatThousands(): String = String.format(Locale.US, "%,d", this).replace(',', '.')

fun Int.formatClock(): String = String.format(Locale.US, "%d:%02d", this / 60, this % 60)

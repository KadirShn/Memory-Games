package com.kadirsahin.hafizaoyunu.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.kadirsahin.hafizaoyunu.R
import com.kadirsahin.hafizaoyunu.domain.CARD_ART_COUNT
import com.kadirsahin.hafizaoyunu.domain.CardBack
import com.kadirsahin.hafizaoyunu.domain.CardTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun CardFace(theme: CardTheme, pairId: Int, modifier: Modifier = Modifier) {
    require(pairId in 0 until CARD_ART_COUNT)
    when (theme) {
        CardTheme.CLASSIC -> SpriteCardFace(R.drawable.card_theme_classic, pairId, modifier, Color(0xFF2A1732))
        CardTheme.COSMIC -> SpriteCardFace(R.drawable.card_theme_cosmic, pairId, modifier)
        CardTheme.FOREST -> SpriteCardFace(R.drawable.card_theme_forest, pairId, modifier)
        CardTheme.NEON -> NeonCardFace(pairId, modifier)
        CardTheme.EMOJI -> EmojiCardFace(pairId, modifier)
    }
}

@Composable
private fun SpriteCardFace(spriteResource: Int, pairId: Int, modifier: Modifier, backgroundColor: Color? = null) {
    val sprite = ImageBitmap.imageResource(spriteResource)
    Canvas(modifier = modifier.fillMaxSize()) {
        backgroundColor?.let { drawRect(it) }
        val column = pairId % SPRITE_COLUMNS
        val row = pairId / SPRITE_COLUMNS
        val left = column * sprite.width / SPRITE_COLUMNS
        val right = (column + 1) * sprite.width / SPRITE_COLUMNS
        val top = row * sprite.height / SPRITE_ROWS
        val bottom = (row + 1) * sprite.height / SPRITE_ROWS
        drawImage(
            image = sprite,
            srcOffset = IntOffset(left, top),
            srcSize = IntSize(right - left, bottom - top),
            dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
            filterQuality = FilterQuality.Medium,
        )
    }
}

private val NeonHues = List(CARD_ART_COUNT) { i -> Color.hsv((i * 137.5f) % 360f, 0.72f, 1f) }

@Composable
private fun NeonCardFace(pairId: Int, modifier: Modifier) {
    val color = NeonHues[pairId]
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(Brush.radialGradient(listOf(Color(0xFF231B4D), Color(0xFF0B0820)), center, size.maxDimension * 0.75f))
        val shape = neonShape(pairId, size.minDimension * 0.62f, center)
        drawPath(shape, color.copy(alpha = 0.22f), style = Stroke(width = size.minDimension * 0.12f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        drawPath(shape, color.copy(alpha = 0.35f), style = Stroke(width = size.minDimension * 0.06f, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        drawPath(shape, Brush.linearGradient(listOf(Color.White, color), Offset.Zero, Offset(size.width, size.height)))
    }
}

/** Eighteen distinct silhouettes so the neon pack stays readable without relying on colour. */
private fun neonShape(index: Int, extent: Float, center: Offset): Path {
    val r = extent / 2
    val cx = center.x
    val cy = center.y
    fun polygon(points: Int, rotation: Double = -PI / 2, radius: Float = r) = Path().apply {
        for (i in 0 until points) {
            val a = rotation + i * 2 * PI / points
            val x = cx + radius * cos(a).toFloat()
            val y = cy + radius * sin(a).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    fun starPath(points: Int, inner: Float) = Path().apply {
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) r else r * inner
            val a = -PI / 2 + i * PI / points
            val x = cx + radius * cos(a).toFloat()
            val y = cy + radius * sin(a).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
    fun circlePath(x: Float, y: Float, radius: Float) = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(Offset(x, y), radius))
    }
    return when (index) {
        0 -> circlePath(cx, cy, r * 0.9f)
        1 -> Path().apply {
            addRoundRect(androidx.compose.ui.geometry.RoundRect(cx - r * 0.8f, cy - r * 0.8f, cx + r * 0.8f, cy + r * 0.8f, CornerRadius(r * 0.18f)))
        }
        2 -> polygon(3, radius = r * 1.05f)
        3 -> polygon(4, rotation = -PI / 2)
        4 -> starPath(5, 0.45f)
        5 -> polygon(6, rotation = 0.0)
        6 -> Path().apply {
            moveTo(cx, cy + r * 0.85f)
            cubicTo(cx - r * 1.4f, cy - r * 0.1f, cx - r * 0.75f, cy - r * 1.15f, cx, cy - r * 0.45f)
            cubicTo(cx + r * 0.75f, cy - r * 1.15f, cx + r * 1.4f, cy - r * 0.1f, cx, cy + r * 0.85f)
            close()
        }
        7 -> Path().apply {
            val t = r * 0.32f
            moveTo(cx - t, cy - r); lineTo(cx + t, cy - r); lineTo(cx + t, cy - t); lineTo(cx + r, cy - t)
            lineTo(cx + r, cy + t); lineTo(cx + t, cy + t); lineTo(cx + t, cy + r); lineTo(cx - t, cy + r)
            lineTo(cx - t, cy + t); lineTo(cx - r, cy + t); lineTo(cx - r, cy - t); lineTo(cx - t, cy - t)
            close()
        }
        8 -> Path.combine(
            androidx.compose.ui.graphics.PathOperation.Difference,
            circlePath(cx, cy, r * 0.9f),
            circlePath(cx + r * 0.42f, cy - r * 0.25f, r * 0.75f),
        )
        9 -> Path.combine(
            androidx.compose.ui.graphics.PathOperation.Difference,
            circlePath(cx, cy, r * 0.9f),
            circlePath(cx, cy, r * 0.48f),
        )
        10 -> polygon(5)
        11 -> Path().apply {
            moveTo(cx + r * 0.2f, cy - r); lineTo(cx - r * 0.65f, cy + r * 0.12f); lineTo(cx - r * 0.05f, cy + r * 0.12f)
            lineTo(cx - r * 0.25f, cy + r); lineTo(cx + r * 0.7f, cy - r * 0.2f); lineTo(cx + r * 0.08f, cy - r * 0.2f)
            close()
        }
        12 -> Path().apply {
            moveTo(cx, cy - r)
            cubicTo(cx + r * 0.25f, cy - r * 0.45f, cx + r * 0.78f, cy - r * 0.05f, cx + r * 0.78f, cy + r * 0.32f)
            cubicTo(cx + r * 0.78f, cy + r * 0.78f, cx + r * 0.42f, cy + r, cx, cy + r)
            cubicTo(cx - r * 0.42f, cy + r, cx - r * 0.78f, cy + r * 0.78f, cx - r * 0.78f, cy + r * 0.32f)
            cubicTo(cx - r * 0.78f, cy - r * 0.05f, cx - r * 0.25f, cy - r * 0.45f, cx, cy - r)
            close()
        }
        13 -> polygon(8, rotation = PI / 8)
        14 -> starPath(4, 0.3f)
        15 -> Path().apply {
            moveTo(cx, cy - r); lineTo(cx + r * 0.9f, cy); lineTo(cx + r * 0.35f, cy); lineTo(cx + r * 0.35f, cy + r)
            lineTo(cx - r * 0.35f, cy + r); lineTo(cx - r * 0.35f, cy); lineTo(cx - r * 0.9f, cy)
            close()
        }
        16 -> Path().apply {
            val t = r * 0.3f
            val d = r * 0.95f
            moveTo(cx - d, cy - d + t); lineTo(cx - d + t, cy - d); lineTo(cx, cy - t); lineTo(cx + d - t, cy - d)
            lineTo(cx + d, cy - d + t); lineTo(cx + t, cy); lineTo(cx + d, cy + d - t); lineTo(cx + d - t, cy + d)
            lineTo(cx, cy + t); lineTo(cx - d + t, cy + d); lineTo(cx - d, cy + d - t); lineTo(cx - t, cy)
            close()
        }
        else -> Path().apply {
            val small = r * 0.46f
            for (i in 0 until 3) {
                val a = -PI / 2 + i * 2 * PI / 3
                addOval(androidx.compose.ui.geometry.Rect(Offset(cx + r * 0.5f * cos(a).toFloat(), cy + r * 0.5f * sin(a).toFloat()), small))
            }
        }
    }
}

private val Animals = listOf("🦊", "🐼", "🐸", "🦁", "🐙", "🦄", "🐨", "🐯", "🐧", "🦉", "🐢", "🦋", "🐳", "🐝", "🦀", "🐰", "🦜", "🐲")

@Composable
private fun EmojiCardFace(pairId: Int, modifier: Modifier) {
    val hue = (pairId * 47f) % 360f
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(Color.hsv(hue, 0.28f, 1f), Color.hsv((hue + 40f) % 360f, 0.45f, 0.92f)))),
        contentAlignment = Alignment.Center,
    ) {
        val fontSize = with(LocalDensity.current) { (min(maxWidth.toPx(), maxHeight.toPx()) * 0.56f).toSp() }
        Text(text = Animals[pairId], style = TextStyle(fontSize = fontSize))
    }
}

/** Theme coloured emblem shown on the default back so each pack keeps its own identity. */
private fun themePalette(theme: CardTheme): List<Color> = when (theme) {
    CardTheme.CLASSIC -> listOf(Color(0xFF4A173D), Color(0xFFC53F58), Color(0xFFF2A541))
    CardTheme.COSMIC -> listOf(Color(0xFF111A4B), Color(0xFF7536B8), Color(0xFF087D9E))
    CardTheme.FOREST -> listOf(Color(0xFF173E2C), Color(0xFF2C6B3F), Color(0xFF8A6A24))
    CardTheme.NEON -> listOf(Color(0xFF12082E), Color(0xFF5B1FA8), Color(0xFFE0329B))
    CardTheme.EMOJI -> listOf(Color(0xFF0B5F8A), Color(0xFF26A69A), Color(0xFFFFC857))
}

@Composable
fun CardBackArt(back: CardBack, theme: CardTheme, modifier: Modifier = Modifier) {
    val palette = remember(theme) { themePalette(theme) }
    Canvas(modifier = modifier.fillMaxSize()) {
        when (back) {
            CardBack.AURORA -> drawAurora(palette)
            CardBack.NEON_GRID -> drawNeonGrid()
            CardBack.WAVES -> drawWaves()
            CardBack.CONSTELLATION -> drawConstellation()
            CardBack.HEX -> drawHex()
            CardBack.SUNBURST -> drawSunburst()
            CardBack.ROYAL -> drawRoyal()
        }
        val inset = size.minDimension * 0.07f
        drawRoundRect(
            color = Color.White.copy(alpha = 0.28f),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(size.minDimension * 0.1f),
            style = Stroke(width = size.minDimension * 0.022f),
        )
        // A soft sheen gives every back a glossy, physical look.
        drawRect(
            Brush.linearGradient(
                0f to Color.White.copy(alpha = 0.22f),
                0.45f to Color.Transparent,
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
        )
    }
}

private fun DrawScope.drawAurora(palette: List<Color>) {
    drawRect(Brush.linearGradient(palette, Offset.Zero, Offset(size.width, size.height)))
    drawCircle(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), Offset(size.width * 0.25f, size.height * 0.2f), size.minDimension * 0.6f))
    val emblem = size.minDimension * 0.2f
    drawPath(sparkPath(center, emblem), Color.White.copy(alpha = 0.92f))
    drawCircle(Color.White.copy(alpha = 0.35f), emblem * 1.45f, center, style = Stroke(size.minDimension * 0.018f))
}

private fun DrawScope.drawNeonGrid() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF0E0628), Color(0xFF2B0A4A))))
    val step = size.minDimension / 6
    var x = step / 2
    while (x < size.width) {
        drawLine(Color(0xFF22D3EE).copy(alpha = 0.32f), Offset(x, 0f), Offset(x, size.height), strokeWidth = size.minDimension * 0.012f)
        x += step
    }
    var y = step / 2
    while (y < size.height) {
        drawLine(Color(0xFFE0329B).copy(alpha = 0.32f), Offset(0f, y), Offset(size.width, y), strokeWidth = size.minDimension * 0.012f)
        y += step
    }
    val r = size.minDimension * 0.24f
    val diamond = Path().apply {
        moveTo(center.x, center.y - r); lineTo(center.x + r, center.y); lineTo(center.x, center.y + r); lineTo(center.x - r, center.y); close()
    }
    drawPath(diamond, Color(0xFFFF4FD8).copy(alpha = 0.3f), style = Stroke(size.minDimension * 0.08f))
    drawPath(diamond, Color(0xFFFF8BE8), style = Stroke(size.minDimension * 0.03f))
}

private fun DrawScope.drawWaves() {
    drawRect(Brush.linearGradient(listOf(Color(0xFF0B3B6F), Color(0xFF0E8C9B)), Offset.Zero, Offset(size.width, size.height)))
    val rows = 6
    for (row in 0 until rows) {
        val baseY = size.height * (row + 0.5f) / rows
        val path = Path()
        val steps = 24
        for (i in 0..steps) {
            val x = size.width * i / steps
            val y = baseY + sin(i / steps.toFloat() * 2 * PI * 1.5 + row).toFloat() * size.height * 0.045f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, Color.White.copy(alpha = 0.18f + 0.06f * (row % 2)), style = Stroke(size.minDimension * 0.025f, cap = StrokeCap.Round))
    }
    drawCircle(Color(0xFFFFE08A), size.minDimension * 0.12f, Offset(size.width * 0.72f, size.height * 0.28f))
}

private fun DrawScope.drawConstellation() {
    drawRect(Brush.radialGradient(listOf(Color(0xFF2A1B5E), Color(0xFF070414)), center, size.maxDimension * 0.8f))
    val random = Random(42)
    repeat(26) {
        drawCircle(Color.White.copy(alpha = 0.3f + random.nextFloat() * 0.6f), size.minDimension * (0.006f + random.nextFloat() * 0.012f), Offset(random.nextFloat() * size.width, random.nextFloat() * size.height))
    }
    val points = listOf(0.22f to 0.3f, 0.42f to 0.22f, 0.6f to 0.4f, 0.78f to 0.3f, 0.56f to 0.66f, 0.32f to 0.72f)
        .map { (x, y) -> Offset(size.width * x, size.height * y) }
    points.zipWithNext().forEach { (a, b) -> drawLine(Color(0xFFB9A8FF).copy(alpha = 0.6f), a, b, strokeWidth = size.minDimension * 0.012f) }
    drawLine(Color(0xFFB9A8FF).copy(alpha = 0.6f), points[2], points[4], strokeWidth = size.minDimension * 0.012f)
    points.forEach { drawCircle(Color.White, size.minDimension * 0.025f, it) }
}

private fun DrawScope.drawHex() {
    drawRect(Brush.linearGradient(listOf(Color(0xFF3A230B), Color(0xFF7A4A10)), Offset.Zero, Offset(size.width, size.height)))
    val r = size.minDimension / 7
    val w = sqrt(3f) * r
    var row = 0
    var y = 0f
    while (y < size.height + r) {
        var x = if (row % 2 == 0) 0f else w / 2
        while (x < size.width + w) {
            val hex = Path().apply {
                for (i in 0 until 6) {
                    val a = PI / 6 + i * PI / 3
                    val px = x + r * 0.9f * cos(a).toFloat()
                    val py = y + r * 0.9f * sin(a).toFloat()
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawPath(hex, Color(0xFFFFC23D).copy(alpha = 0.55f), style = Stroke(size.minDimension * 0.016f))
            x += w
        }
        y += r * 1.5f
        row += 1
    }
    drawCircle(Brush.radialGradient(listOf(Color(0xFFFFD36B), Color(0xFFB8760F)), center, size.minDimension * 0.18f), size.minDimension * 0.17f, center)
}

private fun DrawScope.drawSunburst() {
    drawRect(Color(0xFFE2462C))
    val rays = 16
    val radius = size.maxDimension
    for (i in 0 until rays step 2) {
        val a1 = i * 2 * PI / rays
        val a2 = (i + 1) * 2 * PI / rays
        val ray = Path().apply {
            moveTo(center.x, center.y)
            lineTo(center.x + radius * cos(a1).toFloat(), center.y + radius * sin(a1).toFloat())
            lineTo(center.x + radius * cos(a2).toFloat(), center.y + radius * sin(a2).toFloat())
            close()
        }
        drawPath(ray, Color(0xFFFF9F1C))
    }
    drawCircle(Color(0xFFFFE66D), size.minDimension * 0.2f, center)
    drawCircle(Color(0xFFFF9F1C), size.minDimension * 0.2f, center, style = Stroke(size.minDimension * 0.03f))
}

private fun DrawScope.drawRoyal() {
    drawRect(Brush.linearGradient(listOf(Color(0xFF0E3B2E), Color(0xFF3A0C24)), Offset.Zero, Offset(size.width, size.height)))
    val gold = Color(0xFFE6C25A)
    val step = size.minDimension / 4
    var offset = -size.maxDimension
    while (offset < size.maxDimension * 2) {
        drawLine(gold.copy(alpha = 0.35f), Offset(offset, 0f), Offset(offset + size.height, size.height), strokeWidth = size.minDimension * 0.012f)
        drawLine(gold.copy(alpha = 0.35f), Offset(offset + size.height, 0f), Offset(offset, size.height), strokeWidth = size.minDimension * 0.012f)
        offset += step
    }
    rotate(45f) {
        val s = size.minDimension * 0.3f
        drawRect(gold, Offset(center.x - s / 2, center.y - s / 2), Size(s, s))
        drawRect(Color(0xFF3A0C24), Offset(center.x - s * 0.32f, center.y - s * 0.32f), Size(s * 0.64f, s * 0.64f))
    }
    drawCircle(gold, size.minDimension * 0.05f, center)
}

private fun sparkPath(center: Offset, r: Float) = Path().apply {
    moveTo(center.x, center.y - r)
    quadraticTo(center.x + r * 0.12f, center.y - r * 0.12f, center.x + r, center.y)
    quadraticTo(center.x + r * 0.12f, center.y + r * 0.12f, center.x, center.y + r)
    quadraticTo(center.x - r * 0.12f, center.y + r * 0.12f, center.x - r, center.y)
    quadraticTo(center.x - r * 0.12f, center.y - r * 0.12f, center.x, center.y - r)
    close()
}

private const val SPRITE_COLUMNS = 6
private const val SPRITE_ROWS = 3

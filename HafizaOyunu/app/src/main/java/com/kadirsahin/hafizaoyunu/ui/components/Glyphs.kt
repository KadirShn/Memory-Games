package com.kadirsahin.hafizaoyunu.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** The app's own icon set, drawn on a 24×24 grid so it needs no icon library and scales crisply. */
enum class Glyph {
    BACK, CLOSE, PAUSE, PLAY, REPLAY, HOME, SETTINGS, TROPHY, STAR, HEART, BOLT, EYE, BULB, SNOWFLAKE,
    COIN, LOCK, CHART, CALENDAR, USERS, LEAF, SOUND_ON, SOUND_OFF, CHECK, SHOP, SHUFFLE, HOURGLASS,
    CLOCK, FLAG, SPARK, CARDS, FLAME, CROWN, INFO, MEDAL, MOON, NEXT, USER,
}

@Composable
fun GlyphIcon(
    glyph: Glyph,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    filled: Boolean = false,
    contentDescription: String? = null,
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics { this.contentDescription = contentDescription }
    } else {
        Modifier
    }
    Canvas(modifier = modifier.size(size).then(semanticsModifier)) {
        GlyphPainter(this, tint).draw(glyph, filled)
    }
}

private class GlyphPainter(private val scope: DrawScope, private val color: Color) {
    private val unit = scope.size.minDimension / 24f
    private val stroke = Stroke(width = 2f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)

    private fun o(x: Float, y: Float) = Offset(x * unit, y * unit)

    private fun path(block: PathBuilder.() -> Unit): Path = Path().also { PathBuilder(it, unit).block() }

    private fun strokePath(path: Path, width: Float = 2f) =
        scope.drawPath(path, color, style = if (width == 2f) stroke else Stroke(width * unit, cap = StrokeCap.Round, join = StrokeJoin.Round))

    private fun fillPath(path: Path, tint: Color = color) = scope.drawPath(path, tint)

    private fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float = 2f) =
        scope.drawLine(color, o(x1, y1), o(x2, y2), strokeWidth = width * unit, cap = StrokeCap.Round)

    private fun circle(cx: Float, cy: Float, r: Float, fill: Boolean = false, tint: Color = color) =
        scope.drawCircle(tint, r * unit, o(cx, cy), style = if (fill) androidx.compose.ui.graphics.drawscope.Fill else stroke)

    private fun arc(cx: Float, cy: Float, r: Float, start: Float, sweep: Float) = scope.drawArc(
        color = color,
        startAngle = start,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = o(cx - r, cy - r),
        size = Size(2 * r * unit, 2 * r * unit),
        style = stroke,
    )

    private fun roundRect(l: Float, t: Float, r: Float, b: Float, radius: Float, fill: Boolean = false) = scope.drawRoundRect(
        color = color,
        topLeft = o(l, t),
        size = Size((r - l) * unit, (b - t) * unit),
        cornerRadius = CornerRadius(radius * unit),
        style = if (fill) androidx.compose.ui.graphics.drawscope.Fill else stroke,
    )

    private fun star(cx: Float, cy: Float, outer: Float, inner: Float, points: Int = 5): Path = Path().apply {
        for (i in 0 until points * 2) {
            val radius = if (i % 2 == 0) outer else inner
            val angle = -PI / 2 + i * PI / points
            val x = (cx + radius * cos(angle)).toFloat() * unit
            val y = (cy + radius * sin(angle)).toFloat() * unit
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    fun draw(glyph: Glyph, filled: Boolean) {
        when (glyph) {
            Glyph.BACK -> strokePath(path { m(15f, 5f); l(8f, 12f); l(15f, 19f) }, 2.4f)
            Glyph.NEXT -> strokePath(path { m(9f, 5f); l(16f, 12f); l(9f, 19f) }, 2.4f)
            Glyph.CLOSE -> {
                line(6f, 6f, 18f, 18f, 2.3f)
                line(18f, 6f, 6f, 18f, 2.3f)
            }
            Glyph.PAUSE -> {
                roundRect(6.5f, 5f, 10f, 19f, 1.3f, fill = true)
                roundRect(14f, 5f, 17.5f, 19f, 1.3f, fill = true)
            }
            Glyph.PLAY -> {
                val p = path { m(8f, 5f); l(19f, 12f); l(8f, 19f); close() }
                fillPath(p)
                strokePath(p)
            }
            Glyph.REPLAY -> {
                arc(12f, 12f, 7f, 0f, 290f)
                val angle = (-70.0) * PI / 180
                val tip = Offset((12 + 7 * cos(angle)).toFloat(), (12 + 7 * sin(angle)).toFloat())
                val dir = Offset(-sin(angle).toFloat(), cos(angle).toFloat())
                val perp = Offset(-dir.y, dir.x)
                line(tip.x, tip.y, tip.x - dir.x * 3.5f + perp.x * 3f, tip.y - dir.y * 3.5f + perp.y * 3f)
                line(tip.x, tip.y, tip.x - dir.x * 3.5f - perp.x * 3f, tip.y - dir.y * 3.5f - perp.y * 3f)
            }
            Glyph.HOME -> {
                strokePath(path { m(3.5f, 11.5f); l(12f, 4f); l(20.5f, 11.5f) })
                strokePath(path { m(6.5f, 9.5f); l(6.5f, 19.5f); l(17.5f, 19.5f); l(17.5f, 9.5f) })
                strokePath(path { m(10f, 19.5f); l(10f, 14.5f); l(14f, 14.5f); l(14f, 19.5f) })
            }
            Glyph.SETTINGS -> {
                circle(12f, 12f, 6f)
                circle(12f, 12f, 2.4f)
                for (i in 0 until 8) {
                    val a = i * PI / 4
                    line(
                        (12 + 6.8 * cos(a)).toFloat(), (12 + 6.8 * sin(a)).toFloat(),
                        (12 + 9.2 * cos(a)).toFloat(), (12 + 9.2 * sin(a)).toFloat(),
                        3f,
                    )
                }
            }
            Glyph.TROPHY -> {
                val cup = path { m(7f, 4f); l(7f, 9f); c(7f, 14f, 17f, 14f, 17f, 9f); l(17f, 4f); close() }
                if (filled) fillPath(cup)
                strokePath(cup)
                strokePath(path { m(7f, 5.8f); c(3.2f, 5.8f, 3.2f, 11.2f, 7.8f, 11.2f) })
                strokePath(path { m(17f, 5.8f); c(20.8f, 5.8f, 20.8f, 11.2f, 16.2f, 11.2f) })
                line(12f, 13f, 12f, 17f)
                line(8f, 20f, 16f, 20f)
                line(9.5f, 17f, 14.5f, 17f)
            }
            Glyph.STAR -> {
                val p = star(12f, 12.6f, 9.6f, 4.3f)
                if (filled) fillPath(p)
                strokePath(p, 1.8f)
            }
            Glyph.HEART -> {
                val p = path {
                    m(12f, 20.5f)
                    c(4f, 15f, 2.2f, 9.2f, 6.3f, 6.2f)
                    c(8.9f, 4.4f, 11.2f, 5.6f, 12f, 7.6f)
                    c(12.8f, 5.6f, 15.1f, 4.4f, 17.7f, 6.2f)
                    c(21.8f, 9.2f, 20f, 15f, 12f, 20.5f)
                    close()
                }
                if (filled) fillPath(p) else strokePath(p)
            }
            Glyph.BOLT -> {
                val p = path { m(13.5f, 2.5f); l(5.5f, 13.5f); l(11f, 13.5f); l(10f, 21.5f); l(18.5f, 10f); l(12.8f, 10f); close() }
                fillPath(p)
                strokePath(p, 1.2f)
            }
            Glyph.EYE -> {
                strokePath(path { m(2.5f, 12f); q(12f, 3f, 21.5f, 12f); q(12f, 21f, 2.5f, 12f); close() })
                circle(12f, 12f, 3f, fill = true)
            }
            Glyph.BULB -> {
                arc(12f, 9.5f, 6f, 135f, 270f)
                line(7.76f, 13.74f, 9.5f, 17f)
                line(16.24f, 13.74f, 14.5f, 17f)
                line(9.5f, 17f, 14.5f, 17f)
                line(10.5f, 20.2f, 13.5f, 20.2f)
            }
            Glyph.SNOWFLAKE -> {
                for (i in 0 until 6) {
                    val a = i * PI / 3 - PI / 2
                    val ex = (12 + 9 * cos(a)).toFloat()
                    val ey = (12 + 9 * sin(a)).toFloat()
                    line(12f, 12f, ex, ey, 1.8f)
                    val bx = (12 + 5.8 * cos(a)).toFloat()
                    val by = (12 + 5.8 * sin(a)).toFloat()
                    for (side in listOf(-1, 1)) {
                        val b = a + side * PI / 4
                        line(bx, by, (bx + 2.8 * cos(b)).toFloat(), (by + 2.8 * sin(b)).toFloat(), 1.8f)
                    }
                }
            }
            Glyph.COIN -> {
                circle(12f, 12f, 9.5f, fill = true)
                circle(12f, 12f, 6.3f, tint = Color.Black.copy(alpha = 0.18f))
                scope.drawCircle(Color.White.copy(alpha = 0.5f), 1.6f * unit, o(9f, 8.6f))
            }
            Glyph.LOCK -> {
                roundRect(5f, 10.5f, 19f, 20.5f, 2.4f, fill = filled)
                if (!filled) circle(12f, 15.4f, 1.3f, fill = true)
                arc(12f, 8f, 4f, 180f, 180f)
                line(8f, 8f, 8f, 10.5f)
                line(16f, 8f, 16f, 10.5f)
            }
            Glyph.CHART -> {
                line(6f, 19f, 6f, 13f, 3f)
                line(12f, 19f, 12f, 5.5f, 3f)
                line(18f, 19f, 18f, 9.5f, 3f)
            }
            Glyph.CALENDAR -> {
                roundRect(4f, 5.5f, 20f, 20.5f, 2.5f)
                line(4f, 10f, 20f, 10f)
                line(8f, 3.5f, 8f, 7f)
                line(16f, 3.5f, 16f, 7f)
                circle(12f, 15.2f, 1.6f, fill = true)
            }
            Glyph.USER -> {
                circle(12f, 8f, 4f, fill = filled)
                if (!filled) circle(12f, 8f, 4f)
                val body = path { m(4.5f, 20.5f); c(4.5f, 15.5f, 8f, 13.5f, 12f, 13.5f); c(16f, 13.5f, 19.5f, 15.5f, 19.5f, 20.5f); close() }
                if (filled) fillPath(body) else strokePath(body)
            }
            Glyph.USERS -> {
                circle(9f, 8.5f, 3.2f)
                strokePath(path { m(3.2f, 19.5f); q(3.2f, 13.6f, 9f, 13.6f); q(14.8f, 13.6f, 14.8f, 19.5f) })
                circle(16.8f, 8.8f, 2.6f)
                strokePath(path { m(16.4f, 13.6f); q(20.8f, 13.8f, 20.8f, 18.6f) })
            }
            Glyph.LEAF -> {
                val p = path { m(5f, 19f); c(5f, 9f, 11f, 4.5f, 20f, 4.5f); c(20f, 13f, 15f, 19f, 5f, 19f); close() }
                if (filled) fillPath(p)
                strokePath(p)
                line(4f, 20f, 14f, 10f)
            }
            Glyph.SOUND_ON, Glyph.SOUND_OFF -> {
                val p = path { m(3.5f, 9.5f); l(7.5f, 9.5f); l(12.5f, 5f); l(12.5f, 19f); l(7.5f, 14.5f); l(3.5f, 14.5f); close() }
                fillPath(p)
                strokePath(p)
                if (glyph == Glyph.SOUND_ON) {
                    arc(12.5f, 12f, 4f, -45f, 90f)
                    arc(12.5f, 12f, 7.5f, -45f, 90f)
                } else {
                    line(16.5f, 9.5f, 21f, 14.5f)
                    line(21f, 9.5f, 16.5f, 14.5f)
                }
            }
            Glyph.CHECK -> strokePath(path { m(5f, 12.5f); l(10f, 17.5f); l(19f, 7f) }, 2.6f)
            Glyph.SHOP -> {
                val p = path { m(5f, 8f); l(19f, 8f); l(18f, 20.5f); l(6f, 20.5f); close() }
                if (filled) fillPath(p)
                strokePath(p)
                arc(12f, 8f, 3.6f, 180f, 180f)
            }
            Glyph.SHUFFLE -> {
                strokePath(path { m(3f, 7f); l(7f, 7f); c(12f, 7f, 12f, 17f, 17f, 17f); l(20.5f, 17f) })
                strokePath(path { m(3f, 17f); l(7f, 17f); c(12f, 17f, 12f, 7f, 17f, 7f); l(20.5f, 7f) })
                strokePath(path { m(18f, 4.5f); l(20.5f, 7f); l(18f, 9.5f) })
                strokePath(path { m(18f, 14.5f); l(20.5f, 17f); l(18f, 19.5f) })
            }
            Glyph.HOURGLASS -> {
                line(6f, 3.5f, 18f, 3.5f)
                line(6f, 20.5f, 18f, 20.5f)
                strokePath(path { m(7.5f, 3.5f); c(7.5f, 9f, 11.5f, 10f, 11.5f, 12f); c(11.5f, 14f, 7.5f, 15f, 7.5f, 20.5f) })
                strokePath(path { m(16.5f, 3.5f); c(16.5f, 9f, 12.5f, 10f, 12.5f, 12f); c(12.5f, 14f, 16.5f, 15f, 16.5f, 20.5f) })
                fillPath(path { m(9f, 20f); l(15f, 20f); l(12f, 16.2f); close() })
            }
            Glyph.CLOCK -> {
                circle(12f, 12f, 9f)
                line(12f, 12f, 12f, 7f)
                line(12f, 12f, 15.5f, 14f)
            }
            Glyph.FLAG -> {
                line(6f, 21f, 6f, 3.5f)
                val p = path { m(6f, 4f); l(18.5f, 4f); l(15.5f, 8f); l(18.5f, 12f); l(6f, 12f); close() }
                fillPath(p)
                strokePath(p)
            }
            Glyph.SPARK -> fillPath(
                path { m(12f, 2f); q(13f, 11f, 22f, 12f); q(13f, 13f, 12f, 22f); q(11f, 13f, 2f, 12f); q(11f, 11f, 12f, 2f); close() },
            )
            Glyph.CARDS -> {
                roundRect(4.5f, 7.5f, 14f, 20.5f, 2f)
                roundRect(10f, 3.5f, 19.5f, 16.5f, 2f, fill = filled)
                if (!filled) roundRect(10f, 3.5f, 19.5f, 16.5f, 2f)
            }
            Glyph.FLAME -> fillPath(
                path {
                    m(12f, 2.5f)
                    c(13f, 7f, 18.5f, 9f, 18.5f, 14.5f)
                    c(18.5f, 18.5f, 15.5f, 21.5f, 12f, 21.5f)
                    c(8.5f, 21.5f, 5.5f, 18.5f, 5.5f, 14.5f)
                    c(5.5f, 11.5f, 7.5f, 9.5f, 8.5f, 8.5f)
                    c(8.7f, 11f, 10f, 12f, 11f, 12f)
                    c(10f, 8.5f, 11f, 5f, 12f, 2.5f)
                    close()
                },
            )
            Glyph.CROWN -> {
                val p = path { m(3.5f, 8f); l(8f, 12.5f); l(12f, 5f); l(16f, 12.5f); l(20.5f, 8f); l(19f, 18.5f); l(5f, 18.5f); close() }
                fillPath(p)
                strokePath(p, 1.4f)
            }
            Glyph.INFO -> {
                circle(12f, 12f, 9f)
                line(12f, 11f, 12f, 16.5f)
                circle(12f, 7.6f, 1.3f, fill = true)
            }
            Glyph.MEDAL -> {
                line(8.5f, 10.5f, 6f, 3.5f)
                line(15.5f, 10.5f, 18f, 3.5f)
                circle(12f, 14.5f, 5.8f, fill = filled)
                if (!filled) circle(12f, 14.5f, 2f, fill = true)
            }
            Glyph.MOON -> fillPath(
                path { m(15f, 3f); c(9f, 3.5f, 6f, 8f, 6f, 12f); c(6f, 17f, 10f, 21f, 15f, 21f); c(11.5f, 18f, 10.5f, 15f, 10.5f, 12f); c(10.5f, 8.5f, 12f, 5f, 15f, 3f); close() },
            )
        }
    }
}

private class PathBuilder(private val path: Path, private val unit: Float) {
    fun m(x: Float, y: Float) = path.moveTo(x * unit, y * unit)
    fun l(x: Float, y: Float) = path.lineTo(x * unit, y * unit)
    fun q(x1: Float, y1: Float, x2: Float, y2: Float) = path.quadraticTo(x1 * unit, y1 * unit, x2 * unit, y2 * unit)
    fun c(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) =
        path.cubicTo(x1 * unit, y1 * unit, x2 * unit, y2 * unit, x3 * unit, y3 * unit)
    fun close() = path.close()
}

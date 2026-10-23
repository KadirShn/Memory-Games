package com.kadirsahin.hafizaoyunu.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val maxLife: Float,
    val color: Color,
    val size: Float,
    val confetti: Boolean,
    var rotation: Float,
    val spin: Float,
    val gravity: Float,
    val drag: Float,
)

/** A tiny CPU particle system; it only runs a frame loop while particles are alive. */
@Stable
class ParticleField {
    private val particles = ArrayList<Particle>()
    private val random = Random(System.nanoTime())
    internal var frame by mutableLongStateOf(0L)
    internal var active by mutableStateOf(false)

    fun burst(center: Offset, colors: List<Color>, count: Int = 28, speed: Float = 900f, size: Float = 9f) {
        repeat(count) {
            val angle = random.nextFloat() * 2 * PI.toFloat()
            val velocity = speed * (0.35f + random.nextFloat() * 0.65f)
            val life = 0.55f + random.nextFloat() * 0.45f
            particles += Particle(
                x = center.x,
                y = center.y,
                vx = cos(angle) * velocity,
                vy = sin(angle) * velocity,
                life = life,
                maxLife = life,
                color = colors[random.nextInt(colors.size)],
                size = size * (0.5f + random.nextFloat()),
                confetti = random.nextFloat() < 0.35f,
                rotation = random.nextFloat() * 360f,
                spin = (random.nextFloat() - 0.5f) * 720f,
                gravity = 900f,
                drag = 2.6f,
            )
        }
        active = true
    }

    fun confetti(width: Float, colors: List<Color>, count: Int = 170) {
        repeat(count) {
            val life = 2.2f + random.nextFloat() * 1.6f
            particles += Particle(
                x = random.nextFloat() * width,
                y = -random.nextFloat() * 400f - 20f,
                vx = (random.nextFloat() - 0.5f) * 260f,
                vy = 180f + random.nextFloat() * 360f,
                life = life,
                maxLife = life,
                color = colors[random.nextInt(colors.size)],
                size = 9f + random.nextFloat() * 9f,
                confetti = true,
                rotation = random.nextFloat() * 360f,
                spin = (random.nextFloat() - 0.5f) * 900f,
                gravity = 260f,
                drag = 0.4f,
            )
        }
        active = true
    }

    internal fun step(dt: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.life -= dt
            if (p.life <= 0f) {
                iterator.remove()
                continue
            }
            p.vx -= p.vx * p.drag * dt
            p.vy -= p.vy * p.drag * dt
            p.vy += p.gravity * dt
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.rotation += p.spin * dt
        }
        if (particles.isEmpty()) active = false
    }

    internal fun draw(scope: androidx.compose.ui.graphics.drawscope.DrawScope) {
        particles.forEach { p ->
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            if (p.confetti) {
                scope.rotate(p.rotation, Offset(p.x, p.y)) {
                    drawRect(p.color.copy(alpha = alpha), Offset(p.x - p.size / 2, p.y - p.size / 4), Size(p.size, p.size / 2))
                }
            } else {
                scope.drawCircle(p.color.copy(alpha = alpha), p.size / 2 * (0.4f + alpha * 0.6f), Offset(p.x, p.y))
            }
        }
    }
}

@Composable
fun rememberParticleField(): ParticleField = remember { ParticleField() }

@Composable
fun ParticleOverlay(field: ParticleField, modifier: Modifier = Modifier) {
    LaunchedEffect(field, field.active) {
        if (!field.active) return@LaunchedEffect
        var last = 0L
        while (field.active) {
            withFrameNanos { now ->
                if (last != 0L) field.step(((now - last) / 1_000_000_000f).coerceAtMost(0.05f))
                last = now
                field.frame = now
            }
        }
    }
    Canvas(modifier = modifier) {
        // Reading the frame counter subscribes this draw pass to every simulation step.
        if (field.frame >= 0L) field.draw(this)
    }
}

/** Short lived text that rises and fades, e.g. "+150 ×2". */
data class FloatingLabel(val id: Long, val text: String, val position: Offset, val color: Color, val big: Boolean = false)

@Stable
class FloatingLabels {
    internal val items = mutableStateListOf<FloatingLabel>()
    private var nextId = 0L

    fun add(text: String, position: Offset, color: Color, big: Boolean = false) {
        items += FloatingLabel(nextId++, text, position, color, big)
    }
}

@Composable
fun rememberFloatingLabels(): FloatingLabels = remember { FloatingLabels() }

@Composable
fun BoxScope.FloatingLabelsOverlay(labels: FloatingLabels) {
    var overlayWidth by remember { mutableIntStateOf(0) }
    Box(Modifier.matchParentSize().onSizeChanged { overlayWidth = it.width })
    labels.items.toList().forEach { label ->
        key(label.id) {
            val progress = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                progress.animateTo(1f, tween(if (label.big) 1300 else 950, easing = FastOutSlowInEasing))
                labels.items.remove(label)
            }
            var width by remember { mutableIntStateOf(0) }
            var height by remember { mutableIntStateOf(0) }
            Text(
                text = label.text,
                style = TextStyle(
                    fontSize = if (label.big) 26.sp else 19.sp,
                    fontWeight = FontWeight.Black,
                    color = label.color,
                    shadow = Shadow(Color.Black.copy(alpha = 0.55f), Offset(0f, 3f), 8f),
                ),
                modifier = Modifier
                    .onSizeChanged {
                        width = it.width
                        height = it.height
                    }
                    .graphicsLayer {
                        val p = progress.value
                        // Keep labels for edge cards fully on screen.
                        val margin = 8.dp.toPx()
                        val maxX = (overlayWidth - width - margin).coerceAtLeast(margin)
                        translationX = (label.position.x - width / 2f).coerceIn(margin, maxX)
                        translationY = label.position.y - height / 2f - p * 110f
                        alpha = if (p < 0.7f) 1f else 1f - (p - 0.7f) / 0.3f
                        val pop = if (p < 0.15f) 0.6f + p / 0.15f * 0.55f else 1.15f - (p - 0.15f) * 0.18f
                        scaleX = pop
                        scaleY = pop
                    },
            )
        }
    }
}

/** Slowly drifting colour blobs and twinkling stars behind the menus. */
@Composable
fun AnimatedBackdrop(
    reducedMotion: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "backdrop")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26_000, easing = LinearEasing), RepeatMode.Restart),
        label = "backdropTime",
    )
    val stars = remember {
        val random = Random(7)
        List(46) { Triple(Offset(random.nextFloat(), random.nextFloat()), random.nextFloat() * 6.28f, 1f + random.nextFloat() * 2.2f) }
    }
    val isDark = scheme.background.luminanceApprox() < 0.5f
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                val t = if (reducedMotion) 0.15f else time
                drawRect(scheme.background)
                val blobs = listOf(
                    Triple(scheme.primary, 0.0f, 0.62f),
                    Triple(scheme.tertiary, 2.1f, 0.5f),
                    Triple(scheme.secondary, 4.2f, 0.45f),
                )
                blobs.forEach { (color, phase, scale) ->
                    val angle = t * 2 * PI.toFloat() + phase
                    val c = Offset(
                        size.width * (0.5f + 0.32f * cos(angle)),
                        size.height * (0.38f + 0.22f * sin(angle * 1.3f)),
                    )
                    val radius = size.maxDimension * scale
                    drawCircle(
                        Brush.radialGradient(listOf(color.copy(alpha = if (isDark) 0.26f else 0.16f), Color.Transparent), c, radius),
                        radius,
                        c,
                    )
                }
                if (isDark) {
                    stars.forEach { (position, phase, radius) ->
                        val twinkle = 0.35f + 0.65f * ((sin(t * 2 * PI.toFloat() * 6 + phase) + 1f) / 2f)
                        drawCircle(Color.White.copy(alpha = 0.55f * twinkle), radius, Offset(position.x * size.width, position.y * size.height))
                    }
                }
            },
        content = content,
    )
}

private fun Color.luminanceApprox(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue

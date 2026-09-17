package com.abhinav.sipplanner.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhinav.sipplanner.core.finance.ProjectionPoint
import com.abhinav.sipplanner.ui.theme.SipTheme
import kotlin.math.abs

/**
 * The app's signature visual: corpus over time, split into the part you paid in
 * (teal, bottom) and the part compounding produced (marigold, on top).
 *
 * Drawn directly on Canvas rather than pulled from a charting library. That keeps
 * the dependency list short, keeps full control of the two-band language, and is
 * a more interesting thing to be able to explain.
 *
 * The draw-in animation is the app's one piece of non-user-triggered motion — it
 * runs when a projection first resolves and not on every recomposition.
 */
@Composable
fun GrowthChart(
    points: List<ProjectionPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    targetLine: Double? = null,
    animate: Boolean = true,
    onScrub: ((ProjectionPoint?) -> Unit)? = null,
) {
    val colors = SipTheme.colors

    // Keyed on the shape of the data, so changing a slider redraws instantly
    // instead of replaying a 900ms animation on every keystroke.
    var started by remember { mutableStateOf(!animate) }
    LaunchedEffect(points.size) { started = true }

    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 900, easing = LinearOutSlowInEasing),
        label = "growth-draw-in",
    )

    var scrubX by remember { mutableStateOf<Float?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .then(
                if (onScrub == null) {
                    Modifier
                } else {
                    Modifier.pointerInput(points) {
                        detectHorizontalDragGestures(
                            onDragStart = { scrubX = it.x },
                            onDragEnd = { scrubX = null; onScrub(null) },
                            onDragCancel = { scrubX = null; onScrub(null) },
                        ) { change, _ ->
                            scrubX = change.position.x
                            val maxMonth = points.lastOrNull()?.monthIndex?.coerceAtLeast(1) ?: 1
                            val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                            val targetMonth = (ratio * maxMonth).toInt()
                            val point = points.minByOrNull { abs(it.monthIndex - targetMonth) }
                            onScrub(point)
                        }
                    }
                },
            ),
    ) {
        Canvas(Modifier.fillMaxWidth().height(height)) {
            if (points.size < 2) return@Canvas

            val maxValue = maxOf(
                points.maxOf { it.value },
                targetLine ?: 0.0,
            ).coerceAtLeast(1.0)

            val visibleCount = (points.size * progress).toInt().coerceAtLeast(2)
            val visible = points.take(visibleCount)

            val paddingPx = 4.dp.toPx()
            val topPaddingPx = 8.dp.toPx()
            val usableWidth = size.width - (paddingPx * 2)
            val usableHeight = size.height - topPaddingPx - paddingPx

            val maxMonth = points.lastOrNull()?.monthIndex?.coerceAtLeast(1) ?: 1
            fun xAt(monthIndex: Int) = paddingPx + ((monthIndex.toFloat() / maxMonth.toFloat()) * usableWidth)
            fun yAt(value: Double) = size.height - paddingPx - (((value / maxValue) * usableHeight).toFloat())

            drawBaseline(colors.hairline, paddingPx = paddingPx)

            // Band 1 — total corpus, filled in marigold. Drawn first so the
            // principal band sits on top of it and the marigold reads as "the
            // extra above what I paid".
            val corpusPath = areaPath(
                visible.map { xAt(it.monthIndex) to yAt(it.value) },
                bottom = size.height - paddingPx,
            )
            drawPath(
                path = corpusPath,
                brush = Brush.verticalGradient(
                    listOf(colors.returns.copy(alpha = 0.55f), colors.returns.copy(alpha = 0.08f)),
                ),
            )

            // Band 2 — money actually contributed, solid teal.
            val investedPath = areaPath(
                visible.map { xAt(it.monthIndex) to yAt(it.invested) },
                bottom = size.height - paddingPx,
            )
            drawPath(
                path = investedPath,
                brush = Brush.verticalGradient(
                    listOf(colors.principal.copy(alpha = 0.85f), colors.principal.copy(alpha = 0.45f)),
                ),
            )

            // Crisp top edge on the corpus curve.
            drawPath(
                path = linePath(visible.map { xAt(it.monthIndex) to yAt(it.value) }),
                color = colors.returns,
                style = Stroke(width = 2.5.dp.toPx()),
            )

            targetLine?.let { target ->
                val y = yAt(target)
                drawLine(
                    color = colors.shortfall,
                    start = Offset(paddingPx, y),
                    end = Offset(size.width - paddingPx, y),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(10.dp.toPx(), 8.dp.toPx()),
                    ),
                )
            }

            scrubX?.let { x ->
                val clamped = x.coerceIn(paddingPx, size.width - paddingPx)
                drawLine(
                    color = colors.muted,
                    start = Offset(clamped, topPaddingPx),
                    end = Offset(clamped, size.height - paddingPx),
                    strokeWidth = 1.dp.toPx(),
                )
                val ratio = ((clamped - paddingPx) / usableWidth).coerceIn(0f, 1f)
                val targetMonth = (ratio * maxMonth).toInt()
                val point = points.minByOrNull { abs(it.monthIndex - targetMonth) }
                point?.let { p ->
                    drawCircle(
                        color = colors.returns,
                        radius = 5.dp.toPx(),
                        center = Offset(xAt(p.monthIndex), yAt(p.value)),
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBaseline(color: Color, paddingPx: Float) {
    drawLine(
        color = color,
        start = Offset(paddingPx, size.height - paddingPx),
        end = Offset(size.width - paddingPx, size.height - paddingPx),
        strokeWidth = 1.dp.toPx(),
    )
}

/** A closed shape from the curve down to the baseline, for the filled band. */
private fun areaPath(coords: List<Pair<Float, Float>>, bottom: Float): Path = Path().apply {
    if (coords.isEmpty()) return@apply
    moveTo(coords.first().first, bottom)
    coords.forEach { (x, y) -> lineTo(x, y) }
    lineTo(coords.last().first, bottom)
    close()
}

private fun linePath(coords: List<Pair<Float, Float>>): Path = Path().apply {
    coords.forEachIndexed { index, (x, y) ->
        if (index == 0) moveTo(x, y) else lineTo(x, y)
    }
}

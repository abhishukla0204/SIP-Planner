package com.abhinav.sipplanner.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhinav.sipplanner.domain.model.NavPoint
import com.abhinav.sipplanner.ui.theme.SipTheme

/**
 * A single-series NAV line. Deliberately *not* two-band — a NAV has no principal
 * and returns to split, so borrowing that language here would be dishonest.
 * It uses the marigold return colour alone.
 */
@Composable
fun NavHistoryChart(
    points: List<NavPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    maxSamples: Int = 240,
) {
    val colors = SipTheme.colors

    // mfapi returns newest-first and can hand back 5,000 entries; reverse and thin.
    val series = remember(points) {
        if (points.isEmpty()) {
            emptyList()
        } else {
            val ordered = points.sortedBy { it.date }
            if (ordered.size <= maxSamples) {
                ordered
            } else {
                val step = ordered.size / maxSamples
                ordered.filterIndexed { index, _ -> index % step == 0 }
            }
        }
    }

    Canvas(modifier.fillMaxWidth().height(height)) {
        if (series.size < 2) return@Canvas

        val minNav = series.minOf { it.nav }
        val maxNav = series.maxOf { it.nav }
        val span = (maxNav - minNav).takeIf { it > 0 } ?: 1.0

        val stepX = size.width / (series.size - 1).toFloat()
        fun yAt(nav: Double) = size.height - ((nav - minNav) / span * size.height * 0.9f).toFloat() -
            size.height * 0.05f

        val line = Path().apply {
            series.forEachIndexed { index, point ->
                val x = index * stepX
                val y = yAt(point.nav)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        val area = Path().apply {
            addPath(line)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(
            path = area,
            brush = Brush.verticalGradient(
                listOf(colors.returns.copy(alpha = 0.35f), colors.returns.copy(alpha = 0.02f)),
            ),
        )
        drawPath(path = line, color = colors.returns, style = Stroke(width = 2.dp.toPx()))
    }
}

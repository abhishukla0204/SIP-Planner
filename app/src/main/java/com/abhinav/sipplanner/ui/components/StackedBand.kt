package com.abhinav.sipplanner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abhinav.sipplanner.ui.theme.SipTheme

/**
 * The same two-band idea as the chart, flattened into a bar. Teal for principal,
 * marigold for returns, hollow for whatever is still missing.
 *
 * Reused as the goal progress bar, the portfolio split, and the composition strip
 * on the calculator — one motif, so a user learns to read it once.
 */
@Composable
fun StackedBand(
    principal: Double,
    returns: Double,
    total: Double,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    showShortfall: Boolean = false,
) {
    val colors = SipTheme.colors
    val safeTotal = total.coerceAtLeast(1.0)

    val principalShare by animateFloatAsState(
        targetValue = (principal / safeTotal).coerceIn(0.0, 1.0).toFloat(),
        animationSpec = tween(600),
        label = "band-principal",
    )
    val returnsShare by animateFloatAsState(
        targetValue = (returns / safeTotal).coerceIn(0.0, 1.0).toFloat(),
        animationSpec = tween(600),
        label = "band-returns",
    )

    Canvas(modifier.fillMaxWidth().height(height)) {
        val radius = CornerRadius(size.height / 2f, size.height / 2f)

        drawRoundRect(color = colors.sunken, cornerRadius = radius)

        val principalWidth = size.width * principalShare
        val returnsWidth = size.width * returnsShare

        if (principalWidth + returnsWidth > 0f) {
            drawRoundRect(
                color = if (showShortfall) colors.shortfall else colors.principal,
                size = Size((principalWidth + returnsWidth).coerceAtLeast(size.height), size.height),
                cornerRadius = radius,
            )
        }
        if (returnsWidth > 0f && !showShortfall) {
            drawRoundRect(
                color = colors.returns,
                topLeft = Offset(principalWidth, 0f),
                size = Size(returnsWidth, size.height),
                cornerRadius = CornerRadius(0f, 0f),
            )
            // Round off the right end of the marigold segment.
            drawRoundRect(
                color = colors.returns,
                topLeft = Offset(principalWidth + returnsWidth - size.height, 0f),
                size = Size(size.height, size.height),
                cornerRadius = radius,
            )
        }
    }
}

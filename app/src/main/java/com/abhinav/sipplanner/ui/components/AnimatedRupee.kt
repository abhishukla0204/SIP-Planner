package com.abhinav.sipplanner.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.abhinav.sipplanner.core.format.Money

/**
 * A rupee figure that counts up to its new value instead of snapping.
 *
 * This is motion in response to a user action — moving a slider — which is the
 * kind worth having: it shows *that* the number moved and roughly how far.
 */
@Composable
fun AnimatedRupee(
    amount: Double,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
) {
    val animated by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 420),
        label = "rupee-count",
    )
    Text(
        text = if (compact) {
            Money.compact(animated.toDouble())
        } else {
            Money.rupees(animated.toDouble())
        },
        modifier = modifier,
        style = style,
        color = color,
        maxLines = 1,
    )
}

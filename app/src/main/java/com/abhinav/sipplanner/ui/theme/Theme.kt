package com.abhinav.sipplanner.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The two data colours don't belong in Material's ColorScheme — they aren't
 * "primary" or "secondary", they're semantic. They ride alongside it instead, so
 * a chart can'token accidentally pick up a theme colour that means something else.
 */
data class SipColors(
    val principal: Color,
    val returns: Color,
    val shortfall: Color,
    val muted: Color,
    val hairline: Color,
    val raised: Color,
    val sunken: Color,
)

val LocalSipColors = staticCompositionLocalOf {
    SipColors(
        principal = Palette.Principal,
        returns = Palette.Returns,
        shortfall = Palette.Shortfall,
        muted = Palette.Mist,
        hairline = Palette.Hairline,
        raised = Palette.PaperRaised,
        sunken = Palette.PaperSunken,
    )
}

private val LightScheme = lightColorScheme(
    primary = Palette.Ink,
    onPrimary = Palette.Paper,
    secondary = Palette.Principal,
    onSecondary = Color.White,
    tertiary = Palette.Returns,
    background = Palette.Paper,
    onBackground = Palette.Ink,
    surface = Palette.PaperRaised,
    onSurface = Palette.Ink,
    surfaceVariant = Palette.PaperSunken,
    onSurfaceVariant = Palette.Mist,
    error = Palette.Shortfall,
    outline = Palette.Mist,
)

private val DarkScheme = darkColorScheme(
    primary = Palette.Paper,
    onPrimary = Palette.Ink,
    secondary = Palette.Principal,
    onSecondary = Color.White,
    tertiary = Palette.Returns,
    background = Palette.Ink,
    onBackground = Palette.Paper,
    surface = Palette.InkRaised,
    onSurface = Palette.Paper,
    surfaceVariant = Palette.InkSunken,
    onSurfaceVariant = Palette.MistOnDark,
    error = Palette.Shortfall,
    outline = Palette.MistOnDark,
)

/**
 * Note: dynamic colour is deliberately switched off. The whole point of the
 * slate/emerald pairing is that it means something; letting the wallpaper
 * recolour it would destroy the only piece of information the chart encodes.
 */
@Composable
fun SipPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val sipColors = if (darkTheme) {
        SipColors(
            principal = Palette.Principal,
            returns = Palette.Returns,
            shortfall = Palette.Shortfall,
            muted = Palette.MistOnDark,
            hairline = Palette.HairlineOnDark,
            raised = Palette.InkRaised,
            sunken = Palette.InkSunken,
        )
    } else {
        SipColors(
            principal = Palette.Principal,
            returns = Palette.Returns,
            shortfall = Palette.Shortfall,
            muted = Palette.Mist,
            hairline = Palette.Hairline,
            raised = Palette.PaperRaised,
            sunken = Palette.PaperSunken,
        )
    }

    CompositionLocalProvider(LocalSipColors provides sipColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            typography = SipTypography,
            content = content,
        )
    }
}

/** Shorthand so screens can write `SipTheme.colors.returns`. */
object SipTheme {
    val colors: SipColors
        @Composable get() = LocalSipColors.current
}

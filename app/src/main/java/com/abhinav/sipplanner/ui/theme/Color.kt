package com.abhinav.sipplanner.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Six named values, and everything in the app is built from them.
 *
 * The pairing that carries the whole product is Principal (teal, money you put
 * in) against Returns (marigold, money compounding added). Those two colours are
 * never used for anything else, so a marigold band always means the same thing
 * wherever it appears.
 */
object Palette {
    val Paper = Color(0xFFF2F1F6)      // cool lilac-grey base
    val Ink = Color(0xFF1B1B3A)        // deep indigo — text on light, base on dark
    val Principal = Color(0xFF3E7C7B)  // muted teal
    val Returns = Color(0xFFE8A33D)    // marigold
    val Shortfall = Color(0xFFB8455E)  // deep rose
    val Mist = Color(0xFF6E6E8F)       // lilac-grey, secondary text

    // Derived tints. Kept here so no screen invents its own one-off colour.
    val PaperRaised = Color(0xFFFAFAFD)
    val PaperSunken = Color(0xFFE7E6EF)
    val InkRaised = Color(0xFF26264A)
    val InkSunken = Color(0xFF13132C)
    val MistOnDark = Color(0xFF9E9EC4)
    val Hairline = Color(0x1A1B1B3A)
    val HairlineOnDark = Color(0x1FFFFFFF)
}

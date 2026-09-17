package com.abhinav.sipplanner.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Six named values, and everything in the app is built from them.
 *
 * The pairing that carries the whole product is Principal (slate, money you put
 * in) against Returns (emerald green, money compounding added). Those two colours are
 * never used for anything else, so an emerald band always means the same thing
 * wherever it appears.
 */
object Palette {
    val Paper = Color(0xFFF8FAFC)        // clean slate off-white base
    val Ink = Color(0xFF0F172A)          // deep slate ink — text on light, base on dark
    val Principal = Color(0xFF475569)    // muted slate — invested money
    val Returns = Color(0xFF10B981)      // emerald green — compounding gains
    val Shortfall = Color(0xFFF43F5E)    // rose red — shortfall / losses
    val Mist = Color(0xFF64748B)         // slate secondary text

    // Derived tints & surface shades
    val PaperRaised = Color(0xFFFFFFFF)  // pure white for cards
    val PaperSunken = Color(0xFFE2E8F0)  // slate sunken containers & controls
    val InkRaised = Color(0xFF1E293B)    // dark slate raised surface
    val InkSunken = Color(0xFF0F172A)    // dark slate sunken background
    val MistOnDark = Color(0xFF94A3B8)   // light slate secondary text on dark
    val Hairline = Color(0x1A0F172A)     // subtle hairline border on light
    val HairlineOnDark = Color(0x1FFFFFFF) // subtle hairline border on dark
}

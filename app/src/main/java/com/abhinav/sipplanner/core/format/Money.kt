package com.abhinav.sipplanner.core.format

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Indian money formatting. Two things matter here that a generic formatter gets
 * wrong: the lakh/crore digit grouping (12,34,567 not 1,234,567) and the fact
 * that people read large corpus figures in lakhs and crores, not in full digits.
 */
object Money {

    private val INR: NumberFormat = NumberFormat.getIntegerInstance(Locale("en", "IN"))
    private val dayMonthYear: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
    private val monthYear: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)

    /** "₹12,34,567" — full precision, Indian grouping. */
    fun rupees(amount: Double): String = "₹" + INR.format(amount.roundToLong())

    /** Same but without the symbol, for when the UI draws its own ₹. */
    fun plain(amount: Double): String = INR.format(amount.roundToLong())

    /**
     * "₹1.23 Cr" / "₹45.6 L" / "₹12,000" — what you want on a chart axis or a
     * dashboard tile where the exact rupee is noise.
     */
    fun compact(amount: Double): String {
        val sign = if (amount < 0) "-" else ""
        val value = abs(amount)
        return when {
            value >= 1_00_00_000 -> "$sign₹%.2f Cr".format(value / 1_00_00_000)
            value >= 1_00_000 -> "$sign₹%.2f L".format(value / 1_00_000)
            value >= 1_000 -> "$sign₹%.1fK".format(value / 1_000)
            else -> sign + "₹" + value.roundToLong()
        }
    }

    /** "12.4%" */
    fun percent(value: Double, decimals: Int = 1): String = "%.${decimals}f%%".format(value)

    /** "5 yr 3 mo", "8 months", "12 years" */
    fun duration(months: Int): String {
        val years = months / 12
        val rest = months % 12
        return when {
            years == 0 -> "$rest ${plural(rest, "month")}"
            rest == 0 -> "$years ${plural(years, "year")}"
            else -> "$years yr $rest mo"
        }
    }

    fun date(date: LocalDate): String = date.format(dayMonthYear)

    fun monthLabel(date: LocalDate): String = date.format(monthYear)

    private fun plural(count: Int, word: String) = if (count == 1) word else "${word}s"
}

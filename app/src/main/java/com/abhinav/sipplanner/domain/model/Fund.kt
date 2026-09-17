package com.abhinav.sipplanner.domain.model

import java.time.LocalDate

/** A mutual fund scheme as identified by AMFI's scheme code. */
data class Fund(
    val schemeCode: Int,
    val schemeName: String,
    val fundHouse: String? = null,
    val category: String? = null,
    val schemeType: String? = null,
)

/** One published NAV on one day. */
data class NavPoint(val date: LocalDate, val nav: Double)

/** A fund the user has chosen to follow, plus its most recent NAV. */
data class TrackedFund(
    val fund: Fund,
    val latestNav: NavPoint?,
    val history: List<NavPoint> = emptyList(),
) {
    /** Return over a trailing window, as a percentage. Null when history is too short. */
    fun returnOver(days: Int): Double? {
        val latest = history.firstOrNull() ?: return null
        val past = history.firstOrNull { it.date <= latest.date.minusDays(days.toLong()) }
            ?: return null
        if (past.nav <= 0.0) return null
        return ((latest.nav - past.nav) / past.nav) * 100.0
    }
}

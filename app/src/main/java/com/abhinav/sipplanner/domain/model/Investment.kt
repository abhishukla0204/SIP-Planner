package com.abhinav.sipplanner.domain.model

import java.time.LocalDate

/** A single instalment the user actually paid, logged by hand. */
data class Investment(
    val id: Long = 0,
    val schemeCode: Int,
    val schemeName: String,
    val date: LocalDate,
    val amount: Double,
    val navAtPurchase: Double,
    val goalId: Long? = null,
) {
    val units: Double get() = if (navAtPurchase > 0) amount / navAtPurchase else 0.0
}

/** Everything held in one scheme, rolled up. */
data class Holding(
    val schemeCode: Int,
    val schemeName: String,
    val totalInvested: Double,
    val units: Double,
    val currentNav: Double?,
    val xirrPercent: Double?,
) {
    val currentValue: Double? get() = currentNav?.let { units * it }
    val gain: Double? get() = currentValue?.minus(totalInvested)
    val gainPercent: Double?
        get() = currentValue?.let {
            if (totalInvested <= 0) null else ((it - totalInvested) / totalInvested) * 100.0
        }
}

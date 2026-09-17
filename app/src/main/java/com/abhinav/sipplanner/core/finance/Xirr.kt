package com.abhinav.sipplanner.core.finance

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.pow

/**
 * A dated cash flow.
 *
 * Sign convention matches every spreadsheet XIRR: money leaving your pocket is
 * negative, money coming back (a redemption, or the current value on valuation
 * day) is positive.
 */
data class CashFlow(val date: LocalDate, val amount: Double)

/**
 * Extended internal rate of return for irregularly spaced cash flows.
 *
 * This is the only honest way to score a SIP, because CAGR assumes a single
 * lump sum on day one and a SIP is dozens of instalments at different dates.
 *
 * Solves NPV(rate) = 0 with Newton-Raphson, falling back to bisection when the
 * derivative sends Newton somewhere useless (which happens on volatile flows).
 */
object Xirr {

    private const val DAYS_PER_YEAR = 365.0
    private const val TOLERANCE = 1e-7
    private const val MAX_NEWTON_STEPS = 100
    private const val MAX_BISECTION_STEPS = 300

    /** Lowest rate we'll consider: -99.99%, i.e. almost total loss. */
    private const val MIN_RATE = -0.9999
    private const val MAX_RATE = 1000.0

    /**
     * @return the annualised rate as a percentage, or null when the flows can't
     *         produce one (all same sign, fewer than two flows, no convergence).
     */
    fun calculate(flows: List<CashFlow>): Double? {
        if (flows.size < 2) return null
        if (flows.none { it.amount > 0 } || flows.none { it.amount < 0 }) return null

        val start = flows.minOf { it.date }
        val years = flows.map { ChronoUnit.DAYS.between(start, it.date) / DAYS_PER_YEAR }
        val amounts = flows.map { it.amount }

        newton(amounts, years)?.let { return it * 100.0 }
        return bisect(amounts, years)?.let { it * 100.0 }
    }

    private fun npv(amounts: List<Double>, years: List<Double>, rate: Double): Double {
        val base = 1.0 + rate
        var sum = 0.0
        for (idx in amounts.indices) sum += amounts[idx] / base.pow(years[idx])
        return sum
    }

    private fun dNpv(amounts: List<Double>, years: List<Double>, rate: Double): Double {
        val base = 1.0 + rate
        var sum = 0.0
        for (idx in amounts.indices) {
            val t = years[idx]
            if (t == 0.0) continue
            sum += -t * amounts[idx] / base.pow(t + 1.0)
        }
        return sum
    }

    private fun newton(amounts: List<Double>, years: List<Double>): Double? {
        var rate = 0.1
        repeat(MAX_NEWTON_STEPS) {
            val value = npv(amounts, years, rate)
            if (abs(value) < TOLERANCE) return rate

            val slope = dNpv(amounts, years, rate)
            if (slope == 0.0 || slope.isNaN()) return null

            val next = rate - value / slope
            if (next.isNaN() || next <= MIN_RATE || next > MAX_RATE) return null
            if (abs(next - rate) < TOLERANCE) return next
            rate = next
        }
        return null
    }

    private fun bisect(amounts: List<Double>, years: List<Double>): Double? {
        var low = MIN_RATE
        var high = MAX_RATE
        var fLow = npv(amounts, years, low)
        if (fLow.isNaN()) return null
        if (fLow * npv(amounts, years, high) > 0) return null

        repeat(MAX_BISECTION_STEPS) {
            val mid = (low + high) / 2.0
            val fMid = npv(amounts, years, mid)
            if (abs(fMid) < TOLERANCE) return mid
            if (fLow * fMid < 0) {
                high = mid
            } else {
                low = mid
                fLow = fMid
            }
        }
        return (low + high) / 2.0
    }

    /**
     * Convenience wrapper for the common case: a list of instalments plus the
     * value the holding is worth today.
     */
    fun forSip(
        instalments: List<CashFlow>,
        valuationDate: LocalDate,
        currentValue: Double,
    ): Double? = calculate(
        instalments.map { it.copy(amount = -abs(it.amount)) } +
            CashFlow(valuationDate, abs(currentValue)),
    )
}

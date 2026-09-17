package com.abhinav.sipplanner.core.finance

import kotlin.math.pow

/**
 * Pure-Kotlin SIP mathematics. No Android imports live in this file on purpose:
 * everything here is covered by fast JVM unit tests.
 *
 * Convention used throughout:
 *  - `annualRatePercent` is a nominal annual rate, e.g. 12.0 for 12%.
 *  - The monthly rate is nominal/12, which is what every Indian SIP calculator
 *    (AMFI, the AMCs, Value Research) uses. It is NOT the effective monthly rate.
 *  - SIP instalments are treated as an *annuity due* (paid at the start of the
 *    month) by default, again matching industry convention.
 */
object SipCalculator {

    /** Converts a nominal annual percentage into a per-month decimal rate. */
    fun monthlyRate(annualRatePercent: Double): Double = annualRatePercent / 12.0 / 100.0

    /**
     * Future value of a level SIP.
     *
     * FV = P * [((1+i)^n - 1) / i] * (1+i)   <- the trailing (1+i) is the "due" adjustment
     *
     * Falls back to a plain sum when the rate is zero, which would otherwise divide by 0.
     */
    fun futureValue(
        monthlyAmount: Double,
        annualRatePercent: Double,
        months: Int,
        dueAtStart: Boolean = true,
    ): Double {
        require(months >= 0) { "months must be >= 0" }
        if (monthlyAmount <= 0.0 || months == 0) return 0.0

        val i = monthlyRate(annualRatePercent)
        if (i == 0.0) return monthlyAmount * months

        val growth = ((1.0 + i).pow(months) - 1.0) / i
        return monthlyAmount * growth * if (dueAtStart) (1.0 + i) else 1.0
    }

    /**
     * The monthly instalment needed to reach [targetAmount]. This is [futureValue]
     * rearranged for P, so the two are exact inverses of each other.
     */
    fun requiredMonthly(
        targetAmount: Double,
        annualRatePercent: Double,
        months: Int,
        dueAtStart: Boolean = true,
    ): Double {
        require(months > 0) { "months must be > 0" }
        if (targetAmount <= 0.0) return 0.0

        val i = monthlyRate(annualRatePercent)
        if (i == 0.0) return targetAmount / months

        val growth = ((1.0 + i).pow(months) - 1.0) / i
        return targetAmount / (growth * if (dueAtStart) (1.0 + i) else 1.0)
    }

    /** Future value of a one-time investment held for [years]. */
    fun lumpsumFutureValue(
        principal: Double,
        annualRatePercent: Double,
        years: Double,
    ): Double {
        if (principal <= 0.0) return 0.0
        return principal * (1.0 + annualRatePercent / 100.0).pow(years)
    }

    /**
     * Month-by-month simulation of a step-up (top-up) SIP, where the instalment
     * rises by [annualStepUpPercent] every 12 months.
     *
     * There is no clean closed form once the instalment changes, so this walks the
     * timeline directly. It is also what feeds the growth chart, since the caller
     * gets a value for every single month rather than just the final number.
     */
    fun simulate(
        monthlyAmount: Double,
        annualRatePercent: Double,
        months: Int,
        annualStepUpPercent: Double = 0.0,
        initialLumpsum: Double = 0.0,
        dueAtStart: Boolean = true,
    ): Projection {
        require(months >= 0) { "months must be >= 0" }

        val i = monthlyRate(annualRatePercent)
        var instalment = monthlyAmount.coerceAtLeast(0.0)
        var corpus = initialLumpsum.coerceAtLeast(0.0)
        var invested = initialLumpsum.coerceAtLeast(0.0)

        val points = ArrayList<ProjectionPoint>(months + 1)
        points += ProjectionPoint(monthIndex = 0, invested = invested, value = corpus)

        for (month in 1..months) {
            if (dueAtStart) corpus += instalment
            corpus *= (1.0 + i)
            if (!dueAtStart) corpus += instalment
            invested += instalment

            points += ProjectionPoint(monthIndex = month, invested = invested, value = corpus)

            // Top up the instalment once a full year of contributions is done.
            if (month % 12 == 0 && annualStepUpPercent != 0.0) {
                instalment *= (1.0 + annualStepUpPercent / 100.0)
            }
        }

        return Projection(
            points = points,
            totalInvested = invested,
            finalValue = corpus,
            finalInstalment = instalment,
        )
    }

    /**
     * Compound annual growth rate between two values.
     * Returns null when the inputs can't produce a meaningful rate.
     */
    fun cagr(startValue: Double, endValue: Double, years: Double): Double? {
        if (startValue <= 0.0 || endValue <= 0.0 || years <= 0.0) return null
        return ((endValue / startValue).pow(1.0 / years) - 1.0) * 100.0
    }

    /**
     * What something that costs [costToday] will cost after [years] of inflation.
     * Goal targets should always be inflated before you solve for a SIP amount,
     * otherwise you plan for a number that no longer buys the thing.
     */
    fun inflateCost(costToday: Double, inflationPercent: Double, years: Double): Double =
        costToday * (1.0 + inflationPercent / 100.0).pow(years)

    /** Strips inflation back out, so a future corpus can be read in today's money. */
    fun realValue(futureAmount: Double, inflationPercent: Double, years: Double): Double =
        futureAmount / (1.0 + inflationPercent / 100.0).pow(years)
}

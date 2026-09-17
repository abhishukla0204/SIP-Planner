package com.abhinav.sipplanner.core.finance

/**
 * Turns a goal ("₹40 lakh for a house in 12 years") into a monthly number.
 *
 * The closed-form [SipCalculator.requiredMonthly] only works for a level SIP with
 * nothing already saved. Once you add a step-up or an existing corpus, future value
 * is still *monotonically increasing* in the base instalment, so a binary search
 * converges on the answer quickly and handles every combination with one code path.
 */
object GoalSolver {

    private const val ITERATIONS = 80

    fun solve(
        targetToday: Double,
        years: Double,
        expectedReturnPercent: Double,
        inflationPercent: Double = 0.0,
        annualStepUpPercent: Double = 0.0,
        existingCorpus: Double = 0.0,
        currentMonthly: Double = 0.0,
    ): GoalOutcome {
        require(years > 0) { "years must be > 0" }

        val months = (years * 12).toInt().coerceAtLeast(1)
        val inflatedTarget = SipCalculator.inflateCost(targetToday, inflationPercent, years)

        val required = requiredMonthly(
            target = inflatedTarget,
            months = months,
            expectedReturnPercent = expectedReturnPercent,
            annualStepUpPercent = annualStepUpPercent,
            existingCorpus = existingCorpus,
        )

        val projection = SipCalculator.simulate(
            monthlyAmount = if (currentMonthly > 0.0) currentMonthly else required,
            annualRatePercent = expectedReturnPercent,
            months = months,
            annualStepUpPercent = annualStepUpPercent,
            initialLumpsum = existingCorpus,
        )

        return GoalOutcome(
            inflatedTarget = inflatedTarget,
            requiredMonthly = required,
            projection = projection,
        )
    }

    /**
     * Binary search for the base instalment whose simulated future value hits [target].
     *
     * The upper bound starts at the target itself (paying the whole goal in month one
     * always overshoots) and the interval halves [ITERATIONS] times, which is far more
     * precision than rupees need.
     */
    fun requiredMonthly(
        target: Double,
        months: Int,
        expectedReturnPercent: Double,
        annualStepUpPercent: Double = 0.0,
        existingCorpus: Double = 0.0,
    ): Double {
        if (target <= 0.0 || months <= 0) return 0.0

        // Already funded by what's saved today — no SIP needed.
        val withNoSip = SipCalculator.simulate(
            monthlyAmount = 0.0,
            annualRatePercent = expectedReturnPercent,
            months = months,
            initialLumpsum = existingCorpus,
        ).finalValue
        if (withNoSip >= target) return 0.0

        var low = 0.0
        var high = target.coerceAtLeast(1.0)

        repeat(ITERATIONS) {
            val mid = (low + high) / 2.0
            val value = SipCalculator.simulate(
                monthlyAmount = mid,
                annualRatePercent = expectedReturnPercent,
                months = months,
                annualStepUpPercent = annualStepUpPercent,
                initialLumpsum = existingCorpus,
            ).finalValue

            if (value < target) low = mid else high = mid
        }
        return high
    }

    /**
     * The other direction: given what someone can actually afford, how long until
     * the goal is funded? Returns null if the plan never gets there within [maxMonths].
     */
    fun monthsToReach(
        target: Double,
        monthlyAmount: Double,
        expectedReturnPercent: Double,
        annualStepUpPercent: Double = 0.0,
        existingCorpus: Double = 0.0,
        maxMonths: Int = 12 * 60,
    ): Int? {
        if (target <= 0.0) return 0
        val projection = SipCalculator.simulate(
            monthlyAmount = monthlyAmount,
            annualRatePercent = expectedReturnPercent,
            months = maxMonths,
            annualStepUpPercent = annualStepUpPercent,
            initialLumpsum = existingCorpus,
        )
        return projection.points.firstOrNull { it.value >= target }?.monthIndex
    }
}

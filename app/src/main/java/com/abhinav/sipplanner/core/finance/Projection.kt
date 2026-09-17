package com.abhinav.sipplanner.core.finance

/** One month on the growth timeline. */
data class ProjectionPoint(
    val monthIndex: Int,
    /** Cumulative money put in by the end of this month. */
    val invested: Double,
    /** Corpus value at the end of this month. */
    val value: Double,
) {
    /** The part of [value] that compounding produced rather than you. */
    val gains: Double get() = (value - invested).coerceAtLeast(0.0)
}

/** The full result of a SIP simulation. */
data class Projection(
    val points: List<ProjectionPoint>,
    val totalInvested: Double,
    val finalValue: Double,
    /** The instalment the plan would have reached by the end, after any step-ups. */
    val finalInstalment: Double,
) {
    val totalGains: Double get() = (finalValue - totalInvested).coerceAtLeast(0.0)

    /** Share of the final corpus that came from returns, as 0.0..1.0. */
    val gainsShare: Double
        get() = if (finalValue <= 0.0) 0.0 else (totalGains / finalValue).coerceIn(0.0, 1.0)

    /** Thins the timeline down for charting so we don't hand 480 points to a Canvas. */
    fun sampled(maxPoints: Int = 120): List<ProjectionPoint> {
        if (points.size <= maxPoints) return points
        val step = points.size.toDouble() / maxPoints
        return buildList {
            var acc = 0.0
            while (acc < points.size - 1) {
                add(points[acc.toInt()])
                acc += step
            }
            add(points.last())
        }
    }
}

/** Outcome of solving a goal: what it takes, and whether the current plan gets there. */
data class GoalOutcome(
    /** Target after inflation has been applied. */
    val inflatedTarget: Double,
    /** Base instalment required to land exactly on [inflatedTarget]. */
    val requiredMonthly: Double,
    /** Where the user's *current* plan actually lands. */
    val projection: Projection,
) {
    val surplus: Double get() = projection.finalValue - inflatedTarget
    val isOnTrack: Boolean get() = surplus >= 0.0
    val shortfall: Double get() = (-surplus).coerceAtLeast(0.0)

    /** How far along the plan is, 0.0..1.0, for the progress band. */
    val completion: Double
        get() = if (inflatedTarget <= 0.0) 0.0
        else (projection.finalValue / inflatedTarget).coerceIn(0.0, 1.0)
}

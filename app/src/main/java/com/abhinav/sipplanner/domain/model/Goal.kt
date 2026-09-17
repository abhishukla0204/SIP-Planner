package com.abhinav.sipplanner.domain.model

import java.time.LocalDate

/** A thing the user is saving towards. All money is in rupees. */
data class Goal(
    val id: Long = 0,
    val name: String,
    val icon: GoalIcon = GoalIcon.Star,
    /** What it costs in today's money — inflation is applied at calculation time. */
    val targetToday: Double,
    val targetDate: LocalDate,
    val monthlyContribution: Double,
    val expectedReturnPercent: Double = 12.0,
    val inflationPercent: Double = 6.0,
    val annualStepUpPercent: Double = 0.0,
    /** Anything already set aside for this specific goal. */
    val existingCorpus: Double = 0.0,
    val createdAt: LocalDate = LocalDate.now(),
) {
    val yearsRemaining: Double
        get() {
            val days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), targetDate)
            return (days / 365.0).coerceAtLeast(1.0 / 12.0)
        }
}

/** A small fixed set so goals stay recognisable at a glance in the list. */
enum class GoalIcon { Home, Car, Education, Retirement, Travel, Emergency, Wedding, Star }

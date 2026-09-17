package com.abhinav.sipplanner.finance

import com.abhinav.sipplanner.core.finance.GoalSolver
import com.abhinav.sipplanner.core.finance.SipCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalSolverTest {

    @Test
    fun `binary search agrees with the closed form when there is no step-up`() {
        val closedForm = SipCalculator.requiredMonthly(50_00_000.0, 12.0, 180)
        val searched = GoalSolver.requiredMonthly(50_00_000.0, 180, 12.0)
        assertEquals(closedForm, searched, 1.0)
    }

    @Test
    fun `the solved instalment actually reaches the target`() {
        val required = GoalSolver.requiredMonthly(
            target = 1_00_00_000.0,
            months = 240,
            expectedReturnPercent = 11.0,
            annualStepUpPercent = 8.0,
        )
        val achieved = SipCalculator.simulate(required, 11.0, 240, annualStepUpPercent = 8.0).finalValue
        assertEquals(1_00_00_000.0, achieved, 100.0)
    }

    @Test
    fun `an existing corpus lowers the instalment needed`() {
        val bare = GoalSolver.requiredMonthly(50_00_000.0, 120, 12.0)
        val withCorpus = GoalSolver.requiredMonthly(50_00_000.0, 120, 12.0, existingCorpus = 10_00_000.0)
        assertTrue(withCorpus < bare)
    }

    @Test
    fun `no SIP is needed when the existing corpus already gets there`() {
        val required = GoalSolver.requiredMonthly(
            target = 2_00_000.0,
            months = 120,
            expectedReturnPercent = 12.0,
            existingCorpus = 5_00_000.0,
        )
        assertEquals(0.0, required, 0.001)
    }

    @Test
    fun `inflation pushes the target above its value today`() {
        val outcome = GoalSolver.solve(
            targetToday = 10_00_000.0,
            years = 10.0,
            expectedReturnPercent = 12.0,
            inflationPercent = 6.0,
            currentMonthly = 10_000.0,
        )
        assertTrue(outcome.inflatedTarget > 10_00_000.0)
        assertEquals(17_90_847.0, outcome.inflatedTarget, 1_000.0)
    }

    @Test
    fun `a plan that falls short reports the gap and the fix`() {
        val outcome = GoalSolver.solve(
            targetToday = 1_00_00_000.0,
            years = 10.0,
            expectedReturnPercent = 12.0,
            inflationPercent = 6.0,
            currentMonthly = 5_000.0,
        )
        assertTrue(!outcome.isOnTrack)
        assertTrue(outcome.shortfall > 0)
        assertTrue(outcome.requiredMonthly > 5_000.0)
    }

    @Test
    fun `completion is capped at one for an over-funded goal`() {
        val outcome = GoalSolver.solve(
            targetToday = 1_00_000.0,
            years = 10.0,
            expectedReturnPercent = 12.0,
            currentMonthly = 50_000.0,
        )
        assertEquals(1.0, outcome.completion, 0.0001)
        assertTrue(outcome.isOnTrack)
    }

    @Test
    fun `monthsToReach finds the crossing point`() {
        val months = GoalSolver.monthsToReach(
            target = 10_00_000.0,
            monthlyAmount = 10_000.0,
            expectedReturnPercent = 12.0,
        )
        assertNotNull(months)
        assertTrue(months!! in 60..80)
    }

    @Test
    fun `monthsToReach returns null when the plan never gets there`() {
        val months = GoalSolver.monthsToReach(
            target = 10_00_00_000.0,
            monthlyAmount = 100.0,
            expectedReturnPercent = 5.0,
            maxMonths = 120,
        )
        assertEquals(null, months)
    }
}

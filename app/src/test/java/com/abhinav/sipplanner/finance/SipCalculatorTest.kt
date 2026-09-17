package com.abhinav.sipplanner.finance

import com.abhinav.sipplanner.core.finance.SipCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SipCalculatorTest {

    /**
     * ₹10,000/month at 12% for 10 years. Cross-checked against the standard
     * annuity-due formula, which is what AMC calculators publish.
     */
    @Test
    fun `future value matches the closed form for a level SIP`() {
        val fv = SipCalculator.futureValue(
            monthlyAmount = 10_000.0,
            annualRatePercent = 12.0,
            months = 120,
        )
        assertEquals(23_23_391.0, fv, 500.0)
    }

    @Test
    fun `zero rate degrades to a plain sum`() {
        val fv = SipCalculator.futureValue(5_000.0, 0.0, 24)
        assertEquals(1_20_000.0, fv, 0.01)
    }

    @Test
    fun `requiredMonthly is the exact inverse of futureValue`() {
        val target = 50_00_000.0
        val required = SipCalculator.requiredMonthly(target, 12.0, 180)
        val achieved = SipCalculator.futureValue(required, 12.0, 180)
        assertEquals(target, achieved, 1.0)
    }

    @Test
    fun `simulate agrees with the closed form when there is no step-up`() {
        val closed = SipCalculator.futureValue(15_000.0, 11.0, 96)
        val simulated = SipCalculator.simulate(15_000.0, 11.0, 96).finalValue
        assertEquals(closed, simulated, 1.0)
    }

    @Test
    fun `step-up raises both the contribution and the final corpus`() {
        val level = SipCalculator.simulate(10_000.0, 12.0, 120)
        val stepped = SipCalculator.simulate(10_000.0, 12.0, 120, annualStepUpPercent = 10.0)

        assertTrue(stepped.totalInvested > level.totalInvested)
        assertTrue(stepped.finalValue > level.finalValue)
    }

    @Test
    fun `step-up only kicks in after twelve instalments`() {
        val projection = SipCalculator.simulate(1_000.0, 0.0, 24, annualStepUpPercent = 100.0)
        // Year one: 12 x 1000. Year two at double: 12 x 2000. Total 36,000.
        assertEquals(36_000.0, projection.totalInvested, 0.01)
    }

    @Test
    fun `simulate emits one point per month plus the opening balance`() {
        val projection = SipCalculator.simulate(1_000.0, 10.0, 60)
        assertEquals(61, projection.points.size)
        assertEquals(0, projection.points.first().monthIndex)
        assertEquals(60, projection.points.last().monthIndex)
    }

    /**
     * The lumpsum inside [SipCalculator.simulate] compounds monthly at rate/12,
     * so it must be checked against (1 + rate/12)^months — not against the
     * annual [SipCalculator.lumpsumFutureValue], which compounds once a year and
     * lands about ₹19,000 lower over a decade. Two different conventions, both
     * correct, and worth not conflating.
     */
    @Test
    fun `an initial lumpsum compounds monthly alongside the instalments`() {
        val withoutLumpsum = SipCalculator.simulate(5_000.0, 12.0, 120).finalValue
        val withLumpsum = SipCalculator.simulate(
            monthlyAmount = 5_000.0,
            annualRatePercent = 12.0,
            months = 120,
            initialLumpsum = 1_00_000.0,
        ).finalValue

        val expected = 1_00_000.0 * Math.pow(1.0 + 12.0 / 12.0 / 100.0, 120.0)
        assertEquals(expected, withLumpsum - withoutLumpsum, 1.0)
    }

    /** Guards the distinction above: annual compounding must trail monthly. */
    @Test
    fun `annual lumpsum growth is lower than monthly compounding`() {
        val annual = SipCalculator.lumpsumFutureValue(1_00_000.0, 12.0, 10.0)
        val monthly = 1_00_000.0 * Math.pow(1.01, 120.0)
        assertTrue(annual < monthly)
    }

    @Test
    fun `inflation and real value round-trip`() {
        val inflated = SipCalculator.inflateCost(10_00_000.0, 6.0, 12.0)
        val back = SipCalculator.realValue(inflated, 6.0, 12.0)
        assertEquals(10_00_000.0, back, 0.01)
    }

    @Test
    fun `cagr returns null for impossible inputs`() {
        assertEquals(null, SipCalculator.cagr(0.0, 100.0, 5.0))
        assertEquals(null, SipCalculator.cagr(100.0, 200.0, 0.0))
    }

    @Test
    fun `cagr computes a doubling over ten years`() {
        val rate = SipCalculator.cagr(1_00_000.0, 2_00_000.0, 10.0)!!
        assertEquals(7.177, rate, 0.01)
    }

    @Test
    fun `sampling never exceeds the cap and keeps the endpoints`() {
        val projection = SipCalculator.simulate(1_000.0, 12.0, 480)
        val sampled = projection.sampled(maxPoints = 100)

        assertTrue(sampled.size <= 101)
        assertEquals(0, sampled.first().monthIndex)
        assertEquals(480, sampled.last().monthIndex)
    }
}

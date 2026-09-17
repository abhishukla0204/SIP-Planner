package com.abhinav.sipplanner.finance

import com.abhinav.sipplanner.core.finance.CashFlow
import com.abhinav.sipplanner.core.finance.Xirr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class XirrTest {

    @Test
    fun `a simple doubling over one year is about 100 percent`() {
        val rate = Xirr.calculate(
            listOf(
                CashFlow(LocalDate.of(2024, 1, 1), -1_00_000.0),
                CashFlow(LocalDate.of(2025, 1, 1), 2_00_000.0),
            ),
        )
        assertNotNull(rate)
        assertEquals(100.0, rate!!, 0.5)
    }

    @Test
    fun `flat return over one year is about zero`() {
        val rate = Xirr.calculate(
            listOf(
                CashFlow(LocalDate.of(2024, 1, 1), -50_000.0),
                CashFlow(LocalDate.of(2025, 1, 1), 50_000.0),
            ),
        )!!
        assertEquals(0.0, rate, 0.01)
    }

    @Test
    fun `a loss produces a negative rate`() {
        val rate = Xirr.calculate(
            listOf(
                CashFlow(LocalDate.of(2024, 1, 1), -1_00_000.0),
                CashFlow(LocalDate.of(2025, 1, 1), 80_000.0),
            ),
        )!!
        assertTrue(rate < 0)
        assertEquals(-20.0, rate, 0.5)
    }

    @Test
    fun `a monthly SIP scores close to the underlying rate`() {
        // 24 monthly instalments, redeemed at a value consistent with ~12% a year.
        val start = LocalDate.of(2023, 1, 1)
        val instalments = (0 until 24).map { CashFlow(start.plusMonths(it.toLong()), 10_000.0) }

        val rate = Xirr.forSip(
            instalments = instalments,
            valuationDate = start.plusMonths(24),
            currentValue = 2_71_000.0,
        )!!

        assertTrue("expected a sane double-digit rate, got $rate", rate in 8.0..16.0)
    }

    @Test
    fun `returns null when every flow points the same way`() {
        assertNull(
            Xirr.calculate(
                listOf(
                    CashFlow(LocalDate.of(2024, 1, 1), -1_000.0),
                    CashFlow(LocalDate.of(2025, 1, 1), -1_000.0),
                ),
            ),
        )
    }

    @Test
    fun `returns null for a single cash flow`() {
        assertNull(Xirr.calculate(listOf(CashFlow(LocalDate.of(2024, 1, 1), -1_000.0))))
    }

    @Test
    fun `handles irregular gaps between instalments`() {
        val rate = Xirr.calculate(
            listOf(
                CashFlow(LocalDate.of(2022, 3, 14), -25_000.0),
                CashFlow(LocalDate.of(2022, 9, 2), -40_000.0),
                CashFlow(LocalDate.of(2023, 7, 19), -15_000.0),
                CashFlow(LocalDate.of(2025, 1, 6), 1_10_000.0),
            ),
        )
        assertNotNull(rate)
    }
}

package com.abhinav.sipplanner.ui.screens.calculator

import androidx.lifecycle.ViewModel
import com.abhinav.sipplanner.core.finance.Projection
import com.abhinav.sipplanner.core.finance.SipCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

enum class CalculatorMode { Sip, Lumpsum, Target }

data class CalculatorState(
    val mode: CalculatorMode = CalculatorMode.Sip,
    val monthlyAmount: Double = 10_000.0,
    val lumpsum: Double = 1_00_000.0,
    val targetAmount: Double = 50_00_000.0,
    val years: Int = 15,
    val expectedReturn: Double = 12.0,
    val stepUpPercent: Double = 0.0,
) {
    val months: Int get() = years * 12

    /**
     * Recomputed on every state change. This is cheap — the simulation is a few
     * hundred multiplications — so there's no reason to cache it or push it to a
     * background thread.
     */
    val projection: Projection
        get() = when (mode) {
            CalculatorMode.Sip -> SipCalculator.simulate(
                monthlyAmount = monthlyAmount,
                annualRatePercent = expectedReturn,
                months = months,
                annualStepUpPercent = stepUpPercent,
            )
            CalculatorMode.Lumpsum -> SipCalculator.simulate(
                monthlyAmount = 0.0,
                annualRatePercent = expectedReturn,
                months = months,
                initialLumpsum = lumpsum,
            )
            CalculatorMode.Target -> SipCalculator.simulate(
                monthlyAmount = requiredMonthly,
                annualRatePercent = expectedReturn,
                months = months,
                annualStepUpPercent = stepUpPercent,
            )
        }

    /** Only meaningful in Target mode: the instalment needed to land on target. */
    val requiredMonthly: Double
        get() = SipCalculator.requiredMonthly(
            targetAmount = targetAmount,
            annualRatePercent = expectedReturn,
            months = months,
        )
}

@HiltViewModel
class CalculatorViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun setMode(mode: CalculatorMode) = _state.update { it.copy(mode = mode) }
    fun setMonthly(value: Double) = _state.update { it.copy(monthlyAmount = value) }
    fun setLumpsum(value: Double) = _state.update { it.copy(lumpsum = value) }
    fun setTarget(value: Double) = _state.update { it.copy(targetAmount = value) }
    fun setYears(value: Int) = _state.update { it.copy(years = value) }
    fun setReturn(value: Double) = _state.update { it.copy(expectedReturn = value) }
    fun setStepUp(value: Double) = _state.update { it.copy(stepUpPercent = value) }
}

package com.abhinav.sipplanner.ui.screens.goals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.sipplanner.core.finance.GoalOutcome
import com.abhinav.sipplanner.core.finance.GoalSolver
import com.abhinav.sipplanner.domain.model.Goal
import com.abhinav.sipplanner.domain.model.GoalIcon
import com.abhinav.sipplanner.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddGoalState(
    val id: Long = 0,
    val name: String = "",
    val icon: GoalIcon = GoalIcon.Star,
    val targetToday: Double = 10_00_000.0,
    val years: Int = 10,
    val monthlyContribution: Double = 10_000.0,
    val expectedReturn: Double = 12.0,
    val inflation: Double = 6.0,
    val stepUp: Double = 0.0,
    val existingCorpus: Double = 0.0,
    val saved: Boolean = false,
) {
    val isValid: Boolean get() = name.isNotBlank() && targetToday > 0 && years > 0

    /** Live preview so the user sees the verdict before committing. */
    val outcome: GoalOutcome
        get() = GoalSolver.solve(
            targetToday = targetToday,
            years = years.toDouble(),
            expectedReturnPercent = expectedReturn,
            inflationPercent = inflation,
            annualStepUpPercent = stepUp,
            existingCorpus = existingCorpus,
            currentMonthly = monthlyContribution,
        )
}

@HiltViewModel
class AddGoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(AddGoalState())
    val state: StateFlow<AddGoalState> = _state.asStateFlow()

    init {
        // Non-zero id means we're editing rather than creating.
        savedStateHandle.get<Long>("goalId")?.takeIf { it > 0 }?.let { load(it) }
    }

    private fun load(id: Long) = viewModelScope.launch {
        goalRepository.observeGoal(id).first()?.let { goal ->
            _state.update {
                AddGoalState(
                    id = goal.id,
                    name = goal.name,
                    icon = goal.icon,
                    targetToday = goal.targetToday,
                    years = goal.yearsRemaining.toInt().coerceAtLeast(1),
                    monthlyContribution = goal.monthlyContribution,
                    expectedReturn = goal.expectedReturnPercent,
                    inflation = goal.inflationPercent,
                    stepUp = goal.annualStepUpPercent,
                    existingCorpus = goal.existingCorpus,
                )
            }
        }
    }

    fun setName(value: String) = _state.update { it.copy(name = value) }
    fun setIcon(value: GoalIcon) = _state.update { it.copy(icon = value) }
    fun setTarget(value: Double) = _state.update { it.copy(targetToday = value) }
    fun setYears(value: Int) = _state.update { it.copy(years = value) }
    fun setMonthly(value: Double) = _state.update { it.copy(monthlyContribution = value) }
    fun setReturn(value: Double) = _state.update { it.copy(expectedReturn = value) }
    fun setInflation(value: Double) = _state.update { it.copy(inflation = value) }
    fun setStepUp(value: Double) = _state.update { it.copy(stepUp = value) }
    fun setExistingCorpus(value: Double) = _state.update { it.copy(existingCorpus = value) }

    /** Fills the instalment with whatever actually reaches the target. */
    fun useRequiredAmount() = _state.update { it.copy(monthlyContribution = it.outcome.requiredMonthly) }

    fun save() = viewModelScope.launch {
        val current = _state.value
        if (!current.isValid) return@launch

        goalRepository.upsert(
            Goal(
                id = current.id,
                name = current.name.trim(),
                icon = current.icon,
                targetToday = current.targetToday,
                targetDate = LocalDate.now().plusYears(current.years.toLong()),
                monthlyContribution = current.monthlyContribution,
                expectedReturnPercent = current.expectedReturn,
                inflationPercent = current.inflation,
                annualStepUpPercent = current.stepUp,
                existingCorpus = current.existingCorpus,
            ),
        )
        _state.update { it.copy(saved = true) }
    }
}

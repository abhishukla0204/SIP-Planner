package com.abhinav.sipplanner.ui.screens.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.sipplanner.core.finance.GoalOutcome
import com.abhinav.sipplanner.core.finance.GoalSolver
import com.abhinav.sipplanner.domain.model.Goal
import com.abhinav.sipplanner.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A goal plus the result of solving it, so the list can render without recomputing. */
data class GoalRow(val goal: Goal, val outcome: GoalOutcome)

data class GoalsUiState(
    val rows: List<GoalRow> = emptyList(),
    val loading: Boolean = true,
) {
    val totalMonthly: Double get() = rows.sumOf { it.goal.monthlyContribution }
    val onTrackCount: Int get() = rows.count { it.outcome.isOnTrack }
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
) : ViewModel() {

    val state: StateFlow<GoalsUiState> = goalRepository.observeGoals()
        .map { goals -> GoalsUiState(rows = goals.map { it.solve() }, loading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GoalsUiState(),
        )

    fun delete(id: Long) = viewModelScope.launch { goalRepository.delete(id) }
}

/** Shared by the list and the detail screen so both read the same numbers. */
fun Goal.solve(): GoalRow = GoalRow(
    goal = this,
    outcome = GoalSolver.solve(
        targetToday = targetToday,
        years = yearsRemaining,
        expectedReturnPercent = expectedReturnPercent,
        inflationPercent = inflationPercent,
        annualStepUpPercent = annualStepUpPercent,
        existingCorpus = existingCorpus,
        currentMonthly = monthlyContribution,
    ),
)

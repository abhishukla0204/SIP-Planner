package com.abhinav.sipplanner.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.sipplanner.domain.model.Goal
import com.abhinav.sipplanner.domain.model.Holding
import com.abhinav.sipplanner.domain.model.Investment
import com.abhinav.sipplanner.domain.model.TrackedFund
import com.abhinav.sipplanner.domain.repository.FundRepository
import com.abhinav.sipplanner.domain.repository.GoalRepository
import com.abhinav.sipplanner.domain.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeState(
    val goals: List<Goal> = emptyList(),
    val holdings: List<Holding> = emptyList(),
    val loadingHoldings: Boolean = true,
) {
    val invested: Double get() = holdings.sumOf { it.totalInvested }
    val currentValue: Double get() = holdings.sumOf { it.currentValue ?: it.totalInvested }
    val gain: Double get() = currentValue - invested
    val monthlyCommitment: Double get() = goals.sumOf { it.monthlyContribution }

    /** Portfolio-wide XIRR, weighted by how much sits in each scheme. */
    val blendedXirr: Double?
        get() {
            val scored = holdings.filter { it.xirrPercent != null && it.totalInvested > 0 }
            if (scored.isEmpty()) return null
            val weight = scored.sumOf { it.totalInvested }
            return scored.sumOf { it.xirrPercent!! * it.totalInvested } / weight
        }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val investmentRepository: InvestmentRepository,
    private val fundRepository: FundRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    val trackedFunds: StateFlow<List<TrackedFund>> = fundRepository.observeTracked()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    init {
        viewModelScope.launch {
            goalRepository.observeGoals().collect { goals ->
                _state.update { it.copy(goals = goals) }
            }
        }
        refreshHoldings()
    }

    /** Pulls live NAVs, so it runs on demand rather than on every recomposition. */
    fun refreshHoldings() = viewModelScope.launch {
        _state.update { it.copy(loadingHoldings = true) }
        val holdings = runCatching { investmentRepository.holdings() }.getOrDefault(emptyList())
        _state.update { it.copy(holdings = holdings, loadingHoldings = false) }
    }

    fun addInvestment(
        schemeCode: Int,
        schemeName: String,
        amount: Double,
        navAtPurchase: Double,
        date: LocalDate = LocalDate.now(),
    ) = viewModelScope.launch {
        investmentRepository.add(
            Investment(
                schemeCode = schemeCode,
                schemeName = schemeName,
                amount = amount,
                navAtPurchase = navAtPurchase,
                date = date,
            ),
        )
        refreshHoldings()
    }

    suspend fun searchFunds(query: String) = fundRepository.search(query)

    suspend fun fetchLatestNav(schemeCode: Int) = fundRepository.latestNav(schemeCode)
}

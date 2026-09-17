package com.abhinav.sipplanner.ui.screens.funds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.sipplanner.domain.model.Fund
import com.abhinav.sipplanner.domain.model.TrackedFund
import com.abhinav.sipplanner.domain.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FundSearchState(
    val query: String = "",
    val results: List<Fund> = emptyList(),
    val searching: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class FundSearchViewModel @Inject constructor(
    private val fundRepository: FundRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FundSearchState())
    val state: StateFlow<FundSearchState> = _state.asStateFlow()

    val tracked: StateFlow<List<TrackedFund>> = fundRepository.observeTracked()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(FlowPreview::class)
    private val queries = MutableStateFlow("")

    init {
        // Debounced so typing "hdfc small cap" fires one request, not fourteen.
        viewModelScope.launch {
            queries
                .debounce(350)
                .distinctUntilChanged()
                .collect { runSearch(it) }
        }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        queries.value = value
    }

    private suspend fun runSearch(query: String) {
        if (query.trim().length < 3) {
            _state.update { it.copy(results = emptyList(), searching = false, error = null) }
            return
        }
        _state.update { it.copy(searching = true, error = null) }

        fundRepository.search(query)
            .onSuccess { funds ->
                _state.update { it.copy(results = funds.take(50), searching = false) }
            }
            .onFailure {
                _state.update {
                    it.copy(
                        searching = false,
                        results = emptyList(),
                        error = "Couldn't reach the NAV service. Check your connection and try again.",
                    )
                }
            }
    }

    fun track(fund: Fund) = viewModelScope.launch { fundRepository.track(fund) }
    fun untrack(schemeCode: Int) = viewModelScope.launch { fundRepository.untrack(schemeCode) }
}

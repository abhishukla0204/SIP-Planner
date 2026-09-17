package com.abhinav.sipplanner.ui.screens.funds

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhinav.sipplanner.domain.model.Fund
import com.abhinav.sipplanner.domain.model.NavPoint
import com.abhinav.sipplanner.domain.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FundDetailState(
    val loading: Boolean = true,
    val fund: Fund? = null,
    val history: List<NavPoint> = emptyList(),
    val error: String? = null,
) {
    val latest: NavPoint? get() = history.firstOrNull()

    /** Trailing return over a window, annualised only when the window exceeds a year. */
    fun returnOver(days: Int): Double? {
        val newest = history.firstOrNull() ?: return null
        val older = history.firstOrNull { it.date <= newest.date.minusDays(days.toLong()) }
            ?: return null
        if (older.nav <= 0.0) return null

        val simple = ((newest.nav - older.nav) / older.nav) * 100.0
        if (days <= 365) return simple

        val years = days / 365.0
        return (Math.pow(newest.nav / older.nav, 1.0 / years) - 1.0) * 100.0
    }
}

@HiltViewModel
class FundDetailViewModel @Inject constructor(
    private val fundRepository: FundRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val schemeCode: Int = savedStateHandle.get<Int>("schemeCode") ?: 0

    private val _state = MutableStateFlow(FundDetailState())
    val state: StateFlow<FundDetailState> = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        fundRepository.navHistory(schemeCode)
            .onSuccess { (fund, history) ->
                _state.update { it.copy(loading = false, fund = fund, history = history) }
            }
            .onFailure {
                _state.update {
                    it.copy(
                        loading = false,
                        error = "Couldn't load NAV history for this scheme.",
                    )
                }
            }
    }

    fun track() = viewModelScope.launch {
        _state.value.fund?.let { fundRepository.track(it) }
    }
}

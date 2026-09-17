package com.abhinav.sipplanner.domain.repository

import com.abhinav.sipplanner.domain.model.Fund
import com.abhinav.sipplanner.domain.model.NavPoint
import com.abhinav.sipplanner.domain.model.TrackedFund
import kotlinx.coroutines.flow.Flow

interface FundRepository {
    /** Hits api.mfapi.in. Returns a failure rather than throwing, so the UI can show it. */
    suspend fun search(query: String): Result<List<Fund>>
    suspend fun navHistory(schemeCode: Int): Result<Pair<Fund, List<NavPoint>>>
    suspend fun latestNav(schemeCode: Int): Result<NavPoint>

    fun observeTracked(): Flow<List<TrackedFund>>
    suspend fun track(fund: Fund)
    suspend fun untrack(schemeCode: Int)
}

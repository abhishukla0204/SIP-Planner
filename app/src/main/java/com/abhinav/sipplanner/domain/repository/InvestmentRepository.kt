package com.abhinav.sipplanner.domain.repository

import com.abhinav.sipplanner.domain.model.Holding
import com.abhinav.sipplanner.domain.model.Investment
import kotlinx.coroutines.flow.Flow

interface InvestmentRepository {
    fun observeAll(): Flow<List<Investment>>
    fun observeForGoal(goalId: Long): Flow<List<Investment>>
    suspend fun add(investment: Investment)
    suspend fun delete(id: Long)
    /** Rolls instalments up per scheme and prices them with the latest NAV. */
    suspend fun holdings(): List<Holding>
}

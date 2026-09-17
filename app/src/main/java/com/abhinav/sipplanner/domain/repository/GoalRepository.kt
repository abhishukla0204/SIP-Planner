package com.abhinav.sipplanner.domain.repository

import com.abhinav.sipplanner.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun observeGoals(): Flow<List<Goal>>
    fun observeGoal(id: Long): Flow<Goal?>
    suspend fun upsert(goal: Goal): Long
    suspend fun delete(id: Long)
}

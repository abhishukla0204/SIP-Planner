package com.abhinav.sipplanner.data.repository

import com.abhinav.sipplanner.data.local.dao.GoalDao
import com.abhinav.sipplanner.data.mapper.toDomain
import com.abhinav.sipplanner.data.mapper.toEntity
import com.abhinav.sipplanner.domain.model.Goal
import com.abhinav.sipplanner.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
) : GoalRepository {

    override fun observeGoals(): Flow<List<Goal>> =
        goalDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeGoal(id: Long): Flow<Goal?> =
        goalDao.observeById(id).map { it?.toDomain() }

    override suspend fun upsert(goal: Goal): Long = goalDao.upsert(goal.toEntity())

    override suspend fun delete(id: Long) = goalDao.deleteById(id)
}

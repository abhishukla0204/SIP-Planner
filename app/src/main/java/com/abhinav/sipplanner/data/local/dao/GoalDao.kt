package com.abhinav.sipplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abhinav.sipplanner.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY targetDate ASC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: Long): Flow<GoalEntity?>

    @Upsert
    suspend fun upsert(goal: GoalEntity): Long

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)
}

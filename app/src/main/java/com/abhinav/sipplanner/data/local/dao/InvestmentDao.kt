package com.abhinav.sipplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.abhinav.sipplanner.data.local.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY date DESC")
    fun observeAll(): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE goalId = :goalId ORDER BY date DESC")
    fun observeForGoal(goalId: Long): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments ORDER BY date ASC")
    suspend fun all(): List<InvestmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(investment: InvestmentEntity)

    @Query("DELETE FROM investments WHERE id = :id")
    suspend fun deleteById(id: Long)
}

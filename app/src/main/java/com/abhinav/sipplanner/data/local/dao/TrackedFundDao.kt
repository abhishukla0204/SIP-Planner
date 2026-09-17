package com.abhinav.sipplanner.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.abhinav.sipplanner.data.local.entity.TrackedFundEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedFundDao {
    @Query("SELECT * FROM tracked_funds ORDER BY schemeName ASC")
    fun observeAll(): Flow<List<TrackedFundEntity>>

    @Query("SELECT * FROM tracked_funds WHERE schemeCode = :code")
    suspend fun byCode(code: Int): TrackedFundEntity?

    @Upsert
    suspend fun upsert(fund: TrackedFundEntity)

    @Query("UPDATE tracked_funds SET cachedNav = :nav, cachedNavDate = :date WHERE schemeCode = :code")
    suspend fun updateNav(code: Int, nav: Double, date: Long)

    @Query("DELETE FROM tracked_funds WHERE schemeCode = :code")
    suspend fun deleteByCode(code: Int)
}

package com.abhinav.sipplanner.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.abhinav.sipplanner.data.local.dao.GoalDao
import com.abhinav.sipplanner.data.local.dao.InvestmentDao
import com.abhinav.sipplanner.data.local.dao.TrackedFundDao
import com.abhinav.sipplanner.data.local.entity.GoalEntity
import com.abhinav.sipplanner.data.local.entity.InvestmentEntity
import com.abhinav.sipplanner.data.local.entity.TrackedFundEntity

@Database(
    entities = [GoalEntity::class, InvestmentEntity::class, TrackedFundEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class SipDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun investmentDao(): InvestmentDao
    abstract fun trackedFundDao(): TrackedFundDao

    companion object {
        const val NAME = "sip_planner.db"
    }
}

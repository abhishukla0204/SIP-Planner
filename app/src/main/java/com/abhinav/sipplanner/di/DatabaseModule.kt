package com.abhinav.sipplanner.di

import android.content.Context
import androidx.room.Room
import com.abhinav.sipplanner.data.local.SipDatabase
import com.abhinav.sipplanner.data.local.dao.GoalDao
import com.abhinav.sipplanner.data.local.dao.InvestmentDao
import com.abhinav.sipplanner.data.local.dao.TrackedFundDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SipDatabase =
        Room.databaseBuilder(context, SipDatabase::class.java, SipDatabase.NAME)
            // Fine for v1. Replace with a real Migration before you ever ship an update.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideGoalDao(db: SipDatabase): GoalDao = db.goalDao()
    @Provides fun provideInvestmentDao(db: SipDatabase): InvestmentDao = db.investmentDao()
    @Provides fun provideTrackedFundDao(db: SipDatabase): TrackedFundDao = db.trackedFundDao()
}

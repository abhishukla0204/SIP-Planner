package com.abhinav.sipplanner.di

import com.abhinav.sipplanner.data.repository.FundRepositoryImpl
import com.abhinav.sipplanner.data.repository.GoalRepositoryImpl
import com.abhinav.sipplanner.data.repository.InvestmentRepositoryImpl
import com.abhinav.sipplanner.domain.repository.FundRepository
import com.abhinav.sipplanner.domain.repository.GoalRepository
import com.abhinav.sipplanner.domain.repository.InvestmentRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/** Binds each repository interface to its implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds @Singleton
    abstract fun bindFundRepository(impl: FundRepositoryImpl): FundRepository

    @Binds @Singleton
    abstract fun bindInvestmentRepository(impl: InvestmentRepositoryImpl): InvestmentRepository
}

/**
 * Dispatchers are injected rather than referenced directly so tests can swap in
 * a deterministic one.
 */
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

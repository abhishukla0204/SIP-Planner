package com.abhinav.sipplanner.data.repository

import com.abhinav.sipplanner.data.local.dao.TrackedFundDao
import com.abhinav.sipplanner.data.mapper.toDomain
import com.abhinav.sipplanner.data.mapper.toDomainOrNull
import com.abhinav.sipplanner.data.mapper.toEntity
import com.abhinav.sipplanner.data.mapper.toFund
import com.abhinav.sipplanner.data.remote.MfApi
import com.abhinav.sipplanner.domain.model.Fund
import com.abhinav.sipplanner.domain.model.NavPoint
import com.abhinav.sipplanner.domain.model.TrackedFund
import com.abhinav.sipplanner.domain.repository.FundRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FundRepositoryImpl @Inject constructor(
    private val api: MfApi,
    private val trackedFundDao: TrackedFundDao,
    private val io: CoroutineDispatcher,
) : FundRepository {

    /**
     * Every network call is wrapped in runCatching so a dead connection surfaces as
     * a Result the UI can render, instead of an exception that kills the ViewModel.
     */
    override suspend fun search(query: String): Result<List<Fund>> = withContext(io) {
        if (query.isBlank()) return@withContext Result.success(emptyList())
        runCatching { api.search(query.trim()).map { it.toDomain() } }
    }

    override suspend fun navHistory(schemeCode: Int): Result<Pair<Fund, List<NavPoint>>> =
        withContext(io) {
            runCatching {
                val dto = api.scheme(schemeCode)
                val points = dto.data.mapNotNull { it.toDomainOrNull() }
                if (points.isEmpty()) throw IOException("No NAV history for scheme $schemeCode")
                dto.toFund(schemeCode) to points
            }
        }

    override suspend fun latestNav(schemeCode: Int): Result<NavPoint> = withContext(io) {
        runCatching {
            api.latest(schemeCode).data.firstOrNull()?.toDomainOrNull()
                ?: throw IOException("No latest NAV for scheme $schemeCode")
        }
    }

    override fun observeTracked(): Flow<List<TrackedFund>> =
        trackedFundDao.observeAll().map { rows ->
            rows.map { row ->
                TrackedFund(
                    fund = row.toFund(),
                    latestNav = row.cachedNav?.let { nav ->
                        row.cachedNavDate?.let { NavPoint(LocalDate.ofEpochDay(it), nav) }
                    },
                )
            }
        }

    /** Stores the fund, then opportunistically warms the NAV cache. */
    override suspend fun track(fund: Fund) = withContext(io) {
        trackedFundDao.upsert(fund.toEntity())
        latestNav(fund.schemeCode).getOrNull()?.let {
            trackedFundDao.updateNav(fund.schemeCode, it.nav, it.date.toEpochDay())
        }
        Unit
    }

    override suspend fun untrack(schemeCode: Int) = withContext(io) {
        trackedFundDao.deleteByCode(schemeCode)
    }
}

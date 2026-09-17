package com.abhinav.sipplanner.data.repository

import com.abhinav.sipplanner.core.finance.CashFlow
import com.abhinav.sipplanner.core.finance.Xirr
import com.abhinav.sipplanner.data.local.dao.InvestmentDao
import com.abhinav.sipplanner.data.mapper.toDomain
import com.abhinav.sipplanner.data.mapper.toEntity
import com.abhinav.sipplanner.domain.model.Holding
import com.abhinav.sipplanner.domain.model.Investment
import com.abhinav.sipplanner.domain.repository.FundRepository
import com.abhinav.sipplanner.domain.repository.InvestmentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestmentRepositoryImpl @Inject constructor(
    private val investmentDao: InvestmentDao,
    private val fundRepository: FundRepository,
    private val io: CoroutineDispatcher,
) : InvestmentRepository {

    override fun observeAll(): Flow<List<Investment>> =
        investmentDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun observeForGoal(goalId: Long): Flow<List<Investment>> =
        investmentDao.observeForGoal(goalId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun add(investment: Investment) = investmentDao.insert(investment.toEntity())

    override suspend fun delete(id: Long) = investmentDao.deleteById(id)

    /**
     * Groups instalments by scheme, prices each holding with today's NAV, and scores
     * it with XIRR. XIRR rather than CAGR because the instalments are irregular.
     */
    override suspend fun holdings(): List<Holding> = withContext(io) {
        val all = investmentDao.all().map { it.toDomain() }
        val today = LocalDate.now()

        all.groupBy { it.schemeCode }.map { (schemeCode, rows) ->
            val invested = rows.sumOf { it.amount }
            val units = rows.sumOf { it.units }
            val nav = fundRepository.latestNav(schemeCode).getOrNull()?.nav

            val xirr = nav?.let {
                Xirr.forSip(
                    instalments = rows.map { row -> CashFlow(row.date, row.amount) },
                    valuationDate = today,
                    currentValue = units * it,
                )
            }

            Holding(
                schemeCode = schemeCode,
                schemeName = rows.first().schemeName,
                totalInvested = invested,
                units = units,
                currentNav = nav,
                xirrPercent = xirr,
            )
        }.sortedByDescending { it.totalInvested }
    }
}

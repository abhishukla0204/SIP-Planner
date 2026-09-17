package com.abhinav.sipplanner.data.mapper

import com.abhinav.sipplanner.data.local.entity.GoalEntity
import com.abhinav.sipplanner.data.local.entity.InvestmentEntity
import com.abhinav.sipplanner.data.local.entity.TrackedFundEntity
import com.abhinav.sipplanner.data.remote.dto.NavEntryDto
import com.abhinav.sipplanner.data.remote.dto.SchemeDetailDto
import com.abhinav.sipplanner.data.remote.dto.SchemeSearchDto
import com.abhinav.sipplanner.domain.model.Fund
import com.abhinav.sipplanner.domain.model.Goal
import com.abhinav.sipplanner.domain.model.GoalIcon
import com.abhinav.sipplanner.domain.model.Investment
import com.abhinav.sipplanner.domain.model.NavPoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val MFAPI_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

// ---- Goals -----------------------------------------------------------------

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    name = name,
    icon = runCatching { GoalIcon.valueOf(icon) }.getOrDefault(GoalIcon.Star),
    targetToday = targetToday,
    targetDate = LocalDate.ofEpochDay(targetDate),
    monthlyContribution = monthlyContribution,
    expectedReturnPercent = expectedReturnPercent,
    inflationPercent = inflationPercent,
    annualStepUpPercent = annualStepUpPercent,
    existingCorpus = existingCorpus,
    createdAt = LocalDate.ofEpochDay(createdAt),
)

fun Goal.toEntity(): GoalEntity = GoalEntity(
    id = id,
    name = name,
    icon = icon.name,
    targetToday = targetToday,
    targetDate = targetDate.toEpochDay(),
    monthlyContribution = monthlyContribution,
    expectedReturnPercent = expectedReturnPercent,
    inflationPercent = inflationPercent,
    annualStepUpPercent = annualStepUpPercent,
    existingCorpus = existingCorpus,
    createdAt = createdAt.toEpochDay(),
)

// ---- Investments -----------------------------------------------------------

fun InvestmentEntity.toDomain(): Investment = Investment(
    id = id,
    schemeCode = schemeCode,
    schemeName = schemeName,
    date = LocalDate.ofEpochDay(date),
    amount = amount,
    navAtPurchase = navAtPurchase,
    goalId = goalId,
)

fun Investment.toEntity(): InvestmentEntity = InvestmentEntity(
    id = id,
    schemeCode = schemeCode,
    schemeName = schemeName,
    date = date.toEpochDay(),
    amount = amount,
    navAtPurchase = navAtPurchase,
    goalId = goalId,
)

// ---- Funds -----------------------------------------------------------------

fun SchemeSearchDto.toDomain(): Fund = Fund(schemeCode = schemeCode, schemeName = schemeName)

fun SchemeDetailDto.toFund(fallbackCode: Int): Fund = Fund(
    schemeCode = meta?.schemeCode ?: fallbackCode,
    schemeName = meta?.schemeName.orEmpty().ifBlank { "Scheme $fallbackCode" },
    fundHouse = meta?.fundHouse,
    category = meta?.schemeCategory,
    schemeType = meta?.schemeType,
)

/** Bad rows are dropped rather than crashing the screen — AMFI data has gaps. */
fun NavEntryDto.toDomainOrNull(): NavPoint? {
    val parsedNav = nav.toDoubleOrNull() ?: return null
    if (parsedNav <= 0.0) return null
    val parsedDate = runCatching { LocalDate.parse(date, MFAPI_DATE) }.getOrNull() ?: return null
    return NavPoint(parsedDate, parsedNav)
}

fun TrackedFundEntity.toFund(): Fund = Fund(
    schemeCode = schemeCode,
    schemeName = schemeName,
    fundHouse = fundHouse,
    category = category,
    schemeType = schemeType,
)

fun Fund.toEntity(nav: Double? = null, navDate: Long? = null): TrackedFundEntity =
    TrackedFundEntity(
        schemeCode = schemeCode,
        schemeName = schemeName,
        fundHouse = fundHouse,
        category = category,
        schemeType = schemeType,
        cachedNav = nav,
        cachedNavDate = navDate,
    )

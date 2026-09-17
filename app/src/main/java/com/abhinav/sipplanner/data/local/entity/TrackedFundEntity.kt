package com.abhinav.sipplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_funds")
data class TrackedFundEntity(
    @PrimaryKey val schemeCode: Int,
    val schemeName: String,
    val fundHouse: String?,
    val category: String?,
    val schemeType: String?,
    val cachedNav: Double?,
    val cachedNavDate: Long?,
)

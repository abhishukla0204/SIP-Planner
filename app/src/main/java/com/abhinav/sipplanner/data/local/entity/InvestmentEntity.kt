package com.abhinav.sipplanner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "investments",
    indices = [Index("goalId"), Index("schemeCode")],
)
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val schemeCode: Int,
    val schemeName: String,
    val date: Long,
    val amount: Double,
    val navAtPurchase: Double,
    val goalId: Long?,
)

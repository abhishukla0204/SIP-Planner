package com.abhinav.sipplanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val targetToday: Double,
    /** Stored as epoch day — sorts correctly in SQL and needs no string parsing. */
    val targetDate: Long,
    val monthlyContribution: Double,
    val expectedReturnPercent: Double,
    val inflationPercent: Double,
    val annualStepUpPercent: Double,
    val existingCorpus: Double,
    val createdAt: Long,
)

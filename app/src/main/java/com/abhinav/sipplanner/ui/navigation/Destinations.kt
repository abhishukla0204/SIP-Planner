package com.abhinav.sipplanner.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes. Using @Serializable objects instead of string
 * routes means a typo in a destination is a compile error, not a crash at runtime.
 */
sealed interface Destination {
    @Serializable data object Home : Destination
    @Serializable data object Calculator : Destination
    @Serializable data object Goals : Destination
    @Serializable data object Funds : Destination
    @Serializable data class AddGoal(val goalId: Long = 0) : Destination
    @Serializable data class FundDetail(val schemeCode: Int) : Destination
}

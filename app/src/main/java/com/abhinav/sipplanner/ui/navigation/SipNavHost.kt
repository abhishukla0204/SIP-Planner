package com.abhinav.sipplanner.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abhinav.sipplanner.ui.screens.calculator.CalculatorScreen
import com.abhinav.sipplanner.ui.screens.funds.FundDetailScreen
import com.abhinav.sipplanner.ui.screens.funds.FundSearchScreen
import com.abhinav.sipplanner.ui.screens.goals.AddGoalScreen
import com.abhinav.sipplanner.ui.screens.goals.GoalsScreen
import com.abhinav.sipplanner.ui.screens.home.HomeScreen
import com.abhinav.sipplanner.ui.theme.SipTheme
import kotlin.reflect.KClass

private data class TabItem(
    val destination: Destination,
    val route: KClass<*>,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun SipNavHost(navController: NavHostController = rememberNavController()) {
    val tabs = remember {
        listOf(
            TabItem(Destination.Home, Destination.Home::class, "Portfolio", Icons.Default.PieChart),
            TabItem(Destination.Calculator, Destination.Calculator::class, "Plan", Icons.Default.Timeline),
            TabItem(Destination.Goals, Destination.Goals::class, "Goals", Icons.Default.Insights),
            TabItem(Destination.Funds, Destination.Funds::class, "Funds", Icons.Default.Search),
        )
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // The bar hides on pushed screens so a detail view gets the full height.
    val showBar = tabs.any { tab -> currentDestination?.hasRoute(tab.route) == true }

    fun navigateToTab(destination: Destination) {
        navController.navigate(destination) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                BandNavBar(
                    tabs = tabs,
                    isSelected = { tab -> currentDestination?.hasRoute(tab.route) == true },
                ) { tab -> navigateToTab(tab.destination) }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home,
            modifier = Modifier.padding(padding),
        ) {
            composable<Destination.Home> {
                HomeScreen(onOpenGoals = { navigateToTab(Destination.Goals) })
            }
            composable<Destination.Calculator> {
                CalculatorScreen()
            }
            composable<Destination.Goals> {
                GoalsScreen(
                    onAddGoal = { navController.navigate(Destination.AddGoal()) },
                    onOpenGoal = { id -> navController.navigate(Destination.AddGoal(id)) },
                )
            }
            composable<Destination.Funds> {
                FundSearchScreen(
                    onOpenFund = { code -> navController.navigate(Destination.FundDetail(code)) },
                )
            }
            composable<Destination.AddGoal> {
                AddGoalScreen(onDone = { navController.popBackStack() })
            }
            composable<Destination.FundDetail> {
                FundDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/**
 * The selected tab is marked with a short marigold bar above the icon — the same
 * band that means "growth" everywhere else, used here because these are
 * destinations in a sequence rather than a set of toggles.
 */
@Composable
private fun BandNavBar(
    tabs: List<TabItem>,
    isSelected: (TabItem) -> Boolean,
    onSelect: (TabItem) -> Unit,
) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SipTheme.colors.hairline),
        ) { }
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            tabs.forEach { tab ->
                val selected = isSelected(tab)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(tab) }
                        .padding(horizontal = 12.dp),
                ) {
                    Box(
                        Modifier
                            .width(if (selected) 22.dp else 0.dp)
                            .height(3.dp)
                            .background(SipTheme.colors.returns),
                    ) { }
                    Spacer(Modifier.height(7.dp))
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            SipTheme.colors.muted
                        },
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onBackground
                        } else {
                            SipTheme.colors.muted
                        },
                    )
                }
            }
        }
    }
}

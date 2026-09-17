package com.abhinav.sipplanner.ui.screens.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.sipplanner.core.format.Money
import com.abhinav.sipplanner.ui.components.SipCard
import com.abhinav.sipplanner.ui.components.StackedBand
import com.abhinav.sipplanner.ui.theme.SipTheme

/**
 * Each goal is one horizontal band. The band itself is the data — how far the
 * plan gets, split into contribution and growth — so the row needs no separate
 * chart or percentage badge.
 */
@Composable
fun GoalsScreen(
    onAddGoal: () -> Unit,
    onOpenGoal: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddGoal,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add goal") },
            )
        },
    ) { padding ->
        when {
            state.loading -> Box(Modifier.fillMaxSize())

            state.rows.isEmpty() -> EmptyGoals(
                onAddGoal = onAddGoal,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
            )

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    GoalsSummary(
                        totalMonthly = state.totalMonthly,
                        onTrack = state.onTrackCount,
                        total = state.rows.size,
                    )
                }
                items(state.rows, key = { it.goal.id }) { row ->
                    GoalRowCard(row = row, onClick = { onOpenGoal(row.goal.id) })
                }
            }
        }
    }
}

@Composable
private fun GoalsSummary(totalMonthly: Double, onTrack: Int, total: Int) {
    Column(Modifier.padding(bottom = 6.dp)) {
        Text(
            text = Money.rupees(totalMonthly) + " a month",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "$onTrack of $total goals on track",
            style = MaterialTheme.typography.bodyMedium,
            color = SipTheme.colors.muted,
        )
    }
}

@Composable
private fun GoalRowCard(row: GoalRow, onClick: () -> Unit) {
    val outcome = row.outcome
    val projection = outcome.projection

    SipCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = row.goal.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = Money.compact(outcome.inflatedTarget) +
                        " by " + Money.monthLabel(row.goal.targetDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SipTheme.colors.muted,
                )
            }
            Text(
                text = Money.rupees(row.goal.monthlyContribution) + "/mo",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(Modifier.height(16.dp))

        StackedBand(
            principal = projection.totalInvested,
            returns = projection.totalGains,
            total = outcome.inflatedTarget,
            height = 12.dp,
        )

        Spacer(Modifier.height(10.dp))

        // The verdict, in plain words. This is the sentence the whole app exists
        // to produce, so it gets stated rather than implied by a colour alone.
        Text(
            text = if (outcome.isOnTrack) {
                "On track — lands ${Money.compact(outcome.surplus)} above target"
            } else {
                "Short by ${Money.compact(outcome.shortfall)}. " +
                    "Raise it to ${Money.rupees(outcome.requiredMonthly)} a month"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (outcome.isOnTrack) {
                SipTheme.colors.principal
            } else {
                SipTheme.colors.shortfall
            },
        )
    }
}

@Composable
private fun EmptyGoals(onAddGoal: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Nothing planned yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add a goal — a house, a car, a year off — and see whether " +
                "your monthly SIP actually gets you there.",
            style = MaterialTheme.typography.bodyLarge,
            color = SipTheme.colors.muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Add your first goal",
            style = MaterialTheme.typography.labelLarge,
            color = SipTheme.colors.principal,
            modifier = Modifier.clickable(onClick = onAddGoal),
        )
    }
}

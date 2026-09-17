package com.abhinav.sipplanner.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.sipplanner.core.format.Money
import com.abhinav.sipplanner.ui.components.AnimatedRupee
import com.abhinav.sipplanner.ui.components.BandLegend
import com.abhinav.sipplanner.ui.components.SipCard
import com.abhinav.sipplanner.ui.components.StackedBand
import com.abhinav.sipplanner.ui.theme.SipTheme

/**
 * Portfolio first: what you've actually put in versus what it's worth now, in the
 * same two-band language the planning screens use. That way the projection and
 * the reality are read the same way.
 */
@Composable
fun HomeScreen(
    onOpenGoals: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Portfolio value",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SipTheme.colors.muted,
                )
                AnimatedRupee(
                    amount = state.currentValue,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = if (state.gain >= 0) {
                        "Up ${Money.compact(state.gain)} on ${Money.compact(state.invested)} invested"
                    } else {
                        "Down ${Money.compact(-state.gain)} on ${Money.compact(state.invested)} invested"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.gain >= 0) SipTheme.colors.principal else SipTheme.colors.shortfall,
                )
            }
        }

        item {
            SipCard {
                StackedBand(
                    principal = state.invested,
                    returns = state.gain.coerceAtLeast(0.0),
                    total = state.currentValue.coerceAtLeast(state.invested),
                    height = 14.dp,
                )
                Spacer(Modifier.height(14.dp))
                BandLegend(
                    principalLabel = "Invested ${Money.compact(state.invested)}",
                    returnsLabel = "Gained ${Money.compact(state.gain.coerceAtLeast(0.0))}",
                )
                state.blendedXirr?.let { xirr ->
                    Spacer(Modifier.height(14.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Portfolio XIRR",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SipTheme.colors.muted,
                        )
                        Text(
                            Money.percent(xirr),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }
            }
        }

        if (state.goals.isNotEmpty()) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${Money.rupees(state.monthlyCommitment)} committed monthly",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    TextButton(onClick = onOpenGoals) { Text("All goals") }
                }
            }
        }

        items(state.holdings, key = { it.schemeCode }) { holding ->
            SipCard {
                Text(
                    text = holding.schemeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(10.dp))
                StackedBand(
                    principal = holding.totalInvested,
                    returns = (holding.gain ?: 0.0).coerceAtLeast(0.0),
                    total = (holding.currentValue ?: holding.totalInvested)
                        .coerceAtLeast(holding.totalInvested),
                    height = 10.dp,
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "%.3f units".format(holding.units),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SipTheme.colors.muted,
                    )
                    Text(
                        text = holding.xirrPercent?.let { "XIRR ${Money.percent(it)}" }
                            ?: "NAV unavailable",
                        style = MaterialTheme.typography.labelLarge,
                        color = SipTheme.colors.muted,
                    )
                }
            }
        }

        if (state.holdings.isEmpty() && !state.loadingHoldings) {
            item {
                SipCard {
                    Text(
                        text = "No instalments logged yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Log the SIPs you've already paid and this screen will " +
                            "price them with live NAVs and score them with XIRR.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SipTheme.colors.muted,
                    )
                }
            }
        }
    }
}

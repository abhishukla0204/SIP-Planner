package com.abhinav.sipplanner.ui.screens.funds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.sipplanner.core.format.Money
import com.abhinav.sipplanner.ui.components.NavHistoryChart
import com.abhinav.sipplanner.ui.components.SipCard
import com.abhinav.sipplanner.ui.theme.SipTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FundDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Fund", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        when {
            state.loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp, color = SipTheme.colors.principal)
            }

            state.error != null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.error.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = SipTheme.colors.muted,
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = viewModel::load) { Text("Try again") }
            }

            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = state.fund?.schemeName.orEmpty(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                state.fund?.fundHouse?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SipTheme.colors.muted,
                    )
                }

                state.latest?.let { nav ->
                    Column {
                        Text(
                            text = "₹%.4f".format(nav.nav),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "NAV on ${Money.date(nav.date)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SipTheme.colors.muted,
                        )
                    }
                }

                NavHistoryChart(points = state.history, height = 200.dp)

                SipCard {
                    Text(
                        "Trailing returns",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(Modifier.height(10.dp))
                    ReturnRow("1 year", state.returnOver(365))
                    ReturnRow("3 years", state.returnOver(365 * 3))
                    ReturnRow("5 years", state.returnOver(365 * 5))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Figures beyond a year are annualised. Past returns " +
                            "don't predict future ones.",
                        style = MaterialTheme.typography.labelMedium,
                        color = SipTheme.colors.muted,
                    )
                }

                Button(onClick = viewModel::track, modifier = Modifier.fillMaxWidth()) {
                    Text("Follow this fund")
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReturnRow(label: String, value: Double?) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SipTheme.colors.muted)
        Text(
            text = value?.let { Money.percent(it) } ?: "Not enough history",
            style = MaterialTheme.typography.labelLarge,
            color = when {
                value == null -> SipTheme.colors.muted
                value >= 0 -> SipTheme.colors.principal
                else -> SipTheme.colors.shortfall
            },
        )
    }
}

package com.abhinav.sipplanner.ui.screens.funds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.sipplanner.core.format.Money
import com.abhinav.sipplanner.ui.components.SipCard
import com.abhinav.sipplanner.ui.theme.SipTheme

/**
 * Search over AMFI's full scheme list via api.mfapi.in. Tracked funds sit above
 * the search field, so the screen is useful before anything is typed.
 */
@Composable
fun FundSearchScreen(
    onOpenFund: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FundSearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tracked by viewModel.tracked.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Search mutual funds") },
                placeholder = { Text("Parag Parikh, HDFC index, small cap") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.searching) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = SipTheme.colors.principal,
                    )
                }
            }
        }

        state.error?.let { message ->
            item {
                SipCard {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SipTheme.colors.shortfall,
                    )
                }
            }
        }

        if (state.results.isEmpty() && state.query.isBlank() && tracked.isNotEmpty()) {
            item {
                Text(
                    "Following",
                    style = MaterialTheme.typography.titleMedium,
                    color = SipTheme.colors.muted,
                )
            }
            items(tracked, key = { it.fund.schemeCode }) { item ->
                SipCard(modifier = Modifier.clickable { onOpenFund(item.fund.schemeCode) }) {
                    Text(
                        text = item.fund.schemeName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    item.latestNav?.let { nav ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "NAV ₹%.4f on %s".format(nav.nav, Money.date(nav.date)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SipTheme.colors.muted,
                        )
                    }
                }
            }
        }

        items(state.results, key = { it.schemeCode }) { fund ->
            SipCard(modifier = Modifier.clickable { onOpenFund(fund.schemeCode) }) {
                Text(
                    text = fund.schemeName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Scheme code ${fund.schemeCode}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SipTheme.colors.muted,
                )
            }
        }

        if (state.query.length in 1..2) {
            item {
                Text(
                    text = "Keep typing — search needs at least three letters.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SipTheme.colors.muted,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

package com.abhinav.sipplanner.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.sipplanner.core.format.Money
import com.abhinav.sipplanner.domain.model.Fund
import com.abhinav.sipplanner.domain.model.NavPoint
import com.abhinav.sipplanner.domain.model.TrackedFund
import com.abhinav.sipplanner.ui.components.AnimatedRupee
import com.abhinav.sipplanner.ui.components.BandLegend
import com.abhinav.sipplanner.ui.components.SipCard
import com.abhinav.sipplanner.ui.components.StackedBand
import com.abhinav.sipplanner.ui.theme.SipTheme
import kotlinx.coroutines.launch

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
    val trackedFunds by viewModel.trackedFunds.collectAsStateWithLifecycle()
    var showLogDialog by remember { mutableStateOf(value = false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showLogDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Log Investment") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 96.dp),
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
                            text = when {
                                holding.xirrPercent != null -> "XIRR ${Money.percent(holding.xirrPercent)}"
                                holding.currentNav != null -> "NAV ₹%.2f".format(holding.currentNav)
                                else -> "NAV unavailable"
                            },
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
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { showLogDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                        ) {
                            Text("Log your first investment")
                        }
                    }
                }
            }
        }
    }

    if (showLogDialog) {
        LogInvestmentDialog(
            trackedFunds = trackedFunds,
            onDismiss = { showLogDialog = false },
            onSave = { schemeCode, schemeName, amount, nav ->
                viewModel.addInvestment(
                    schemeCode = schemeCode,
                    schemeName = schemeName,
                    amount = amount,
                    navAtPurchase = nav,
                )
                showLogDialog = false
            },
            onSearchFunds = viewModel::searchFunds,
            onFetchLatestNav = viewModel::fetchLatestNav,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogInvestmentDialog(
    trackedFunds: List<TrackedFund>,
    onDismiss: () -> Unit,
    onSave: (schemeCode: Int, schemeName: String, amount: Double, nav: Double) -> Unit,
    onSearchFunds: suspend (String) -> Result<List<Fund>>,
    onFetchLatestNav: suspend (Int) -> Result<NavPoint>,
) {
    val scope = rememberCoroutineScope()
    var selectedFundName by remember { mutableStateOf("") }
    var selectedSchemeCode by remember { mutableStateOf<Int?>(null) }
    var amountText by remember { mutableStateOf("") }
    var navText by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(value = false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Fund>>(emptyList()) }
    var isSearching by remember { mutableStateOf(value = false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length >= 3) {
            isSearching = true
            val res = onSearchFunds(searchQuery)
            searchResults = res.getOrDefault(emptyList())
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }

    fun selectFund(code: Int, name: String, initialNav: Double?) {
        selectedSchemeCode = code
        selectedFundName = name
        searchQuery = name
        expanded = false
        if ((initialNav != null) && (initialNav > 0)) {
            navText = initialNav.toString()
        } else {
            scope.launch {
                val navRes = onFetchLatestNav(code)
                navRes.getOrNull()?.let { navText = it.nav.toString() }
            }
        }
    }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val nav = navText.toDoubleOrNull() ?: 0.0
    val calculatedUnits = if (nav > 0) amount / nav else 0.0
    val isValid = (selectedSchemeCode != null) && (amount > 0) && (nav > 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Investment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            selectedSchemeCode = null
                            expanded = true
                        },
                        label = { Text("Select or search fund") },
                        placeholder = { Text("Parag Parikh, SBI, etc.") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryEditable),
                    )

                    ExposedDropdownMenu(
                        expanded = expanded && (trackedFunds.isNotEmpty() || searchResults.isNotEmpty() || isSearching),
                        onDismissRequest = { expanded = false },
                    ) {
                        if (trackedFunds.isNotEmpty() && searchQuery.isBlank()) {
                            DropdownMenuItem(
                                text = { Text("FOLLOWED FUNDS", style = MaterialTheme.typography.labelSmall) },
                                onClick = {},
                                enabled = false,
                            )
                            trackedFunds.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.fund.schemeName) },
                                    onClick = {
                                        selectFund(
                                            code = item.fund.schemeCode,
                                            name = item.fund.schemeName,
                                            initialNav = item.latestNav?.nav,
                                        )
                                    },
                                )
                            }
                        }

                        if (searchResults.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("SEARCH RESULTS", style = MaterialTheme.typography.labelSmall) },
                                onClick = {},
                                enabled = false,
                            )
                            searchResults.take(10).forEach { fund ->
                                DropdownMenuItem(
                                    text = { Text(fund.schemeName) },
                                    onClick = {
                                        selectFund(
                                            code = fund.schemeCode,
                                            name = fund.schemeName,
                                            initialNav = null,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() || (c == '.') } },
                    label = { Text("Amount Invested (₹)") },
                    placeholder = { Text("10000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = navText,
                    onValueChange = { navText = it.filter { c -> c.isDigit() || (c == '.') } },
                    label = { Text("NAV at Purchase (₹)") },
                    placeholder = { Text("124.50") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                if (calculatedUnits > 0) {
                    Text(
                        text = "Estimated Units: %.3f".format(calculatedUnits),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SipTheme.colors.principal,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid,
                onClick = {
                    val code = selectedSchemeCode ?: return@Button
                    onSave(code, selectedFundName, amount, nav)
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

package com.abhinav.sipplanner.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.sipplanner.core.format.Money
import com.abhinav.sipplanner.ui.components.AnimatedRupee
import com.abhinav.sipplanner.ui.components.BandLegend
import com.abhinav.sipplanner.ui.components.GrowthChart
import com.abhinav.sipplanner.ui.components.LabeledSlider
import com.abhinav.sipplanner.ui.components.SipCard
import com.abhinav.sipplanner.ui.components.StackedBand
import com.abhinav.sipplanner.ui.theme.SipTheme

/**
 * The hero here is the resulting corpus — one very large number with the two-band
 * chart directly beneath it, so the split between contribution and growth is the
 * first thing read after the total.
 */
@Composable
fun CalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val projection = remember(state) { state.projection }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        ModeSwitch(
            selected = state.mode,
            onSelect = viewModel::setMode,
        )

        // --- The headline result -------------------------------------------
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = when (state.mode) {
                    CalculatorMode.Target -> "You'd need to invest each month"
                    else -> "In ${Money.duration(state.months)} you'd have"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = SipTheme.colors.muted,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            AnimatedRupee(
                amount = if (state.mode == CalculatorMode.Target) {
                    state.requiredMonthly
                } else {
                    projection.finalValue
                },
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        GrowthChart(
            points = remember(projection) { projection.sampled() },
            height = 210.dp,
        )

        BandLegend(
            principalLabel = "You invest ${Money.compact(projection.totalInvested)}",
            returnsLabel = "Returns add ${Money.compact(projection.totalGains)}",
            modifier = Modifier.fillMaxWidth(),
        )

        // --- Inputs ---------------------------------------------------------
        SipCard {
            when (state.mode) {
                CalculatorMode.Sip -> LabeledSlider(
                    label = "Monthly investment",
                    valueText = Money.rupees(state.monthlyAmount),
                    value = state.monthlyAmount.toFloat(),
                    onValueChange = { viewModel.setMonthly(it.toDouble()) },
                    valueRange = 500f..2_00_000f,
                )
                CalculatorMode.Lumpsum -> LabeledSlider(
                    label = "One-time investment",
                    valueText = Money.rupees(state.lumpsum),
                    value = state.lumpsum.toFloat(),
                    onValueChange = { viewModel.setLumpsum(it.toDouble()) },
                    valueRange = 5_000f..1_00_00_000f,
                )
                CalculatorMode.Target -> LabeledSlider(
                    label = "Amount you want",
                    valueText = Money.compact(state.targetAmount),
                    value = state.targetAmount.toFloat(),
                    onValueChange = { viewModel.setTarget(it.toDouble()) },
                    valueRange = 1_00_000f..10_00_00_000f,
                )
            }

            Spacer(Modifier.height(12.dp))

            LabeledSlider(
                label = "For how long",
                valueText = Money.duration(state.months),
                value = state.years.toFloat(),
                onValueChange = { viewModel.setYears(it.toInt()) },
                valueRange = 1f..40f,
                steps = 38,
            )

            Spacer(Modifier.height(12.dp))

            LabeledSlider(
                label = "Expected return",
                valueText = Money.percent(state.expectedReturn) + " a year",
                value = state.expectedReturn.toFloat(),
                onValueChange = { viewModel.setReturn(it.toDouble()) },
                valueRange = 1f..24f,
            )

            if (state.mode != CalculatorMode.Lumpsum) {
                Spacer(Modifier.height(12.dp))
                LabeledSlider(
                    label = "Raise instalment yearly",
                    valueText = if (state.stepUpPercent == 0.0) {
                        "Off"
                    } else {
                        Money.percent(state.stepUpPercent, 0)
                    },
                    value = state.stepUpPercent.toFloat(),
                    onValueChange = { viewModel.setStepUp(it.toDouble()) },
                    valueRange = 0f..25f,
                    steps = 24,
                )
            }
        }

        // --- Breakdown ------------------------------------------------------
        SipCard {
            Text(
                text = "Where the money comes from",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(14.dp))
            StackedBand(
                principal = projection.totalInvested,
                returns = projection.totalGains,
                total = projection.finalValue,
                height = 14.dp,
            )
            Spacer(Modifier.height(16.dp))
            BreakdownRow("You invest", Money.rupees(projection.totalInvested))
            BreakdownRow("Returns add", Money.rupees(projection.totalGains))
            BreakdownRow(
                label = "Returns are",
                value = Money.percent(projection.gainsShare * 100) + " of the total",
            )
            if (state.stepUpPercent > 0) {
                BreakdownRow(
                    label = "Final instalment",
                    value = Money.rupees(projection.finalInstalment) + " a month",
                )
            }
        }

        Text(
            text = "Projections assume a steady return every year. Real markets " +
                "don't work that way, so treat this as a planning tool rather " +
                "than a forecast.",
            style = MaterialTheme.typography.bodyMedium,
            color = SipTheme.colors.muted,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = SipTheme.colors.muted)
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/**
 * A three-way switch. The selected item is marked with a solid fill rather than
 * an underline, because unlike the nav bar these are alternatives rather than
 * destinations.
 */
@Composable
private fun ModeSwitch(
    selected: CalculatorMode,
    onSelect: (CalculatorMode) -> Unit,
) {
    val labels = mapOf(
        CalculatorMode.Sip to "Monthly SIP",
        CalculatorMode.Lumpsum to "One-time",
        CalculatorMode.Target to "Reach a target",
    )

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SipTheme.colors.sunken)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CalculatorMode.entries.forEach { mode ->
            val isSelected = mode == selected
            Text(
                text = labels.getValue(mode),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    SipTheme.colors.muted
                },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else SipTheme.colors.sunken,
                    )
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
            )
        }
    }
}

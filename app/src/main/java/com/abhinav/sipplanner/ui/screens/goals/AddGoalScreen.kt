package com.abhinav.sipplanner.ui.screens.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abhinav.sipplanner.core.format.Money
import com.abhinav.sipplanner.ui.components.GrowthChart
import com.abhinav.sipplanner.ui.components.LabeledSlider
import com.abhinav.sipplanner.ui.components.SipCard
import com.abhinav.sipplanner.ui.theme.SipTheme

/**
 * Editing and creating share one screen. The verdict card updates live as the
 * sliders move, so the answer arrives before the user commits to saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddGoalViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (state.id == 0L) "New goal" else "Edit goal") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
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
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::setName,
                label = { Text("What are you saving for?") },
                placeholder = { Text("Down payment, Europe trip, emergency fund") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            SipCard {
                LabeledSlider(
                    label = "Cost today",
                    valueText = Money.compact(state.targetToday),
                    value = state.targetToday.toFloat(),
                    onValueChange = { viewModel.setTarget(it.toDouble()) },
                    valueRange = 50_000f..5_00_00_000f,
                )
                Spacer(Modifier.height(12.dp))
                LabeledSlider(
                    label = "Years away",
                    valueText = Money.duration(state.years * 12),
                    value = state.years.toFloat(),
                    onValueChange = { viewModel.setYears(it.toInt()) },
                    valueRange = 1f..40f,
                    steps = 38,
                )
                Spacer(Modifier.height(12.dp))
                LabeledSlider(
                    label = "You can invest monthly",
                    valueText = Money.rupees(state.monthlyContribution),
                    value = state.monthlyContribution.toFloat(),
                    onValueChange = { viewModel.setMonthly(it.toDouble()) },
                    valueRange = 500f..2_00_000f,
                )
                Spacer(Modifier.height(12.dp))
                LabeledSlider(
                    label = "Already saved for this",
                    valueText = Money.compact(state.existingCorpus),
                    value = state.existingCorpus.toFloat(),
                    onValueChange = { viewModel.setExistingCorpus(it.toDouble()) },
                    valueRange = 0f..1_00_00_000f,
                )
            }

            SipCard {
                Text(
                    "Assumptions",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(12.dp))
                LabeledSlider(
                    label = "Expected return",
                    valueText = Money.percent(state.expectedReturn),
                    value = state.expectedReturn.toFloat(),
                    onValueChange = { viewModel.setReturn(it.toDouble()) },
                    valueRange = 1f..24f,
                )
                Spacer(Modifier.height(12.dp))
                LabeledSlider(
                    label = "Inflation",
                    valueText = Money.percent(state.inflation),
                    value = state.inflation.toFloat(),
                    onValueChange = { viewModel.setInflation(it.toDouble()) },
                    valueRange = 0f..12f,
                )
                Spacer(Modifier.height(12.dp))
                LabeledSlider(
                    label = "Raise instalment yearly",
                    valueText = if (state.stepUp == 0.0) "Off" else Money.percent(state.stepUp, 0),
                    value = state.stepUp.toFloat(),
                    onValueChange = { viewModel.setStepUp(it.toDouble()) },
                    valueRange = 0f..25f,
                    steps = 24,
                )
            }

            // --- Live verdict --------------------------------------------------
            val outcome = state.outcome
            SipCard {
                Text(
                    text = if (outcome.isOnTrack) "This plan gets you there" else "This plan falls short",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (outcome.isOnTrack) SipTheme.colors.principal else SipTheme.colors.shortfall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${Money.compact(state.targetToday)} today is " +
                        "${Money.compact(outcome.inflatedTarget)} in ${state.years} years " +
                        "after inflation. You'd reach ${Money.compact(outcome.projection.finalValue)}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SipTheme.colors.muted,
                )
                Spacer(Modifier.height(16.dp))
                GrowthChart(
                    points = outcome.projection.sampled(),
                    height = 160.dp,
                    targetLine = outcome.inflatedTarget,
                )
                if (!outcome.isOnTrack) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = viewModel::useRequiredAmount) {
                        Text("Use ${Money.rupees(outcome.requiredMonthly)} a month instead")
                    }
                }
            }

            Button(
                onClick = viewModel::save,
                enabled = state.isValid,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(if (state.id == 0L) "Save goal" else "Save changes")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

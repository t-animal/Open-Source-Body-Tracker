package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.text.NumberFormat

@Composable
fun MeasurementListRoute(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
    onEdit: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    val vm: MeasurementListViewModel = viewModel(
        factory = MeasurementListViewModelFactory(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    MeasurementListScreen(
        state = state,
        onEdit = onEdit,
        contentPadding = contentPadding,
    )
}

@Composable
fun MeasurementListScreen(
    state: MeasurementListUiState,
    onEdit: (Long) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
    ) {
        items(items = state.measurements, key = { it.measurement.id }) { measurement ->
            MeasurementRow(
                item = measurement,
                onClick = { onEdit(measurement.measurement.id) },
            )
        }
    }
}

@Composable
fun MeasurementListAddButton(
    onAdd: () -> Unit,
) {
    ExtendedFloatingActionButton(onClick = onAdd) {
        Text("Add")
    }
}

@Composable
private fun MeasurementRow(
    item: MeasurementListItemUiModel,
    onClick: () -> Unit,
) {
    val measurement = item.measurement

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = formatEpochMillisToLocalizedNumericDate(measurement.dateEpochMillis),
            style = MaterialTheme.typography.titleMedium,
        )

        val weight = measurement.weightKg
        if (weight != null) {
            Text(text = "Weight: ${formatDecimal(weight)} kg")
        }

        item.derivedMetrics.bmi?.let { Text(text = "BMI: ${formatDecimal(it)}") }
        item.derivedMetrics.navyBodyFatPercent?.let { Text(text = "Body Fat (Navy): ${formatDecimal(it)} %") }
        item.derivedMetrics.skinfold3SiteBodyFatPercent?.let {
            Text(text = "Body Fat (Skinfold 3-site): ${formatDecimal(it)} %")
        }
        item.derivedMetrics.waistHipRatio?.let { Text(text = "WHR: ${formatDecimal(it)}") }
        item.derivedMetrics.waistHeightRatio?.let { Text(text = "WHtR: ${formatDecimal(it)}") }
        item.derivedMetrics.hipHeightRatio?.let { Text(text = "Hip/Height: ${formatDecimal(it)}") }
    }
}

private fun formatDecimal(value: Double): String {
    val nf = NumberFormat.getNumberInstance()
    nf.isGroupingUsed = false
    nf.maximumFractionDigits = 2
    return nf.format(value)
}

@Preview(showBackground = true)
@Composable
private fun MeasurementListScreenPreview() {
    BodyTrackerTheme {
        MeasurementListScreen(
            state = MeasurementListUiState(
                measurements = listOf(
                    MeasurementListItemUiModel(
                        measurement = BodyMeasurement(
                            id = 1,
                            dateEpochMillis = 1_700_000_000_000,
                            weightKg = 80.0,
                        ),
                        derivedMetrics = DerivedMetrics(bmi = 24.69, waistHeightRatio = 0.50),
                    ),
                    MeasurementListItemUiModel(
                        measurement = BodyMeasurement(
                            id = 2,
                            dateEpochMillis = 1_710_000_000_000,
                            neckCircumferenceCm = 40.0,
                        ),
                        derivedMetrics = DerivedMetrics(),
                    ),
                ),
            ),
            onEdit = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}

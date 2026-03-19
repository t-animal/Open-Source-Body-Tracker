package de.t_animal.opensourcebodytracker.feature.measurements.components

import android.widget.Toast
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedMetricRatings
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MetricRating
import de.t_animal.opensourcebodytracker.core.model.RatingSeverity
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListItemUiModel
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListUiState
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.buildLatestMeasurementMetrics
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
internal fun LatestMeasurementCard(
    state: MeasurementListUiState,
    onAdd: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Latest Measurement",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    Text("Loading…")
                }

                state.isEmpty -> {
                    Text(
                        text = "No measurements yet – create your first measurement",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onAdd) {
                        Text("Add")
                    }
                }

                else -> {
                    val latest = state.latestMeasurement
                    if (latest != null) {
                        val analysisMetrics = state.visibleInTableMetrics.filterIsInstance<DerivedBodyMetric>()
                        val rawMetrics = state.visibleInTableMetrics.filterIsInstance<MeasuredBodyMetric>()

                        if (analysisMetrics.isNotEmpty()) {
                            Text(
                                text = "Analyses",
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LatestMeasurementGrid(
                                item = latest,
                                visibleMetrics = analysisMetrics,
                            )
                        }

                        if (rawMetrics.isNotEmpty()) {
                            if (analysisMetrics.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            Text(
                                text = "Measurements",
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LatestMeasurementGrid(
                                item = latest,
                                visibleMetrics = rawMetrics,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LatestMeasurementCardPreview() {
    BodyTrackerTheme {
        LatestMeasurementCard(
            state = MeasurementListUiState(
                latestMeasurement = MeasurementListItemUiModel(
                    measurement = BodyMeasurement(
                        id = 1,
                        dateEpochMillis = 1_710_000_000_000,
                        weightKg = 80.0,
                        bodyFatPercent = 20.0,
                        neckCircumferenceCm = 38.0,
                        chestCircumferenceCm = 100.0,
                        waistCircumferenceCm = 86.0,
                        abdomenCircumferenceCm = 88.0,
                        hipCircumferenceCm = 95.0,
                        chestSkinfoldMm = 12.0,
                        abdomenSkinfoldMm = 18.0,
                        thighSkinfoldMm = 15.0,
                    ),
                    derivedMetrics = DerivedMetrics(
                        bmi = 24.69,
                        navyBodyFatPercent = 18.3,
                        skinfold3SiteBodyFatPercent = 17.8,
                        waistHipRatio = 0.91,
                        waistHeightRatio = 0.5,
                    ),
                    derivedMetricRatings = DerivedMetricRatings(
                        bmi = MetricRating("Normal", RatingSeverity.Good),
                        navyBodyFatPercent = MetricRating("Fit", RatingSeverity.Good),
                        skinfold3SiteBodyFatPercent = MetricRating("Fit", RatingSeverity.Good),
                        waistHipRatio = MetricRating("Moderate risk", RatingSeverity.Fair),
                        waistHeightRatio = MetricRating("Increased risk", RatingSeverity.Fair),
                    ),
                ),
                visibleInTableMetrics = MeasuredBodyMetric.entries + DerivedBodyMetric.entries,
                isEmpty = false,
                isLoading = false,
            ),
            onAdd = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LatestMeasurementCardEmptyPreview() {
    BodyTrackerTheme {
        LatestMeasurementCard(
            state = MeasurementListUiState(
                isEmpty = true,
                isLoading = false,
            ),
            onAdd = {},
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LatestMeasurementGrid(
    item: MeasurementListItemUiModel,
    visibleMetrics: List<BodyMetric>,
) {
    val metrics = remember(item, visibleMetrics) {
        buildLatestMeasurementMetrics(item, visibleMetrics)
    }
    val context = LocalContext.current

    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        metrics.forEach { metric ->
            Column(
                modifier = Modifier
                    .width(0.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        Toast.makeText(context, metric.fullName, Toast.LENGTH_SHORT).show()
                    }
                )
                metric.rating?.let { rating ->
                    Text(
                        text = rating.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (rating.severity) {
                            RatingSeverity.Good -> MaterialTheme.colorScheme.tertiary
                            RatingSeverity.Fair -> MaterialTheme.colorScheme.secondary
                            RatingSeverity.Poor -> MaterialTheme.colorScheme.error
                            RatingSeverity.Severe -> MaterialTheme.colorScheme.error
                        },
                    )
                }
            }
        }
    }
}

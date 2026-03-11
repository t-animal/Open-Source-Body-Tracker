package de.t_animal.opensourcebodytracker.feature.measurements.components

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
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListItemUiModel
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListUiState
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.buildLatestMeasurementMetrics

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
                        LatestMeasurementGrid(
                            item = latest,
                            visibleMetrics = state.visibleInTableMetrics,
                        )
                    }
                }
            }
        }
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
                )
            }
        }
    }
}

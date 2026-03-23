package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.BodyMetricType
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.label
import de.t_animal.opensourcebodytracker.ui.helpers.displaySymbol

@Composable
fun MetricSections(
    metrics: List<MeasuredBodyMetric>,
    bodyMetricInputs: Map<MeasuredBodyMetric, String>,
    onMetricChanged: (MeasuredBodyMetric, String) -> Unit,
    unitSystem: UnitSystem,
) {
    if (metrics.isEmpty()) {
        return
    }

    val sectionOrder = listOf(
        BodyMetricType.Weight,
        BodyMetricType.Circumference,
        BodyMetricType.SkinfoldThickness,
    )

    val metricsByType = sectionOrder
        .associateWith { metricType -> metrics.filter { it.metricType == metricType } }
        .filterValues { it.isNotEmpty() }

    val lastMetric = metrics.last()

    metricsByType.forEach { (metricType, typedMetrics) ->
        Text(
            text = metricSectionTitle(metricType),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))

        typedMetrics.forEach { metric ->
            val isLastMetric = metric == lastMetric
            MetricInputField(
                isVisible = true,
                label = metricInputLabel(metric, unitSystem),
                value = bodyMetricInputs[metric].orEmpty(),
                onValueChange = { value -> onMetricChanged(metric, value) },
                imeAction = if (isLastMetric) ImeAction.Done else ImeAction.Next,
                addBottomSpacing = !isLastMetric,
            )
        }

        if (typedMetrics.last() != lastMetric) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun metricSectionTitle(metricType: BodyMetricType): String {
    return when (metricType) {
        BodyMetricType.Weight -> stringResource(R.string.metric_section_weight)
        BodyMetricType.Circumference -> stringResource(R.string.metric_section_circumference)
        BodyMetricType.SkinfoldThickness -> stringResource(R.string.metric_section_skinfold)
        BodyMetricType.AnalysisResult -> stringResource(R.string.metric_section_analysis)
    }
}

@Composable
private fun metricInputLabel(metric: MeasuredBodyMetric, unitSystem: UnitSystem): String {
    val label = metric.label()
    return if (metric.unit == BodyMetricUnit.Unitless) {
        label
    } else {
        stringResource(R.string.metric_input_label_with_unit, label, metric.unit.displaySymbol(unitSystem))
    }
}

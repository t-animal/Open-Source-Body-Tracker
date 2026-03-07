package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.core.model.BodyMetricType
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric

@Composable
fun MetricSections(
    metrics: List<MeasuredBodyMetric>,
    metricInputs: Map<MeasuredBodyMetric, String>,
    onMetricChanged: (MeasuredBodyMetric, String) -> Unit,
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
                label = metricInputLabel(metric),
                value = metricInputs[metric].orEmpty(),
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

private fun metricSectionTitle(metricType: BodyMetricType): String {
    return when (metricType) {
        BodyMetricType.Weight -> "Weight"
        BodyMetricType.Circumference -> "Circumference"
        BodyMetricType.SkinfoldThickness -> "Skinfold Thickness"
        BodyMetricType.AnalysisResult -> "Analysis"
    }
}

private fun metricInputLabel(metric: MeasuredBodyMetric): String {
    return when (metric) {
        MeasuredBodyMetric.Weight -> "Weight (kg)"
        MeasuredBodyMetric.BodyFat -> "Body Fat (%)"
        MeasuredBodyMetric.NeckCircumference -> "Neck (cm)"
        MeasuredBodyMetric.ChestCircumference -> "Chest (cm)"
        MeasuredBodyMetric.WaistCircumference -> "Waist (cm)"
        MeasuredBodyMetric.AbdomenCircumference -> "Abdomen (cm)"
        MeasuredBodyMetric.HipCircumference -> "Hip (cm)"
        MeasuredBodyMetric.ChestSkinfold -> "Chest Skinfold (mm)"
        MeasuredBodyMetric.AbdomenSkinfold -> "Abdomen Skinfold (mm)"
        MeasuredBodyMetric.ThighSkinfold -> "Thigh Skinfold (mm)"
        MeasuredBodyMetric.TricepsSkinfold -> "Triceps Skinfold (mm)"
        MeasuredBodyMetric.SuprailiacSkinfold -> "Suprailiac Skinfold (mm)"
    }
}

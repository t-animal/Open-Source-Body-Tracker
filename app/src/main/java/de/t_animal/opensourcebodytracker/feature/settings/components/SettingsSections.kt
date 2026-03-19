package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onVisibilityChangedNode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric

@Composable
fun AnalysisMethodsSection(
    bmiEnabled: Boolean,
    navyBodyFatEnabled: Boolean,
    skinfoldBodyFatEnabled: Boolean,
    waistHipRatioEnabled: Boolean,
    waistHeightRatioEnabled: Boolean,
    onBmiEnabledChanged: (Boolean) -> Unit,
    onNavyBodyFatEnabledChanged: (Boolean) -> Unit,
    onSkinfoldBodyFatEnabledChanged: (Boolean) -> Unit,
    onWaistHipRatioEnabledChanged: (Boolean) -> Unit,
    onWaistHeightRatioEnabledChanged: (Boolean) -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsCheckRow(
                label = "BMI",
                secondaryLabel = "Puts body weight into relation to height. Not a good health indicator, but can be a useful first approximation for many people.",
                checked = bmiEnabled,
                enabled = true,
                onCheckedChange = onBmiEnabledChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCheckRow(
                label = "Waist–Hip Ratio",
                secondaryLabel = "Accounts for fat distribution. Higher values are associated with increased risk of cardiovascular and metabolic diseases.",
                checked = waistHipRatioEnabled,
                enabled = true,
                onCheckedChange = onWaistHipRatioEnabledChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCheckRow(
                label = "Waist–Height Ratio",
                secondaryLabel = "Strong predictor of cardiovascular and metabolic risk, often more informative than BMI.",
                checked = waistHeightRatioEnabled,
                enabled = true,
                onCheckedChange = onWaistHeightRatioEnabledChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCheckRow(
                label = "Navy Body Fat %",
                secondaryLabel = "Estimates body fat percentage based on circumference measurements. Rough approximation and very quick to measure.",
                checked = navyBodyFatEnabled,
                enabled = true,
                onCheckedChange = onNavyBodyFatEnabledChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCheckRow(
                label = "Skinfold Body Fat %",
                secondaryLabel = "Estimates body fat percentage based on skinfold measurements. Quite accurate when done right, but requires some skill and practice.",
                checked = skinfoldBodyFatEnabled,
                enabled = true,
                onCheckedChange = onSkinfoldBodyFatEnabledChanged,
            )
        }
    }
}

@Composable
fun MeasurementCollectionSection(
    enabledMeasurements: Set<MeasuredBodyMetric>,
    requiredMeasurements: Set<MeasuredBodyMetric>,
    measurementToAnalysisMethods: Map<MeasuredBodyMetric, Set<AnalysisMethod>>,
    onMeasurementEnabledChanged: (MeasuredBodyMetric, Boolean) -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            MeasuredBodyMetric.entries.forEach { measurement ->
                val required = measurement in requiredMeasurements
                val requiredByMethods = measurementToAnalysisMethods[measurement].orEmpty()
                val secondaryLabel = when {
                    requiredByMethods.isNotEmpty() ->
                        "(required for ${requiredByMethods.toAnalysisMethodLabelList()})"

                    required -> "(required)"
                    else -> "(optional)"
                }

                SettingsCheckRow(
                    label = measurement.label(),
                    secondaryLabel = secondaryLabel,
                    checked = measurement in enabledMeasurements,
                    enabled = !required,
                    onCheckedChange = { onMeasurementEnabledChanged(measurement, it) },
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SettingsCheckRow(
    label: String,
    secondaryLabel: String? = null,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!secondaryLabel.isNullOrBlank()) {
                Text(
                    text = secondaryLabel,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
        )
    }
}

private fun Set<AnalysisMethod>.toAnalysisMethodLabelList(): String = AnalysisMethod.entries
    .filter { it in this }
    .joinToString(", ") { it.label() }

private fun AnalysisMethod.label(): String = when (this) {
    AnalysisMethod.Bmi -> "BMI"
    AnalysisMethod.NavyBodyFat -> "Navy Body Fat %"
    AnalysisMethod.Skinfold3SiteBodyFat -> "Skinfold Body Fat %"
    AnalysisMethod.WaistHipRatio -> "Waist–Hip Ratio"
    AnalysisMethod.WaistHeightRatio -> "Waist–Height Ratio"
}

private fun MeasuredBodyMetric.label(): String = when (this) {
    MeasuredBodyMetric.Weight -> "Weight"
    MeasuredBodyMetric.BodyFat -> "Body Fat"
    MeasuredBodyMetric.NeckCircumference -> "Neck"
    MeasuredBodyMetric.WaistCircumference -> "Waist"
    MeasuredBodyMetric.HipCircumference -> "Hip"
    MeasuredBodyMetric.ChestCircumference -> "Chest"
    MeasuredBodyMetric.AbdomenCircumference -> "Abdomen"
    MeasuredBodyMetric.ChestSkinfold -> "Chest Skinfold"
    MeasuredBodyMetric.AbdomenSkinfold -> "Abdomen Skinfold"
    MeasuredBodyMetric.ThighSkinfold -> "Thigh Skinfold"
    MeasuredBodyMetric.TricepsSkinfold -> "Triceps Skinfold"
    MeasuredBodyMetric.SuprailiacSkinfold -> "Suprailiac Skinfold"
}


@Composable
@Preview
fun MeasurementCollectionSectionPreview() {
    val enabledMeasurements = setOf(
        MeasuredBodyMetric.Weight,
        MeasuredBodyMetric.WaistCircumference,
        MeasuredBodyMetric.ChestSkinfold,
        MeasuredBodyMetric.AbdomenSkinfold
    )
    val requiredMeasurements = setOf(
        MeasuredBodyMetric.Weight,
        MeasuredBodyMetric.WaistCircumference,
    )
    val measurementToAnalysisMethods = mapOf(
        MeasuredBodyMetric.WaistCircumference to setOf(
            AnalysisMethod.NavyBodyFat,
            AnalysisMethod.WaistHipRatio
        ),
    )

    MeasurementCollectionSection(
        enabledMeasurements = enabledMeasurements,
        requiredMeasurements = requiredMeasurements,
        measurementToAnalysisMethods = measurementToAnalysisMethods,
        onMeasurementEnabledChanged = { _, _ -> },
    )
}

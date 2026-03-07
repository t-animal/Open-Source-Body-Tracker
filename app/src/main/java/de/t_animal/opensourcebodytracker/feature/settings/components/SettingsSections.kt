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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            Text("Analysis Methods", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCheckRow(
                label = "BMI",
                checked = bmiEnabled,
                enabled = true,
                onCheckedChange = onBmiEnabledChanged,
            )
            SettingsCheckRow(
                label = "Navy Body Fat %",
                checked = navyBodyFatEnabled,
                enabled = true,
                onCheckedChange = onNavyBodyFatEnabledChanged,
            )
            SettingsCheckRow(
                label = "Skinfold Body Fat %",
                checked = skinfoldBodyFatEnabled,
                enabled = true,
                onCheckedChange = onSkinfoldBodyFatEnabledChanged,
            )
            SettingsCheckRow(
                label = "Waist–Hip Ratio",
                checked = waistHipRatioEnabled,
                enabled = true,
                onCheckedChange = onWaistHipRatioEnabledChanged,
            )
            SettingsCheckRow(
                label = "Waist–Height Ratio",
                checked = waistHeightRatioEnabled,
                enabled = true,
                onCheckedChange = onWaistHeightRatioEnabledChanged,
            )
        }
    }
}

@Composable
fun MeasurementCollectionSection(
    enabledMeasurements: Set<MeasuredBodyMetric>,
    requiredMeasurements: Set<MeasuredBodyMetric>,
    onMeasurementEnabledChanged: (MeasuredBodyMetric, Boolean) -> Unit,
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Measurement Collection", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            MeasuredBodyMetric.entries.forEach { measurement ->
                val required = measurement in requiredMeasurements
                val label = if (required) {
                    "${measurement.label()} (required)"
                } else {
                    measurement.label()
                }
                SettingsCheckRow(
                    label = label,
                    checked = measurement in enabledMeasurements,
                    enabled = !required,
                    onCheckedChange = { onMeasurementEnabledChanged(measurement, it) },
                )
            }
        }
    }
}

@Composable
private fun SettingsCheckRow(
    label: String,
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
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
            modifier = Modifier.size(30.dp),
        )
    }
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
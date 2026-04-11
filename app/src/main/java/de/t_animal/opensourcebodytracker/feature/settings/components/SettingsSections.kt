package de.t_animal.opensourcebodytracker.feature.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
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
                label = stringResource(R.string.analysis_method_bmi_label),
                secondaryLabel = stringResource(R.string.analysis_method_bmi_description),
                checked = bmiEnabled,
                enabled = true,
                onCheckedChange = onBmiEnabledChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCheckRow(
                label = stringResource(R.string.analysis_method_waist_hip_ratio_label),
                secondaryLabel = stringResource(R.string.analysis_method_waist_hip_ratio_description),
                checked = waistHipRatioEnabled,
                enabled = true,
                onCheckedChange = onWaistHipRatioEnabledChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCheckRow(
                label = stringResource(R.string.analysis_method_waist_height_ratio_label),
                secondaryLabel = stringResource(R.string.analysis_method_waist_height_ratio_description),
                checked = waistHeightRatioEnabled,
                enabled = true,
                onCheckedChange = onWaistHeightRatioEnabledChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCheckRow(
                label = stringResource(R.string.analysis_method_navy_body_fat_label),
                secondaryLabel = stringResource(R.string.analysis_method_navy_body_fat_description),
                checked = navyBodyFatEnabled,
                enabled = true,
                onCheckedChange = onNavyBodyFatEnabledChanged,
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsCheckRow(
                label = stringResource(R.string.analysis_method_skinfold_body_fat_label),
                secondaryLabel = stringResource(R.string.analysis_method_skinfold_body_fat_description),
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
    onShowInfo: ((MeasuredBodyMetric) -> Unit)? = null,
) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            MeasuredBodyMetric.entries.forEach { measurement ->
                val required = measurement in requiredMeasurements
                val requiredByMethods = measurementToAnalysisMethods[measurement].orEmpty()
                val secondaryLabel = when {
                    requiredByMethods.isNotEmpty() ->
                        stringResource(R.string.measurement_required_for, requiredByMethods.toAnalysisMethodLabelList())

                    required -> stringResource(R.string.measurement_required)
                    else -> stringResource(R.string.measurement_optional)
                }

                SettingsCheckRow(
                    label = measurement.getLabel(),
                    secondaryLabel = secondaryLabel,
                    checked = measurement in enabledMeasurements,
                    enabled = !required,
                    onCheckedChange = { onMeasurementEnabledChanged(measurement, it) },
                    trailingContent = if (onShowInfo != null) {
                        {
                            IconButton(onClick = { onShowInfo(measurement) }) {
                                Icon(
                                    imageVector = Icons.Outlined.HelpOutline,
                                    contentDescription = stringResource(R.string.cd_metric_info),
                                )
                            }
                        }
                    } else null,
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
    trailingContent: @Composable (() -> Unit)? = null,
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
        trailingContent?.invoke()
    }
}

@Composable
private fun Set<AnalysisMethod>.toAnalysisMethodLabelList(): String {
    val labels = AnalysisMethod.entries
        .filter { it in this }
        .map { it.getLabel() }
    return labels.joinToString(", ")
}

@Composable
private fun AnalysisMethod.getLabel(): String = when (this) {
    AnalysisMethod.Bmi -> stringResource(R.string.analysis_method_bmi_label)
    AnalysisMethod.NavyBodyFat -> stringResource(R.string.analysis_method_navy_body_fat_label)
    AnalysisMethod.Skinfold3SiteBodyFat -> stringResource(R.string.analysis_method_skinfold_body_fat_label)
    AnalysisMethod.WaistHipRatio -> stringResource(R.string.analysis_method_waist_hip_ratio_label)
    AnalysisMethod.WaistHeightRatio -> stringResource(R.string.analysis_method_waist_height_ratio_label)
}

@Composable
private fun MeasuredBodyMetric.getLabel(): String = when (this) {
    MeasuredBodyMetric.Weight -> stringResource(R.string.metric_label_weight)
    MeasuredBodyMetric.BodyFat -> stringResource(R.string.metric_label_body_fat)
    MeasuredBodyMetric.NeckCircumference -> stringResource(R.string.metric_label_neck)
    MeasuredBodyMetric.WaistCircumference -> stringResource(R.string.metric_label_waist)
    MeasuredBodyMetric.HipCircumference -> stringResource(R.string.metric_label_hip)
    MeasuredBodyMetric.ChestCircumference -> stringResource(R.string.metric_label_chest)
    MeasuredBodyMetric.AbdomenCircumference -> stringResource(R.string.metric_label_abdomen)
    MeasuredBodyMetric.ChestSkinfold -> stringResource(R.string.metric_label_chest_skinfold)
    MeasuredBodyMetric.AbdomenSkinfold -> stringResource(R.string.metric_label_abdomen_skinfold)
    MeasuredBodyMetric.ThighSkinfold -> stringResource(R.string.metric_label_thigh_skinfold)
    MeasuredBodyMetric.TricepsSkinfold -> stringResource(R.string.metric_label_triceps_skinfold)
    MeasuredBodyMetric.SuprailiacSkinfold -> stringResource(R.string.metric_label_suprailiac_skinfold)
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

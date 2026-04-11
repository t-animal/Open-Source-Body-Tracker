package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.fullName
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.initialGuidanceOrientation
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.guidanceResId
import de.t_animal.opensourcebodytracker.ui.helpers.styledStringResource
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun MeasurementGuidanceDialog(
    metric: MeasuredBodyMetric?,
    sex: Sex?,
    onDismiss: () -> Unit,
) {
    val guidanceMetric = metric ?: return

    val maxCardHeight = LocalConfiguration.current.screenHeightDp.dp * 0.85f
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxCardHeight),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(
                    text = guidanceMetric.fullName(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = styledStringResource(guidanceMetric.guidanceResId()))
                Spacer(modifier = Modifier.height(12.dp))
                MeasurementGuidanceImage(
                    metric = guidanceMetric,
                    initialSex = sex,
                    initialOrientation = guidanceMetric.initialGuidanceOrientation(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementGuidanceDialogPreview_Male() {
    BodyTrackerTheme {
        MeasurementGuidanceDialog(
            metric = MeasuredBodyMetric.WaistCircumference,
            sex = Sex.Male,
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementGuidanceDialogPreview_NoSex() {
    BodyTrackerTheme {
        MeasurementGuidanceDialog(
            metric = MeasuredBodyMetric.WaistCircumference,
            sex = null,
            onDismiss = {},
        )
    }
}

package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.fullName
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.guidanceResId
import de.t_animal.opensourcebodytracker.ui.helpers.styledStringResource

@Composable
fun MeasurementGuidanceDialog(
    metric: MeasuredBodyMetric?,
    onDismiss: () -> Unit,
) {
    val guidanceMetric = metric ?: return

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = guidanceMetric.fullName(),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = styledStringResource(guidanceMetric.guidanceResId()))
            }
        }
    }
}

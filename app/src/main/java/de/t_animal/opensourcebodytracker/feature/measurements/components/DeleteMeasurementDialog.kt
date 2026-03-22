package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.t_animal.opensourcebodytracker.R

@Composable
fun DeleteMeasurementDialog(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.measurement_delete_dialog_title)) },
        text = { Text(stringResource(R.string.measurement_delete_dialog_body)) },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
    )
}

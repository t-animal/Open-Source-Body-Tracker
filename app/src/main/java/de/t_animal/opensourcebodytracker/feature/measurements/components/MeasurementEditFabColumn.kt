package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MeasurementEditFabColumn(
    hasPhoto: Boolean,
    onTakePhotoClicked: () -> Unit,
    onDeletePhotoClicked: () -> Unit,
    onSaveClicked: () -> Unit,
) {
    Column(modifier = Modifier.imePadding()) {
        FloatingActionButton(
            onClick = if (hasPhoto) onDeletePhotoClicked else onTakePhotoClicked,
        ) {
            Icon(
                imageVector = if (hasPhoto) Icons.Filled.Delete else Icons.Filled.CameraAlt,
                contentDescription = if (hasPhoto) "Delete photo" else "Take photo",
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        ExtendedFloatingActionButton(onClick = onSaveClicked) {
            Text("Save")
        }
    }
}

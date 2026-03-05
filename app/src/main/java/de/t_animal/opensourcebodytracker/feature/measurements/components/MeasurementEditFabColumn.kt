package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NoPhotography
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MeasurementEditFabColumn(
    isCreatingNew: Boolean,
    hasPhoto: Boolean,
    onTakePhotoClicked: () -> Unit,
    onDeletePhotoClicked: () -> Unit,
    onDeleteMeasurementClicked: () -> Unit,
    onSaveClicked: () -> Unit,
) {
    Column(modifier = Modifier.imePadding()) {
        if (!isCreatingNew) {
            FloatingActionButton(onClick = onDeleteMeasurementClicked) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        FloatingActionButton(
            onClick = if (hasPhoto) onDeletePhotoClicked else onTakePhotoClicked,
        ) {
            Icon(
                imageVector = if (hasPhoto) Icons.Filled.NoPhotography else Icons.Filled.CameraAlt,
                contentDescription = if (hasPhoto) "Delete photo" else "Take photo",
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        FloatingActionButton(onClick = onSaveClicked) {
            Icon(
                imageVector = Icons.Filled.Save,
                contentDescription = "Save"
            )
        }
    }
}

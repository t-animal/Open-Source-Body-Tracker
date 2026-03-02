package de.t_animal.opensourcebodytracker.feature.photos.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotoMode

@Composable
fun PhotosModeFab(
    mode: PhotoMode,
    modifier: Modifier = Modifier,
    onEnterCompareModeClicked: () -> Unit,
    onExitModeClicked: () -> Unit,
) {
    FloatingActionButton(
        onClick = {
            if (mode == PhotoMode.NORMAL) {
                onEnterCompareModeClicked()
            } else {
                onExitModeClicked()
            }
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        if (mode == PhotoMode.NORMAL) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                contentDescription = "Enter compare mode",
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Exit compare mode",
            )
        }
    }
}

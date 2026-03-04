package de.t_animal.opensourcebodytracker.feature.photos.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotoMode

@Composable
fun PhotosModeFab(
    mode: PhotoMode,
    modifier: Modifier = Modifier,
    onEnterCompareModeClicked: () -> Unit,
    onEnterAnimateModeClicked: () -> Unit,
    onExitModeClicked: () -> Unit,
) {
    if (mode == PhotoMode.NORMAL) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FloatingActionButton(
                onClick = onEnterCompareModeClicked,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = "Enter compare mode",
                )
            }

            FloatingActionButton(
                onClick = onEnterAnimateModeClicked,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Enter animate mode",
                )
            }
        }
    } else {
        FloatingActionButton(
            onClick = onExitModeClicked,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Exit mode",
            )
        }
    }
}

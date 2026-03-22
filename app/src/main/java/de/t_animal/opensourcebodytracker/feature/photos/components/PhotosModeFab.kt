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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
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
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = stringResource(R.string.cd_enter_compare_mode),
                )
            }

            FloatingActionButton(
                onClick = onEnterAnimateModeClicked,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.cd_enter_animate_mode),
                )
            }
        }
    } else {
        FloatingActionButton(
            onClick = onExitModeClicked,
            modifier = modifier,
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.cd_exit_mode),
            )
        }
    }
}

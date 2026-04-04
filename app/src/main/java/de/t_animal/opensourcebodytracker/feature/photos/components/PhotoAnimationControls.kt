package de.t_animal.opensourcebodytracker.feature.photos.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun PhotoAnimationControls(
    isPlaying: Boolean,
    canSlowDown: Boolean,
    canStepFrames: Boolean,
    canSpeedUp: Boolean,
    onSlower: () -> Unit,
    onPreviousFrame: () -> Unit,
    onPlayPauseToggle: () -> Unit,
    onNextFrame: () -> Unit,
    onFaster: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            IconButton(
                onClick = onSlower,
                enabled = canSlowDown,
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = stringResource(R.string.cd_animation_slower),
                )
            }

            IconButton(
                onClick = onPreviousFrame,
                enabled = canStepFrames,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_animation_previous),
                )
            }

            IconButton(onClick = onPlayPauseToggle) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) stringResource(R.string.cd_animation_pause) else stringResource(R.string.cd_animation_play),
                )
            }

            IconButton(
                onClick = onNextFrame,
                enabled = canStepFrames,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.cd_animation_next),
                )
            }

            IconButton(
                onClick = onFaster,
                enabled = canSpeedUp,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.cd_animation_faster),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoAnimationControlsPlayingPreview() {
    BodyTrackerTheme {
        PhotoAnimationControls(
            isPlaying = true,
            canSlowDown = true,
            canStepFrames = false,
            canSpeedUp = true,
            onSlower = {},
            onPreviousFrame = {},
            onPlayPauseToggle = {},
            onNextFrame = {},
            onFaster = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoAnimationControlsPausedPreview() {
    BodyTrackerTheme {
        PhotoAnimationControls(
            isPlaying = false,
            canSlowDown = false,
            canStepFrames = true,
            canSpeedUp = true,
            onSlower = {},
            onPreviousFrame = {},
            onPlayPauseToggle = {},
            onNextFrame = {},
            onFaster = {},
        )
    }
}
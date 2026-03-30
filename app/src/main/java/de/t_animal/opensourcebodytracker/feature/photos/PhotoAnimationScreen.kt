package de.t_animal.opensourcebodytracker.feature.photos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.feature.photos.components.AnimatedPhotos
import de.t_animal.opensourcebodytracker.feature.photos.components.PhotoAnimationControls
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.ui.components.SecondaryScreenScaffold
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

@Composable
fun PhotoAnimationRoute(
    onNavigateBack: () -> Unit,
) {
    val viewModel: PhotoAnimationViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PhotoAnimationScreen(
        state = state,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun PhotoAnimationScreen(
    state: PhotoAnimationUiState,
    onNavigateBack: () -> Unit,
) {
    SecondaryScreenScaffold(
        title = stringResource(R.string.photos_title_animation),
        onNavigateBack = onNavigateBack,
    ) {
        when (state) {
            PhotoAnimationUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is PhotoAnimationUiState.Loaded -> {
                if (state.hasError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.photos_error_animation_load),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    PhotoAnimationContent(
                        frames = state.frames,
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoAnimationContent(
    frames: List<PhotosItemUiModel>,
) {
    AnimatedPhotos(
        frames = frames,
        modifier = Modifier
            .fillMaxSize(),
    ) { playbackState, playbackActions ->
        PhotoAnimationControls(
            isPlaying = playbackState.isPlaying,
            canSlowDown = playbackState.canSlowDown,
            canStepFrames = playbackState.canStepFrames,
            canSpeedUp = playbackState.canSpeedUp,
            onSlower = playbackActions.onSlower,
            onPreviousFrame = playbackActions.onPreviousFrame,
            onPlayPauseToggle = playbackActions.onPlayPauseToggle,
            onNextFrame = playbackActions.onNextFrame,
            onFaster = playbackActions.onFaster,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoAnimationLoadedPreview() {
    BodyTrackerTheme {
        PhotoAnimationScreen(
            state = PhotoAnimationUiState.Loaded(
                frames = listOf(
                    PhotosItemUiModel(
                        measurementId = 1L,
                        dateEpochMillis = 1_767_916_800_000,
                        photoFile = java.io.File("/tmp/photo_1.jpg"),
                    ),
                    PhotosItemUiModel(
                        measurementId = 2L,
                        dateEpochMillis = 1_769_126_400_000,
                        photoFile = java.io.File("/tmp/photo_2.jpg"),
                    ),
                ),
            ),
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoAnimationErrorPreview() {
    BodyTrackerTheme {
        PhotoAnimationScreen(
            state = PhotoAnimationUiState.Loaded(
                hasError = true,
            ),
            onNavigateBack = {},
        )
    }
}

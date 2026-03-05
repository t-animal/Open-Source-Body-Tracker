package de.t_animal.opensourcebodytracker.feature.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.feature.photos.components.PhotoAnimationControls
import de.t_animal.opensourcebodytracker.feature.photos.helpers.DEFAULT_ANIMATION_SPEED_FPS
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.feature.photos.helpers.canDecreaseSpeed
import de.t_animal.opensourcebodytracker.feature.photos.helpers.canIncreaseSpeed
import de.t_animal.opensourcebodytracker.feature.photos.helpers.frameDelayMillisForSpeed
import de.t_animal.opensourcebodytracker.feature.photos.helpers.nextFasterSpeedFps
import de.t_animal.opensourcebodytracker.feature.photos.helpers.nextFrameIndex
import de.t_animal.opensourcebodytracker.feature.photos.helpers.nextSlowerSpeedFps
import de.t_animal.opensourcebodytracker.feature.photos.helpers.previousFrameIndex
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import kotlinx.coroutines.delay

@Composable
fun PhotoAnimationRoute(
    measurementRepository: MeasurementRepository,
    photoStorage: InternalPhotoStorage,
    selectedMeasurementIds: List<Long>,
) {
    val viewModel: PhotoAnimationViewModel = viewModel(
        factory = PhotoAnimationViewModelFactory(
            measurementRepository = measurementRepository,
            photoStorage = photoStorage,
            selectedMeasurementIds = selectedMeasurementIds,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PhotoAnimationScreen(state = state)
}

@Composable
fun PhotoAnimationScreen(
    state: PhotoAnimationUiState,
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
            if (state.errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage,
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

@Composable
private fun PhotoAnimationContent(
    frames: List<PhotosItemUiModel>,
) {
    if (frames.isEmpty()) {
        return
    }

    val context = LocalContext.current
    var frameIndex by remember(frames) { mutableIntStateOf(0) }
    var isPlaying by remember(frames) { mutableStateOf(true) }
    var speedFps by remember(frames) { mutableFloatStateOf(DEFAULT_ANIMATION_SPEED_FPS) }
    var isPreloading by remember(frames) { mutableStateOf(true) }
    val frameDelayMillis = remember(speedFps) { frameDelayMillisForSpeed(speedFps) }

    LaunchedEffect(frames, context) {
        isPreloading = true
        frameIndex = 0
        speedFps = DEFAULT_ANIMATION_SPEED_FPS
        isPlaying = true

        val imageLoader = context.imageLoader
        frames.forEach { frame ->
            imageLoader.execute(
                ImageRequest.Builder(context)
                    .data(frame.photoFile)
                    .build(),
            )
        }

        isPreloading = false
    }

    LaunchedEffect(frames, isPlaying, frameDelayMillis, isPreloading) {
        if (isPreloading || !isPlaying || frames.size < 2) {
            return@LaunchedEffect
        }

        while (true) {
            delay(frameDelayMillis)
            frameIndex = nextFrameIndex(frameIndex, frames.size)
        }
    }

    val currentFrame = frames[frameIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            if (isPreloading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                AsyncImage(
                    model = currentFrame.photoFile,
                    contentDescription = "Animation frame",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Text(
            text = formatEpochMillisToLocalizedNumericDate(currentFrame.dateEpochMillis),
            style = MaterialTheme.typography.bodyLarge,
        )

        PhotoAnimationControls(
            isPlaying = isPlaying,
            canSlowDown = canDecreaseSpeed(speedFps),
            canStepFrames = !isPlaying && !isPreloading,
            canSpeedUp = canIncreaseSpeed(speedFps),
            onSlower = {
                speedFps = nextSlowerSpeedFps(speedFps)
            },
            onPreviousFrame = {
                frameIndex = previousFrameIndex(frameIndex, frames.size)
            },
            onPlayPauseToggle = {
                isPlaying = !isPlaying
            },
            onNextFrame = {
                frameIndex = nextFrameIndex(frameIndex, frames.size)
            },
            onFaster = {
                speedFps = nextFasterSpeedFps(speedFps)
            },
        )

        Text(
            text = "Frame Rate: ${formatSpeedFps(speedFps)} fps",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun formatSpeedFps(speedFps: Float): String {
    return if (speedFps % 1f == 0f) {
        speedFps.toInt().toString()
    } else {
        speedFps.toString()
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
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoAnimationErrorPreview() {
    BodyTrackerTheme {
        PhotoAnimationScreen(
            state = PhotoAnimationUiState.Loaded(
                errorMessage = "Unable to load at least 2 photos for animation",
            ),
        )
    }
}

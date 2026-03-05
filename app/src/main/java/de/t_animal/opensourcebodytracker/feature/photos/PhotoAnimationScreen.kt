package de.t_animal.opensourcebodytracker.feature.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import kotlinx.coroutines.delay

const val ANIMATION_FRAME_RATE = 5

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

    var frameIndex by remember(frames) { mutableIntStateOf(0) }
    val frameDelayMillis = remember { (1000L / ANIMATION_FRAME_RATE).coerceAtLeast(1L) }

    LaunchedEffect(frames) {
        frameIndex = 0
        while (true) {
            delay(frameDelayMillis)
            frameIndex = (frameIndex + 1) % frames.size
        }
    }

    val currentFrame = frames[frameIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AsyncImage(
                model = currentFrame.photoFile,
                contentDescription = "Animation frame",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Text(
            text = formatEpochMillisToLocalizedNumericDate(currentFrame.dateEpochMillis),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = "Frame Rate: $ANIMATION_FRAME_RATE fps",
            style = MaterialTheme.typography.bodyMedium,
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

package de.t_animal.opensourcebodytracker.feature.photos.components

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import coil.size.Scale
import de.t_animal.opensourcebodytracker.feature.photos.helpers.DEFAULT_ANIMATION_SPEED_FPS
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.feature.photos.helpers.canDecreaseSpeed
import de.t_animal.opensourcebodytracker.feature.photos.helpers.canIncreaseSpeed
import de.t_animal.opensourcebodytracker.feature.photos.helpers.estimateDecodedMemoryBytes
import de.t_animal.opensourcebodytracker.feature.photos.helpers.frameDelayMillisForSpeed
import de.t_animal.opensourcebodytracker.feature.photos.helpers.isWithinPreloadMemoryBudget
import de.t_animal.opensourcebodytracker.feature.photos.helpers.maxPreloadBudgetBytes
import de.t_animal.opensourcebodytracker.feature.photos.helpers.nextFasterSpeedFps
import de.t_animal.opensourcebodytracker.feature.photos.helpers.nextFrameIndex
import de.t_animal.opensourcebodytracker.feature.photos.helpers.nextSlowerSpeedFps
import de.t_animal.opensourcebodytracker.feature.photos.helpers.previousFrameIndex
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class AnimatedPhotosPlaybackState(
    val isPlaying: Boolean,
    val speedFps: Float,
    val canSlowDown: Boolean,
    val canStepFrames: Boolean,
    val canSpeedUp: Boolean,
)

data class AnimatedPhotosPlaybackActions(
    val onSlower: () -> Unit,
    val onPreviousFrame: () -> Unit,
    val onPlayPauseToggle: () -> Unit,
    val onNextFrame: () -> Unit,
    val onFaster: () -> Unit,
)

@Composable
fun AnimatedPhotos(
    frames: List<PhotosItemUiModel>,
    modifier: Modifier = Modifier,
    controls: @Composable (AnimatedPhotosPlaybackState, AnimatedPhotosPlaybackActions) -> Unit,
) {
    if (frames.isEmpty()) {
        return
    }

    var frameIndex by remember(frames) { mutableIntStateOf(0) }
    var isPlaying by remember(frames) { mutableStateOf(true) }
    var speedFps by remember(frames) { mutableFloatStateOf(DEFAULT_ANIMATION_SPEED_FPS) }
    var decodeTargetSize by remember(frames) { mutableStateOf(IntSize.Zero) }
    var playbackFrames by remember(frames) { mutableStateOf<List<DecodedAnimationFrame>>(emptyList()) }
    val frameDelayMillis = remember(speedFps) { frameDelayMillisForSpeed(speedFps) }
    val decodedAnimationState by rememberDecodedAnimationState(
        frames = frames,
        targetSizePx = decodeTargetSize,
    )

    LaunchedEffect(frames) {
        frameIndex = 0
        speedFps = DEFAULT_ANIMATION_SPEED_FPS
        isPlaying = true
        playbackFrames = emptyList()
    }

    LaunchedEffect(decodedAnimationState) {
        val readyFrames = (decodedAnimationState as? DecodedAnimationState.Ready)?.frames ?: return@LaunchedEffect
        playbackFrames = readyFrames
    }

    LaunchedEffect(playbackFrames.size) {
        frameIndex = if (playbackFrames.isNotEmpty()) {
            frameIndex.coerceIn(0, playbackFrames.lastIndex)
        } else {
            0
        }
    }

    LaunchedEffect(playbackFrames.size, isPlaying, frameDelayMillis) {
        if (!isPlaying || playbackFrames.size < 2) {
            return@LaunchedEffect
        }

        while (true) {
            delay(frameDelayMillis)
            frameIndex = nextFrameIndex(frameIndex, playbackFrames.size)
        }
    }

    val currentFrame = playbackFrames.getOrNull(frameIndex)
    val isReady = playbackFrames.isNotEmpty()

    val playbackState = AnimatedPhotosPlaybackState(
        isPlaying = isPlaying,
        speedFps = speedFps,
        canSlowDown = isReady && canDecreaseSpeed(speedFps),
        canStepFrames = isReady && !isPlaying,
        canSpeedUp = isReady && canIncreaseSpeed(speedFps),
    )
    val playbackActions = AnimatedPhotosPlaybackActions(
        onSlower = {
            if (isReady) {
                speedFps = nextSlowerSpeedFps(speedFps)
            }
        },
        onPreviousFrame = {
            if (playbackFrames.isNotEmpty()) {
                frameIndex = previousFrameIndex(frameIndex, playbackFrames.size)
            }
        },
        onPlayPauseToggle = {
            if (isReady) {
                isPlaying = !isPlaying
            }
        },
        onNextFrame = {
            if (playbackFrames.isNotEmpty()) {
                frameIndex = nextFrameIndex(frameIndex, playbackFrames.size)
            }
        },
        onFaster = {
            if (isReady) {
                speedFps = nextFasterSpeedFps(speedFps)
            }
        },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onSizeChanged { size ->
                    if (decodeTargetSize == IntSize.Zero && size.width > 0 && size.height > 0) {
                        decodeTargetSize = size
                    }
                }
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            val errorMessage = (decodedAnimationState as? DecodedAnimationState.Error)?.message

            when {
                errorMessage != null && playbackFrames.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                currentFrame == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    Image(
                        bitmap = currentFrame.imageBitmap,
                        contentDescription = "Animation frame",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        if (currentFrame != null) {
            Text(
                text = formatEpochMillisToLocalizedNumericDate(currentFrame.dateEpochMillis),
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        controls(playbackState, playbackActions)

        Text(
            text = "Frame Rate: ${formatSpeedFps(playbackState.speedFps)} fps",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private sealed interface DecodedAnimationState {
    data object WaitingForSize : DecodedAnimationState

    data object Loading : DecodedAnimationState

    data class Ready(
        val frames: List<DecodedAnimationFrame>,
    ) : DecodedAnimationState

    data class Error(
        val message: String,
    ) : DecodedAnimationState
}

private data class DecodedAnimationFrame(
    val dateEpochMillis: Long,
    val imageBitmap: ImageBitmap,
)

@Composable
private fun rememberDecodedAnimationState(
    frames: List<PhotosItemUiModel>,
    targetSizePx: IntSize,
): State<DecodedAnimationState> {
    val context = LocalContext.current

    return produceState<DecodedAnimationState>(
        initialValue = DecodedAnimationState.WaitingForSize,
        key1 = frames,
        key2 = targetSizePx,
        key3 = context,
    ) {
        if (targetSizePx.width <= 0 || targetSizePx.height <= 0) {
            value = DecodedAnimationState.WaitingForSize
            return@produceState
        }

        val estimatedDecodedBytes = estimateDecodedMemoryBytes(
            widthPx = targetSizePx.width,
            heightPx = targetSizePx.height,
            frameCount = frames.size,
        )
        val maxHeapBytes = Runtime.getRuntime().maxMemory()

        if (!isWithinPreloadMemoryBudget(estimatedDecodedBytes, maxHeapBytes)) {
            val estimatedMiB = bytesToMiBString(estimatedDecodedBytes)
            val budgetMiB = bytesToMiBString(maxPreloadBudgetBytes(maxHeapBytes))
            value = DecodedAnimationState.Error(
                message = "Too many images selected for this app's RAM. " +
                    "Decoded memory estimate ($estimatedMiB MiB) exceeds " +
                    "the 50% heap budget ($budgetMiB MiB) of the RAM" +
                    "that Android made available to this app.",
            )
            return@produceState
        }

        value = DecodedAnimationState.Loading

        val decodedFrames = withContext(Dispatchers.IO) {
            val decoded = mutableListOf<DecodedAnimationFrame>()
            for (frame in frames) {
                val decodedFrame = decodeFrameToImageBitmap(
                    context = context,
                    frame = frame,
                    targetSizePx = targetSizePx,
                )
                if (decodedFrame != null) {
                    decoded += decodedFrame
                }
            }
            decoded
        }

        value = if (decodedFrames.size < 2) {
            DecodedAnimationState.Error(
                message = "Unable to decode at least 2 photos for animation",
            )
        } else {
            DecodedAnimationState.Ready(decodedFrames)
        }
    }
}

private suspend fun decodeFrameToImageBitmap(
    context: Context,
    frame: PhotosItemUiModel,
    targetSizePx: IntSize,
): DecodedAnimationFrame? {
    val result = context.imageLoader.execute(
        ImageRequest.Builder(context)
            .data(frame.photoFile)
            .size(targetSizePx.width, targetSizePx.height)
            .scale(Scale.FIT)
            .precision(Precision.INEXACT)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build(),
    ) as? SuccessResult ?: return null

    val bitmap = result.drawable.toBitmap(config = Bitmap.Config.ARGB_8888)

    return DecodedAnimationFrame(
        dateEpochMillis = frame.dateEpochMillis,
        imageBitmap = bitmap.asImageBitmap(),
    )
}

private fun bytesToMiBString(bytes: Long): String {
    val mebibytes = bytes / (1024f * 1024f)
    return String.format(Locale.US, "%.1f", mebibytes)
}

private fun formatSpeedFps(speedFps: Float): String {
    return if (speedFps % 1f == 0f) {
        speedFps.toInt().toString()
    } else {
        speedFps.toString()
    }
}
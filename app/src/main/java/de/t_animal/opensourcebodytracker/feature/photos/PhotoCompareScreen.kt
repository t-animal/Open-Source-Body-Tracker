package de.t_animal.opensourcebodytracker.feature.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.io.File

@Composable
fun PhotoCompareRoute() {
    val viewModel: PhotoCompareViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    PhotoCompareScreen(state = state)
}

@Composable
fun PhotoCompareScreen(state: PhotoCompareUiState) {
    when (state) {
        PhotoCompareUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        is PhotoCompareUiState.Loaded -> {
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
            } else if (state.left != null && state.right != null) {
                PhotoCompareContent(
                    left = state.left,
                    right = state.right,
                )
            }
        }
    }
}

@Composable
private fun PhotoCompareContent(
    left: PhotosItemUiModel,
    right: PhotosItemUiModel,
) {
    var sliderFraction by remember { mutableFloatStateOf(0.5f) }

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
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, dragAmount ->
                        change.consume()
                        sliderFraction = (sliderFraction + (dragAmount / size.width)).coerceIn(0f, 1f)
                    }
                },
        ) {
            AsyncImage(
                model = right.photoFile,
                contentDescription = "Right photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
            )

            AsyncImage(
                model = left.photoFile,
                contentDescription = "Left photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        clipRect(right = size.width * sliderFraction) {
                            this@drawWithContent.drawContent()
                        }
                    },
            )

            val sliderOffset = maxWidth * sliderFraction
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = sliderOffset - 1.dp)
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = sliderOffset - 12.dp)
                    .height(24.dp)
                    .width(24.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatEpochMillisToLocalizedNumericDate(left.dateEpochMillis),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = formatEpochMillisToLocalizedNumericDate(right.dateEpochMillis),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoCompareScreenPreview() {
    BodyTrackerTheme {
        PhotoCompareScreen(
            state = PhotoCompareUiState.Loaded(
                left = PhotosItemUiModel(
                    measurementId = 1L,
                    dateEpochMillis = 1_767_916_800_000,
                    photoFile = File("/tmp/photo_1.jpg"),
                ),
                right = PhotosItemUiModel(
                    measurementId = 2L,
                    dateEpochMillis = 1_769_126_400_000,
                    photoFile = File("/tmp/photo_2.jpg"),
                ),
            ),
        )
    }
}

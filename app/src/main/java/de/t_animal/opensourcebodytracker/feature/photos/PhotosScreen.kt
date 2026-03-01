package de.t_animal.opensourcebodytracker.feature.photos
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import java.io.File

data class PhotosItemUiModel(
    val measurementId: Long,
    val dateEpochMillis: Long,
    val photoFile: File,
)

data class PhotosUiState(
    val items: List<PhotosItemUiModel> = emptyList(),
)

class PhotosViewModel(
    measurementRepository: MeasurementRepository,
    photoStorage: InternalPhotoStorage,
) : ViewModel() {
    val uiState: StateFlow<PhotosUiState> = measurementRepository.observeAll()
        .map { measurements ->
            PhotosUiState(
                items = measurements
                    .asSequence()
                    .filter { !it.photoFilePath.isNullOrBlank() }
                    .map { measurement ->
                        PhotosItemUiModel(
                            measurementId = measurement.id,
                            dateEpochMillis = measurement.dateEpochMillis,
                            photoFile = photoStorage.resolvePhotoFile(
                                measurement.photoFilePath.orEmpty(),
                            ),
                        )
                    }
                    .toList(),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PhotosUiState(),
        )
}

class PhotosViewModelFactory(
    private val measurementRepository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhotosViewModel(
            measurementRepository = measurementRepository,
            photoStorage = photoStorage,
        ) as T
    }
}

@Composable
fun PhotosRoute(
    measurementRepository: MeasurementRepository,
    photoStorage: InternalPhotoStorage,
) {
    val vm: PhotosViewModel = viewModel(
        factory = PhotosViewModelFactory(
            measurementRepository = measurementRepository,
            photoStorage = photoStorage,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    PhotosScreen(state = state)
}

@Composable
fun PhotosScreen(
    state: PhotosUiState,
) {
    if (state.items.isEmpty()) {
        Text(
            text = "No photos yet",
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = state.items,
            key = { it.measurementId },
        ) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(12.dp)),
                ) {
                    AsyncImage(
                        model = item.photoFile,
                        contentDescription = "Progress photo",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = formatEpochMillisToLocalizedNumericDate(item.dateEpochMillis),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhotosScreenPreview() {
    BodyTrackerTheme {
        PhotosScreen(
            state = PhotosUiState(
                items = listOf(
                    PhotosItemUiModel(
                        measurementId = 3L,
                        dateEpochMillis = 1_769_990_400_000,
                        photoFile = File("/tmp/photo_3.jpg"),
                    ),
                    PhotosItemUiModel(
                        measurementId = 2L,
                        dateEpochMillis = 1_769_126_400_000,
                        photoFile = File("/tmp/photo_2.jpg"),
                    ),
                    PhotosItemUiModel(
                        measurementId = 1L,
                        dateEpochMillis = 1_767_916_800_000,
                        photoFile = File("/tmp/photo_1.jpg"),
                    ),
                ),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PhotosScreenEmptyPreview() {
    BodyTrackerTheme {
        PhotosScreen(state = PhotosUiState())
    }
}

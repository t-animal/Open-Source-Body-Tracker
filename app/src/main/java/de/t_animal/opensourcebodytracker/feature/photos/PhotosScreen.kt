package de.t_animal.opensourcebodytracker.feature.photos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

data class PhotosItemUiModel(
    val measurementId: Long,
    val dateEpochMillis: Long,
)

data class PhotosUiState(
    val items: List<PhotosItemUiModel> = emptyList(),
)

class PhotosViewModel(
    measurementRepository: MeasurementRepository,
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
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhotosViewModel(measurementRepository = measurementRepository) as T
    }
}

@Composable
fun PhotosRoute(
    measurementRepository: MeasurementRepository,
    onOpenMeasurement: (Long) -> Unit,
) {
    val vm: PhotosViewModel = viewModel(
        factory = PhotosViewModelFactory(measurementRepository = measurementRepository),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    PhotosScreen(
        state = state,
        onOpenMeasurement = onOpenMeasurement,
    )
}

@Composable
fun PhotosScreen(
    state: PhotosUiState,
    onOpenMeasurement: (Long) -> Unit,
) {
    if (state.items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("No photos yet")
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = state.items,
            key = { it.measurementId },
        ) { item ->
            Text(
                text = formatEpochMillisToLocalizedNumericDate(item.dateEpochMillis),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMeasurement(item.measurementId) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            )
            HorizontalDivider()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotosScreenPreview() {
    BodyTrackerTheme {
        PhotosScreen(
            state = PhotosUiState(
                items = listOf(
                    PhotosItemUiModel(measurementId = 3L, dateEpochMillis = 1_769_990_400_000),
                    PhotosItemUiModel(measurementId = 2L, dateEpochMillis = 1_769_126_400_000),
                    PhotosItemUiModel(measurementId = 1L, dateEpochMillis = 1_767_916_800_000),
                ),
            ),
            onOpenMeasurement = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotosScreenEmptyPreview() {
    BodyTrackerTheme {
        PhotosScreen(
            state = PhotosUiState(),
            onOpenMeasurement = {},
        )
    }
}

package de.t_animal.opensourcebodytracker.feature.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotoMode
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosUiState
import de.t_animal.opensourcebodytracker.feature.photos.helpers.orderedCompareSelection
import de.t_animal.opensourcebodytracker.feature.photos.helpers.togglePhotoSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private data class PhotosSelectionState(
    val mode: PhotoMode = PhotoMode.NORMAL,
    val selectedMeasurementIds: List<Long> = emptyList(),
    val snackbarMessage: String? = null,
)

class PhotosViewModel(
    measurementRepository: MeasurementRepository,
    photoStorage: InternalPhotoStorage,
) : ViewModel() {
    private val selectionState = MutableStateFlow(PhotosSelectionState())

    private val photoItems: StateFlow<List<PhotosItemUiModel>> = measurementRepository.observeAll()
        .map { measurements ->
            measurements
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
                .toList()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val uiState: StateFlow<PhotosUiState> = combine(
        photoItems,
        selectionState,
    ) { items, selection ->
        val availableMeasurementIds = items.map { it.measurementId }.toSet()
        val selectedMeasurementIds = selection.selectedMeasurementIds
            .filter { it in availableMeasurementIds }

        PhotosUiState(
            items = items,
            mode = selection.mode,
            selectedMeasurementIds = selectedMeasurementIds,
            snackbarMessage = selection.snackbarMessage,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PhotosUiState(),
    )

    fun onEnterCompareModeClicked() {
        selectionState.update {
            it.copy(
                mode = PhotoMode.COMPARE,
                selectedMeasurementIds = emptyList(),
                snackbarMessage = null,
            )
        }
    }

    fun onExitModeClicked() {
        selectionState.update {
            it.copy(
                mode = PhotoMode.NORMAL,
                selectedMeasurementIds = emptyList(),
                snackbarMessage = null,
            )
        }
    }

    fun onPhotoClicked(measurementId: Long) {
        selectionState.update { currentState ->
            if (currentState.mode != PhotoMode.COMPARE) {
                return@update currentState
            }

            val selectionResult = togglePhotoSelection(
                selectedMeasurementIds = currentState.selectedMeasurementIds,
                clickedMeasurementId = measurementId,
            )

            if (selectionResult.selectionLimitReached) {
                currentState.copy(snackbarMessage = "You can select at most 2 photos")
            } else {
                currentState.copy(
                    selectedMeasurementIds = selectionResult.selectedMeasurementIds,
                    snackbarMessage = null,
                )
            }
        }
    }

    fun onSnackbarShown() {
        selectionState.update { currentState ->
            currentState.copy(snackbarMessage = null)
        }
    }

    fun consumeCompareSelection(): Pair<Long, Long>? {
        return orderedCompareSelection(
            selectedMeasurementIds = uiState.value.selectedMeasurementIds,
            items = uiState.value.items,
        )
    }
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

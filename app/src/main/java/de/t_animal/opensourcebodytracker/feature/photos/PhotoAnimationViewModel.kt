package de.t_animal.opensourcebodytracker.feature.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.photos.PersistedPhotoPath
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PhotoAnimationUiState {
    data object Loading : PhotoAnimationUiState

    data class Loaded(
        val frames: List<PhotosItemUiModel> = emptyList(),
        val errorMessage: String? = null,
    ) : PhotoAnimationUiState
}

@HiltViewModel
class PhotoAnimationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val measurementRepository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
) : ViewModel() {
    private val selectedMeasurementIds: List<Long> = Routes.parsePhotoAnimateIds(savedStateHandle)

    private val mutableUiState = MutableStateFlow<PhotoAnimationUiState>(PhotoAnimationUiState.Loading)
    val uiState: StateFlow<PhotoAnimationUiState> = mutableUiState.asStateFlow()

    init {
        loadAnimationPhotos()
    }

    private fun loadAnimationPhotos() {
        viewModelScope.launch {
            val measurementById = selectedMeasurementIds.associateWith { measurementId ->
                measurementRepository.getById(measurementId)
            }

            val frames = buildAnimationFrameItems(
                selectedMeasurementIds = selectedMeasurementIds,
                measurementsById = measurementById,
                resolvePhotoFile = photoStorage::resolvePhotoFile,
            )

            if (frames.size < 2) {
                mutableUiState.update {
                    PhotoAnimationUiState.Loaded(
                        errorMessage = "Unable to load at least 2 photos for animation",
                    )
                }
                return@launch
            }

            mutableUiState.update {
                PhotoAnimationUiState.Loaded(
                    frames = frames,
                    errorMessage = null,
                )
            }
        }
    }
}

internal fun buildAnimationFrameItems(
    selectedMeasurementIds: List<Long>,
    measurementsById: Map<Long, BodyMeasurement?>,
    resolvePhotoFile: (PersistedPhotoPath) -> File,
    fileExists: (File) -> Boolean = { file -> file.exists() },
): List<PhotosItemUiModel> {
    return selectedMeasurementIds.mapNotNull { measurementId ->
        val measurement = measurementsById[measurementId] ?: return@mapNotNull null
        val photoPath = measurement.photoFilePath ?: return@mapNotNull null
        val photoFile = resolvePhotoFile(photoPath)
        if (!fileExists(photoFile)) {
            return@mapNotNull null
        }
        PhotosItemUiModel(
            measurementId = measurement.id,
            dateEpochMillis = measurement.dateEpochMillis,
            photoFile = photoFile,
        )
    }
}


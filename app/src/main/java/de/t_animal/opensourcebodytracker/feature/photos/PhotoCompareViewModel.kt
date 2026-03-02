package de.t_animal.opensourcebodytracker.feature.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PhotoCompareUiState {
    data object Loading : PhotoCompareUiState

    data class Loaded(
        val left: PhotosItemUiModel? = null,
        val right: PhotosItemUiModel? = null,
        val errorMessage: String? = null,
    ) : PhotoCompareUiState
}

class PhotoCompareViewModel(
    private val measurementRepository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
    private val leftMeasurementId: Long,
    private val rightMeasurementId: Long,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow<PhotoCompareUiState>(PhotoCompareUiState.Loading)
    val uiState: StateFlow<PhotoCompareUiState> = mutableUiState.asStateFlow()

    init {
        loadComparePhotos()
    }

    private fun loadComparePhotos() {
        viewModelScope.launch {
            val leftMeasurement = measurementRepository.getById(leftMeasurementId)
            val rightMeasurement = measurementRepository.getById(rightMeasurementId)

            val leftItem = leftMeasurement
                ?.photoFilePath
                ?.takeIf { it.isNotBlank() }
                ?.let { photoPath ->
                    PhotosItemUiModel(
                        measurementId = leftMeasurement.id,
                        dateEpochMillis = leftMeasurement.dateEpochMillis,
                        photoFile = photoStorage.resolvePhotoFile(photoPath),
                    )
                }

            val rightItem = rightMeasurement
                ?.photoFilePath
                ?.takeIf { it.isNotBlank() }
                ?.let { photoPath ->
                    PhotosItemUiModel(
                        measurementId = rightMeasurement.id,
                        dateEpochMillis = rightMeasurement.dateEpochMillis,
                        photoFile = photoStorage.resolvePhotoFile(photoPath),
                    )
                }

            val missingPhoto = leftItem == null || rightItem == null ||
                !leftItem.photoFile.exists() ||
                !rightItem.photoFile.exists()

            if (missingPhoto) {
                mutableUiState.update {
                    PhotoCompareUiState.Loaded(
                        errorMessage = "Unable to load one or both selected photos",
                    )
                }
                return@launch
            }

            mutableUiState.update {
                PhotoCompareUiState.Loaded(
                    left = leftItem,
                    right = rightItem,
                    errorMessage = null,
                )
            }
        }
    }
}

class PhotoCompareViewModelFactory(
    private val measurementRepository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
    private val leftMeasurementId: Long,
    private val rightMeasurementId: Long,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhotoCompareViewModel(
            measurementRepository = measurementRepository,
            photoStorage = photoStorage,
            leftMeasurementId = leftMeasurementId,
            rightMeasurementId = rightMeasurementId,
        ) as T
    }
}

package de.t_animal.opensourcebodytracker.feature.photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.feature.photos.helpers.toPhotoItemOrNull
import de.t_animal.opensourcebodytracker.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
        val hasError: Boolean = false,
    ) : PhotoCompareUiState
}

@HiltViewModel
class PhotoCompareViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val measurementRepository: MeasurementRepository,
    private val photoStorage: InternalPhotoStorage,
) : ViewModel() {
    private val leftMeasurementId: Long
    private val rightMeasurementId: Long

    private val mutableUiState = MutableStateFlow<PhotoCompareUiState>(PhotoCompareUiState.Loading)
    val uiState: StateFlow<PhotoCompareUiState> = mutableUiState.asStateFlow()

    init {
        val (left, right) = Routes.parsePhotoCompareIds(savedStateHandle)
        leftMeasurementId = left
        rightMeasurementId = right
        loadComparePhotos()
    }

    private fun loadComparePhotos() {
        viewModelScope.launch {
            val leftMeasurement = measurementRepository.getById(leftMeasurementId)
            val rightMeasurement = measurementRepository.getById(rightMeasurementId)

            val leftItem = leftMeasurement?.toPhotoItemOrNull(photoStorage)
            val rightItem = rightMeasurement?.toPhotoItemOrNull(photoStorage)

            val missingPhoto = leftItem == null || rightItem == null ||
                !leftItem.photoFile.exists() ||
                !rightItem.photoFile.exists()

            if (missingPhoto) {
                mutableUiState.update {
                    PhotoCompareUiState.Loaded(
                        hasError = true,
                    )
                }
                return@launch
            }

            mutableUiState.update {
                PhotoCompareUiState.Loaded(
                    left = leftItem,
                    right = rightItem,
                    hasError = false,
                )
            }
        }
    }
}


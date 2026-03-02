package de.t_animal.opensourcebodytracker.feature.photos
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.feature.photos.components.CompareSelectionBottomBar
import de.t_animal.opensourcebodytracker.feature.photos.components.PhotosFeed
import de.t_animal.opensourcebodytracker.feature.photos.components.PhotosModeFab
import de.t_animal.opensourcebodytracker.feature.photos.components.PhotosScreenSnackbarHost
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotoMode
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosUiState
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.io.File

@Composable
fun PhotosRoute(
    measurementRepository: MeasurementRepository,
    photoStorage: InternalPhotoStorage,
    onOpenCompare: (leftMeasurementId: Long, rightMeasurementId: Long) -> Unit,
) {
    val vm: PhotosViewModel = viewModel(
        factory = PhotosViewModelFactory(
            measurementRepository = measurementRepository,
            photoStorage = photoStorage,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.snackbarMessage) {
        val message = state.snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = message)
        vm.onSnackbarShown()
    }

    PhotosScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEnterCompareModeClicked = vm::onEnterCompareModeClicked,
        onExitModeClicked = vm::onExitModeClicked,
        onPhotoClicked = vm::onPhotoClicked,
        onCompareClicked = {
            vm.consumeCompareSelection()?.let { (leftMeasurementId, rightMeasurementId) ->
                onOpenCompare(leftMeasurementId, rightMeasurementId)
            }
        },
    )
}

@Composable
fun PhotosScreen(
    state: PhotosUiState,
    snackbarHostState: SnackbarHostState,
    onEnterCompareModeClicked: () -> Unit,
    onExitModeClicked: () -> Unit,
    onPhotoClicked: (Long) -> Unit,
    onCompareClicked: () -> Unit,
) {
    val selectedIds = state.selectedMeasurementIds.toSet()
    val selectedItems = state.selectedMeasurementIds.mapNotNull { selectedId ->
        state.items.firstOrNull { item -> item.measurementId == selectedId }
    }
    val isCompareBottomBarVisible = state.mode == PhotoMode.COMPARE && selectedItems.isNotEmpty()

    val compareEnabled = state.mode == PhotoMode.COMPARE && selectedItems.size == 2

    val listBottomPadding = if (state.mode == PhotoMode.COMPARE) {
        if (selectedItems.isNotEmpty()) {
            170.dp
        } else {
            96.dp
        }
    } else {
        96.dp
    }

    Box(modifier = Modifier.fillMaxSize()) {
        PhotosFeed(
            items = state.items,
            selectedIds = selectedIds,
            mode = state.mode,
            listBottomPadding = listBottomPadding,
            onPhotoClicked = onPhotoClicked,
        )

        if (isCompareBottomBarVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
            ) {
                CompareSelectionBottomBar(
                    selectedItems = selectedItems,
                    compareEnabled = compareEnabled,
                    onCompareClicked = onCompareClicked,
                )
            }
        }

        PhotosModeFab(
            mode = state.mode,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (state.mode == PhotoMode.COMPARE) 100.dp else 24.dp),
            onEnterCompareModeClicked = onEnterCompareModeClicked,
            onExitModeClicked = onExitModeClicked,
        )

        PhotosScreenSnackbarHost(
            snackbarHostState = snackbarHostState,
            isCompareBottomBarVisible = isCompareBottomBarVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )
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
            snackbarHostState = remember { SnackbarHostState() },
            onEnterCompareModeClicked = {},
            onExitModeClicked = {},
            onPhotoClicked = {},
            onCompareClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PhotosScreenEmptyPreview() {
    BodyTrackerTheme {
        PhotosScreen(
            state = PhotosUiState(),
            snackbarHostState = remember { SnackbarHostState() },
            onEnterCompareModeClicked = {},
            onExitModeClicked = {},
            onPhotoClicked = {},
            onCompareClicked = {},
        )
    }
}

package de.t_animal.opensourcebodytracker.feature.photos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.ui.components.PhotoPreviewDialog
import de.t_animal.opensourcebodytracker.feature.photos.components.AnimateSelectionBottomBar
import de.t_animal.opensourcebodytracker.feature.photos.components.CompareSelectionBottomBar
import de.t_animal.opensourcebodytracker.feature.photos.components.PhotosFeed
import de.t_animal.opensourcebodytracker.feature.photos.components.PhotosModeFab
import de.t_animal.opensourcebodytracker.feature.photos.components.PhotosScreenSnackbarHost
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotoMode
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosItemUiModel
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosSnackbarMessage
import de.t_animal.opensourcebodytracker.feature.photos.helpers.PhotosUiState
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.io.File

@Composable
fun PhotosRoute(
    onOpenCompare: (leftMeasurementId: Long, rightMeasurementId: Long) -> Unit,
    onOpenAnimate: (selectedMeasurementIds: List<Long>) -> Unit,
) {
    val vm: PhotosViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val snackbarMessage = when (state.snackbarMessage) {
        PhotosSnackbarMessage.SelectionLimitReached -> stringResource(R.string.photos_snackbar_selection_limit)
        PhotosSnackbarMessage.MinimumSelectionRequired -> stringResource(R.string.photos_snackbar_minimum_selection)
        null -> null
    }

    LaunchedEffect(state.snackbarMessage) {
        val message = snackbarMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = message)
        vm.onSnackbarShown()
    }

    PhotosScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onEnterCompareModeClicked = vm::onEnterCompareModeClicked,
        onEnterAnimateModeClicked = vm::onEnterAnimateModeClicked,
        onExitModeClicked = vm::onExitModeClicked,
        onPhotoClicked = vm::onPhotoClicked,
        onCompareClicked = {
            vm.consumeCompareSelection()?.let { (leftMeasurementId, rightMeasurementId) ->
                onOpenCompare(leftMeasurementId, rightMeasurementId)
            }
        },
        onAnimateClicked = {
            vm.consumeAnimateSelectionOrShowError()?.let(onOpenAnimate)
        },
    )
}

@Composable
fun PhotosScreen(
    state: PhotosUiState,
    snackbarHostState: SnackbarHostState,
    onEnterCompareModeClicked: () -> Unit,
    onEnterAnimateModeClicked: () -> Unit,
    onExitModeClicked: () -> Unit,
    onPhotoClicked: (Long) -> Unit,
    onCompareClicked: () -> Unit,
    onAnimateClicked: () -> Unit,
) {
    var previewPhoto by remember { mutableStateOf<File?>(null) }
    val selectedIds = state.selectedMeasurementIds.toSet()
    val selectedItems = state.selectedMeasurementIds.mapNotNull { selectedId ->
        state.items.firstOrNull { item -> item.measurementId == selectedId }
    }
    val isSelectionMode = state.mode != PhotoMode.NORMAL
    val isCompareMode = state.mode == PhotoMode.COMPARE
    val isAnimateMode = state.mode == PhotoMode.ANIMATE
    val isCompareBottomBarVisible = isCompareMode && selectedItems.isNotEmpty()
    val isAnimateBottomBarVisible = isAnimateMode && selectedItems.isNotEmpty()
    val isSelectionBottomBarVisible = isCompareBottomBarVisible || isAnimateBottomBarVisible

    val compareEnabled = isCompareMode && selectedItems.size == 2
    val animateEnabled = isAnimateMode && selectedItems.size >= 2

    val listBottomPadding = when (state.mode) {
        PhotoMode.NORMAL -> 136.dp
        PhotoMode.COMPARE -> {
            if (selectedItems.isNotEmpty()) {
                170.dp
            } else {
                96.dp
            }
        }
        PhotoMode.ANIMATE -> {
            if (selectedItems.isNotEmpty()) {
                130.dp
            } else {
                96.dp
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        PhotosFeed(
            items = state.items,
            selectedIds = selectedIds,
            isSelectionMode = isSelectionMode,
            listBottomPadding = listBottomPadding,
            onPhotoClicked = { measurementId ->
                if (state.mode == PhotoMode.NORMAL) {
                    previewPhoto = state.items
                        .firstOrNull { item -> item.measurementId == measurementId }
                        ?.photoFile
                } else {
                    onPhotoClicked(measurementId)
                }
            },
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

        if (isAnimateBottomBarVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
            ) {
                AnimateSelectionBottomBar(
                    selectedCount = selectedItems.size,
                    playEnabled = animateEnabled,
                    onPlayClicked = onAnimateClicked,
                )
            }
        }

        PhotosModeFab(
            mode = state.mode,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 16.dp,
                    bottom = if (state.mode == PhotoMode.NORMAL) {
                        24.dp
                    } else {
                        100.dp
                    },
                ),
            onEnterCompareModeClicked = onEnterCompareModeClicked,
            onEnterAnimateModeClicked = onEnterAnimateModeClicked,
            onExitModeClicked = onExitModeClicked,
        )

        PhotosScreenSnackbarHost(
            snackbarHostState = snackbarHostState,
            isSelectionBottomBarVisible = isSelectionBottomBarVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        )

        PhotoPreviewDialog(
            isVisible = previewPhoto != null,
            photoPreviewModel = previewPhoto,
            onDismiss = { previewPhoto = null },
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
            onEnterAnimateModeClicked = {},
            onExitModeClicked = {},
            onPhotoClicked = {},
            onCompareClicked = {},
            onAnimateClicked = {},
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
            onEnterAnimateModeClicked = {},
            onExitModeClicked = {},
            onPhotoClicked = {},
            onCompareClicked = {},
            onAnimateClicked = {},
        )
    }
}

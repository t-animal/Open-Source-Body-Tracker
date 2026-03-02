package de.t_animal.opensourcebodytracker.feature.photos
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import coil.compose.AsyncImage
import java.io.File

data class PhotosItemUiModel(
    val measurementId: Long,
    val dateEpochMillis: Long,
    val photoFile: File,
)

data class PhotosUiState(
    val items: List<PhotosItemUiModel> = emptyList(),
    val mode: PhotoMode = PhotoMode.NORMAL,
    val selectedMeasurementIds: List<Long> = emptyList(),
    val snackbarMessage: String? = null,
)

enum class PhotoMode {
    NORMAL,
    COMPARE,
}

data class PhotoSelectionResult(
    val selectedMeasurementIds: List<Long>,
    val selectionLimitReached: Boolean,
)

internal fun togglePhotoSelection(
    selectedMeasurementIds: List<Long>,
    clickedMeasurementId: Long,
): PhotoSelectionResult {
    return if (selectedMeasurementIds.contains(clickedMeasurementId)) {
        PhotoSelectionResult(
            selectedMeasurementIds = selectedMeasurementIds - clickedMeasurementId,
            selectionLimitReached = false,
        )
    } else if (selectedMeasurementIds.size >= 2) {
        PhotoSelectionResult(
            selectedMeasurementIds = selectedMeasurementIds,
            selectionLimitReached = true,
        )
    } else {
        PhotoSelectionResult(
            selectedMeasurementIds = selectedMeasurementIds + clickedMeasurementId,
            selectionLimitReached = false,
        )
    }
}

internal fun orderedCompareSelection(
    selectedMeasurementIds: List<Long>,
    items: List<PhotosItemUiModel>,
): Pair<Long, Long>? {
    if (selectedMeasurementIds.size != 2) {
        return null
    }
    val itemById = items.associateBy { it.measurementId }
    val selectedItems = selectedMeasurementIds.mapNotNull { itemById[it] }
    if (selectedItems.size != 2) {
        return null
    }
    val ordered = selectedItems.sortedWith(
        compareBy<PhotosItemUiModel> { it.dateEpochMillis }
            .thenBy { it.measurementId },
    )
    return ordered[0].measurementId to ordered[1].measurementId
}

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
        if (state.items.isEmpty()) {
            Text(
                text = "No photos yet",
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = listBottomPadding,
                ),
            ) {
                items(
                    items = state.items,
                    key = { it.measurementId },
                ) { item ->
                    val isSelected = selectedIds.contains(item.measurementId)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = state.mode == PhotoMode.COMPARE) {
                                    onPhotoClicked(item.measurementId)
                                },
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
                                        shape = RoundedCornerShape(6.dp),
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                            )
                            if (state.mode == PhotoMode.COMPARE && isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .size(28.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }
            }
        }

        if (state.mode == PhotoMode.COMPARE && selectedItems.isNotEmpty()) {
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        selectedItems.forEachIndexed { index, item ->
                            if (index > 0) {
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            }
                            AsyncImage(
                                model = item.photoFile,
                                contentDescription = "Selected thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    Button(
                        onClick = onCompareClicked,
                        enabled = compareEnabled,
                    ) {
                        Text("Compare")
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                if (state.mode == PhotoMode.NORMAL) {
                    onEnterCompareModeClicked()
                } else {
                    onExitModeClicked()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (state.mode == PhotoMode.COMPARE) 100.dp else 24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            if (state.mode == PhotoMode.NORMAL) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = "Enter compare mode",
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Exit compare mode",
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    bottom = if (state.mode == PhotoMode.COMPARE && selectedItems.isNotEmpty()) {
                        96.dp
                    } else {
                        24.dp
                    },
                ),
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

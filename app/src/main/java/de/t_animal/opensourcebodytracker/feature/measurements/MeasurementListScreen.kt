package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import de.t_animal.opensourcebodytracker.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.feature.measurements.components.DemoModeBanner
import de.t_animal.opensourcebodytracker.feature.measurements.components.LatestMeasurementCard
import de.t_animal.opensourcebodytracker.feature.measurements.components.MeasurementTable
import de.t_animal.opensourcebodytracker.feature.measurements.components.ResetAppConfirmationDialog
import de.t_animal.opensourcebodytracker.ui.components.SecondaryScreenScaffold
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme

private val MEASUREMENT_LIST_FAB_CLEARANCE = 96.dp

@Composable
fun MeasurementListRoute(
    onEdit: (Long) -> Unit,
    onAdd: () -> Unit,
    onOpenMore: () -> Unit,
    showDemoBanner: Boolean = false,
    onResetApp: () -> Unit,
    contentPadding: PaddingValues,
) {
    val vm: MeasurementListViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    when (val state = state) {
        is MeasurementListUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is MeasurementListUiState.Loaded -> MeasurementListScreen(
            state = state,
            onEdit = onEdit,
            onAdd = onAdd,
            onOpenMore = onOpenMore,
            showDemoBanner = showDemoBanner,
            onResetApp = onResetApp,
            contentPadding = contentPadding,
        )
    }
}

@Composable
fun MeasurementListFullRoute(
    onNavigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    val vm: MeasurementListViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()
    when (val state = state) {
        is MeasurementListUiState.Loading -> {
            SecondaryScreenScaffold(
                title = stringResource(R.string.common_loading),
                onNavigateBack = onNavigateBack,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        is MeasurementListUiState.Loaded -> MeasurementFullListScreen(
            state = state,
            onNavigateBack = onNavigateBack,
            onEdit = onEdit,
        )
    }
}

@Composable
fun MeasurementListScreen(
    state: MeasurementListUiState.Loaded,
    onEdit: (Long) -> Unit,
    onAdd: () -> Unit,
    onOpenMore: () -> Unit,
    showDemoBanner: Boolean = false,
    onResetApp: () -> Unit,
    contentPadding: PaddingValues,
) {
    var showResetConfirmationDialog by remember { mutableStateOf(false) }
    var selectedMeasurementIds by remember { mutableStateOf(emptySet<Long>()) }

    if (showResetConfirmationDialog) {
        ResetAppConfirmationDialog(
            onDismiss = { showResetConfirmationDialog = false },
            onConfirm = {
                showResetConfirmationDialog = false
                onResetApp()
            },
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(16.dp),
        ) {
            if (showDemoBanner) {
                item {
                    DemoModeBanner(
                        onResetApp = { showResetConfirmationDialog = true },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                LatestMeasurementCard(
                    state = state,
                    onAdd = onAdd,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = stringResource(R.string.measurement_list_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                MeasurementTable(
                    items = state.previewMeasurements,
                    visibleMetrics = state.visibleInTableMetrics,
                    selectedIds = selectedMeasurementIds,
                    unitSystem = state.unitSystem,
                    onRowSelect = { id ->
                        selectedMeasurementIds = if (id in selectedMeasurementIds) {
                            selectedMeasurementIds - id
                        } else {
                            selectedMeasurementIds + id
                        }
                    },
                )
            }

            if (state.hasMoreMeasurements) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(onClick = onOpenMore) {
                            Text(stringResource(R.string.common_more))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(MEASUREMENT_LIST_FAB_CLEARANCE))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(contentPadding)
                .padding(bottom = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End,
        ) {
            if (selectedMeasurementIds.size == 1) {
                FloatingActionButton(
                    onClick = { selectedMeasurementIds.singleOrNull()?.let { onEdit(it) } },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.cd_edit_measurement),
                    )
                }
            }

            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.AddBox,
                    contentDescription = stringResource(R.string.cd_add_measurement),
                )
            }
        }
    }
}

@Composable
fun MeasurementFullListScreen(
    state: MeasurementListUiState.Loaded,
    onNavigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
) {
    var selectedMeasurementIds by remember { mutableStateOf(emptySet<Long>()) }

    SecondaryScreenScaffold(
        title = stringResource(R.string.measurement_list_title),
        onNavigateBack = onNavigateBack,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                MeasurementTable(
                    items = state.allMeasurements,
                    visibleMetrics = state.visibleInTableMetrics,
                    selectedIds = selectedMeasurementIds,
                    unitSystem = state.unitSystem,
                    onRowSelect = { id ->
                        selectedMeasurementIds = if (id in selectedMeasurementIds) {
                            selectedMeasurementIds - id
                        } else {
                            selectedMeasurementIds + id
                        }
                    },
                )

                Spacer(modifier = Modifier.height(MEASUREMENT_LIST_FAB_CLEARANCE))
            }

            if (selectedMeasurementIds.size == 1) {
                FloatingActionButton(
                    onClick = { selectedMeasurementIds.singleOrNull()?.let { onEdit(it) } },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.cd_edit_measurement),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementListScreenPreview() {
    BodyTrackerTheme {
        MeasurementListScreen(
            state = MeasurementListUiState.Loaded(
                latestMeasurement = MeasurementListItemUiModel(
                    measurement = BodyMeasurement(
                        id = 1,
                        dateEpochMillis = 1_710_000_000_000,
                        weightKg = 80.0,
                        waistCircumferenceCm = 86.0,
                        hipCircumferenceCm = 95.0,
                    ),
                    derivedMetrics = DerivedMetrics(
                        bmi = 24.69,
                        navyBodyFatPercent = 18.3,
                        waistHipRatio = 0.91,
                        waistHeightRatio = 0.5,
                    ),
                ),
                previewMeasurements = listOf(
                    MeasurementListItemUiModel(
                        measurement = BodyMeasurement(
                            id = 1,
                            dateEpochMillis = 1_700_000_000_000,
                            weightKg = 80.0,
                        ),
                        derivedMetrics = DerivedMetrics(bmi = 24.69, waistHeightRatio = 0.50),
                    ),
                    MeasurementListItemUiModel(
                        measurement = BodyMeasurement(
                            id = 2,
                            dateEpochMillis = 1_710_000_000_000,
                            neckCircumferenceCm = 40.0,
                        ),
                        derivedMetrics = DerivedMetrics(),
                    ),
                ),
                allMeasurements = listOf(
                    MeasurementListItemUiModel(
                        measurement = BodyMeasurement(
                            id = 1,
                            dateEpochMillis = 1_700_000_000_000,
                            weightKg = 80.0,
                        ),
                        derivedMetrics = DerivedMetrics(bmi = 24.69, waistHeightRatio = 0.50),
                    ),
                ),
                hasMoreMeasurements = true,
                visibleInTableMetrics = BodyMetric.entries,
                unitSystem = UnitSystem.Metric,
                isEmpty = false,
            ),
            onEdit = {},
            onAdd = {},
            onOpenMore = {},
            onResetApp = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementListScreenEmptyPreview() {
    BodyTrackerTheme {
        MeasurementListScreen(
            state = MeasurementListUiState.Loaded(
                latestMeasurement = null,
                previewMeasurements = emptyList(),
                allMeasurements = emptyList(),
                hasMoreMeasurements = false,
                visibleInTableMetrics = emptyList(),
                unitSystem = UnitSystem.Metric,
                isEmpty = true,
            ),
            onEdit = {},
            onAdd = {},
            onOpenMore = {},
            onResetApp = {},
            contentPadding = PaddingValues(0.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MeasurementFullListScreenPreview() {
    BodyTrackerTheme {
        MeasurementFullListScreen(
            state = MeasurementListUiState.Loaded(
                latestMeasurement = null,
                previewMeasurements = emptyList(),
                allMeasurements = listOf(
                    MeasurementListItemUiModel(
                        measurement = BodyMeasurement(
                            id = 1,
                            dateEpochMillis = 1_700_000_000_000,
                            weightKg = 80.0,
                        ),
                        derivedMetrics = DerivedMetrics(
                            bmi = 24.69,
                            navyBodyFatPercent = 18.3,
                        ),
                    ),
                ),
                hasMoreMeasurements = false,
                visibleInTableMetrics = BodyMetric.entries,
                unitSystem = UnitSystem.Metric,
                isEmpty = false,
            ),
            onNavigateBack = {},
            onEdit = {},
        )
    }
}

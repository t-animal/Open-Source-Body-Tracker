package de.t_animal.opensourcebodytracker.feature.measurements

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.text.NumberFormat

@Composable
fun MeasurementListRoute(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
    onEdit: (Long) -> Unit,
    onAdd: () -> Unit,
    onOpenMore: () -> Unit,
    showDemoBanner: Boolean = false,
    onResetApp: () -> Unit,
    contentPadding: PaddingValues,
) {
    val vm: MeasurementListViewModel = viewModel(
        factory = MeasurementListViewModelFactory(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    MeasurementListScreen(
        state = state,
        onEdit = onEdit,
        onAdd = onAdd,
        onOpenMore = onOpenMore,
        showDemoBanner = showDemoBanner,
        onResetApp = onResetApp,
        contentPadding = contentPadding,
    )
}

@Composable
fun MeasurementListFullRoute(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
    onEdit: (Long) -> Unit,
) {
    val vm: MeasurementListViewModel = viewModel(
        factory = MeasurementListViewModelFactory(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    MeasurementFullListScreen(
        state = state,
        onEdit = onEdit,
    )
}

@Composable
fun MeasurementListScreen(
    state: MeasurementListUiState,
    onEdit: (Long) -> Unit,
    onAdd: () -> Unit,
    onOpenMore: () -> Unit,
    showDemoBanner: Boolean = false,
    onResetApp: () -> Unit,
    contentPadding: PaddingValues,
) {
    var showResetConfirmationDialog by remember { mutableStateOf(false) }

    if (showResetConfirmationDialog) {
        ResetAppConfirmationDialog(
            onDismiss = { showResetConfirmationDialog = false },
            onConfirm = {
                showResetConfirmationDialog = false
                onResetApp()
            },
        )
    }

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
                text = "All Measurements",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            MeasurementTable(
                items = state.previewMeasurements,
                visibleMetrics = state.visibleInTableMetrics,
                onRowClick = { onEdit(it) },
            )
        }

        if (state.hasMoreMeasurements) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                ) {
                    TextButton(onClick = onOpenMore) {
                        Text("More")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(MEASUREMENT_LIST_FAB_CLEARANCE))
        }
    }
}

@Composable
private fun DemoModeBanner(
    onResetApp: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "You are currently using demo data.",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Reset the app to create your own profile.",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onResetApp) {
                Text("Reset App")
            }
        }
    }
}

@Composable
private fun ResetAppConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset App?") },
        text = {
            Text(
                text = buildAnnotatedString {
                    append("This will delete all app data and close the app.")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("You will have to restart it manually.")
                    }
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun MeasurementFullListScreen(
    state: MeasurementListUiState,
    onEdit: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "All Measurements",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        MeasurementTable(
            items = state.allMeasurements,
            visibleMetrics = state.visibleInTableMetrics,
            onRowClick = onEdit,
        )
    }
}

@Composable
fun MeasurementListAddButton(
    onAdd: () -> Unit,
) {
    FloatingActionButton(onClick = onAdd) {
        Icon(
            imageVector = Icons.Filled.MonitorWeight,
            contentDescription = "Add measurement"
        )
    }
}

@Composable
private fun LatestMeasurementCard(
    state: MeasurementListUiState,
    onAdd: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Latest Measurement",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading -> {
                    Text("Loading…")
                }

                state.isEmpty -> {
                    Text(
                        text = "No measurements yet – create your first measurement",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onAdd) {
                        Text("Add")
                    }
                }

                else -> {
                    val latest = state.latestMeasurement
                    if (latest != null) {
                        LatestMeasurementGrid(
                            item = latest,
                            visibleMetrics = state.visibleInTableMetrics,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LatestMeasurementGrid(
    item: MeasurementListItemUiModel,
    visibleMetrics: List<BodyMetric>,
) {
    val metrics = remember(item, visibleMetrics) {
        buildLatestMeasurementMetrics(item, visibleMetrics)
    }

    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        metrics.forEach { metric ->
            Column(
                modifier = Modifier
                    .width(0.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = metric.value,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = metric.label,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun MeasurementTable(
    items: List<MeasurementListItemUiModel>,
    visibleMetrics: List<BodyMetric>,
    onRowClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 8.dp),
        ) {
            Text(
                text = "Date",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .width(TABLE_DATE_CELL_WIDTH)
                    .padding(horizontal = 8.dp),
            )
            visibleMetrics.forEach { column ->
                Text(
                    text = column.label(),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .width(column.tableCellWidth())
                        .padding(horizontal = 8.dp),
                )
            }
        }

        HorizontalDivider()

        if (items.isEmpty()) {
            Text(
                text = "--",
                modifier = Modifier.padding(16.dp),
            )
            return
        }

        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScroll)
                    .clickable { onRowClick(item.measurement.id) }
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    text = formatEpochMillisToLocalizedNumericDate(item.measurement.dateEpochMillis),
                    modifier = Modifier
                        .width(TABLE_DATE_CELL_WIDTH)
                        .padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                )
                visibleMetrics.forEach { column ->
                    Text(
                        text = column.formattedValue(item),
                        modifier = Modifier
                            .width(column.tableCellWidth())
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            HorizontalDivider()
        }
    }
}

private data class MetricDisplayItem(
    val label: String,
    val value: String,
)

private val TABLE_DATE_CELL_WIDTH = 90.dp

private fun BodyMetric.tableCellWidth(): Dp = when (unit) {
    BodyMetricUnit.Kilogram   -> 80.dp  // e.g. "300.00 kg"
    BodyMetricUnit.Centimeter -> 80.dp  // e.g. "200.00 cm"
    BodyMetricUnit.Millimeter -> 68.dp  // e.g. "50.00 mm"
    BodyMetricUnit.Percent    -> 70.dp  // e.g. "60.00 %"
    BodyMetricUnit.Unitless   -> 60.dp  // e.g. "60.00"
}
private val MEASUREMENT_LIST_FAB_CLEARANCE = 96.dp

private fun buildLatestMeasurementMetrics(
    item: MeasurementListItemUiModel,
    visibleMetrics: List<BodyMetric>,
): List<MetricDisplayItem> {
    return visibleMetrics.map { metric ->
        MetricDisplayItem(
            label = metric.label(),
            value = metric.formattedValue(item),
        )
    }
}

private fun BodyMetric.formattedValue(item: MeasurementListItemUiModel): String {
    val value = valueSelector(item.measurement, item.derivedMetrics)
    return valueWithUnit(value, unit)
}

private fun BodyMetric.label(): String = when (this) {
    is MeasuredBodyMetric -> when (this) {
        MeasuredBodyMetric.Weight -> "Weight"
        MeasuredBodyMetric.BodyFat -> "Body Fat"
        MeasuredBodyMetric.NeckCircumference -> "Neck"
        MeasuredBodyMetric.WaistCircumference -> "Waist"
        MeasuredBodyMetric.HipCircumference -> "Hip"
        MeasuredBodyMetric.ChestCircumference -> "Chest"
        MeasuredBodyMetric.AbdomenCircumference -> "Abdomen"
        MeasuredBodyMetric.ChestSkinfold -> "Chest Skinfold"
        MeasuredBodyMetric.AbdomenSkinfold -> "Abdomen Skinfold"
        MeasuredBodyMetric.ThighSkinfold -> "Thigh Skinfold"
        MeasuredBodyMetric.TricepsSkinfold -> "Triceps Skinfold"
        MeasuredBodyMetric.SuprailiacSkinfold -> "Suprailiac Skinfold"
    }

    is DerivedBodyMetric -> when (this) {
        DerivedBodyMetric.Bmi -> "BMI"
        DerivedBodyMetric.NavyBodyFatPercent -> "Body Fat Navy"
        DerivedBodyMetric.SkinfoldBodyFatPercent -> "Body Fat Skinfold"
        DerivedBodyMetric.WaistHipRatio -> "WHR"
        DerivedBodyMetric.WaistHeightRatio -> "WHtR"
    }

    else -> id
}

private fun valueWithUnit(value: Double?, unit: BodyMetricUnit): String {
    if (value == null) return MISSING_VALUE_PLACEHOLDER
    val number = formatDecimal(value)
    return if (unit == BodyMetricUnit.Unitless) number else "$number ${unit.symbol}"
}

private const val MISSING_VALUE_PLACEHOLDER = "--"

private fun formatDecimal(value: Double): String {
    val nf = NumberFormat.getNumberInstance()
    nf.isGroupingUsed = false
    nf.maximumFractionDigits = 2
    return nf.format(value)
}

@Preview(showBackground = true)
@Composable
private fun MeasurementListScreenPreview() {
    BodyTrackerTheme {
        MeasurementListScreen(
            state = MeasurementListUiState(
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
                isEmpty = false,
                isLoading = false,
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
            state = MeasurementListUiState(
                isLoading = false,
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
            state = MeasurementListUiState(
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
                isLoading = false,
                isEmpty = false,
            ),
            onEdit = {},
        )
    }
}

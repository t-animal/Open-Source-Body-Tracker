package de.t_animal.opensourcebodytracker.feature.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
import com.patrykandpatrick.vico.compose.cartesian.axis.Axis
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarkerController
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.Interaction
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.DerivedBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

@Composable
fun AnalysisRoute(
    measurementRepository: MeasurementRepository,
    profileRepository: ProfileRepository,
    settingsRepository: SettingsRepository,
    calculateMeasurementDerivedMetrics: CalculateMeasurementDerivedMetricsUseCase,
    contentPadding: PaddingValues,
) {
    val vm: AnalysisViewModel = viewModel(
        factory = AnalysisViewModelFactory(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            settingsRepository = settingsRepository,
            calculateMeasurementDerivedMetrics = calculateMeasurementDerivedMetrics,
        ),
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    AnalysisScreen(
        state = state,
        onDurationSelected = vm::onDurationSelected,
        contentPadding = contentPadding,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    state: AnalysisUiState,
    onDurationSelected: (AnalysisDuration) -> Unit,
    contentPadding: PaddingValues,
) {
    var selectedEpochMillis by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
    ) {
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AnalysisDuration.entries.forEachIndexed { index, duration ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = AnalysisDuration.entries.size,
                            ),
                            onClick = { onDurationSelected(duration) },
                            selected = duration == state.selectedDuration,
                            label = { Text(duration.label) },
                        )
                    }
                }
            }
        }

        item {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
        }

        items(state.metricCharts, key = { it.definition.id }) { chart ->
            MetricChartCard(
                chart = chart,
                duration = state.selectedDuration,
                selectedEpochMillis = selectedEpochMillis,
                onSelectedEpochMillisChange = { selectedEpochMillis = it },
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MetricChartCard(
    chart: AnalysisMetricChartUiModel,
    duration: AnalysisDuration,
    selectedEpochMillis: Long?,
    onSelectedEpochMillisChange: (Long?) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = chart.definition.analysisTitle(),
                style = MaterialTheme.typography.titleMedium,
            )

            if (chart.points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CHART_HEIGHT),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "no data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                MetricLineChart(
                    chart = chart,
                    duration = duration,
                    selectedEpochMillis = selectedEpochMillis,
                    onSelectedEpochMillisChange = onSelectedEpochMillisChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CHART_HEIGHT),
                )
            }
        }
    }
}

@Composable
private fun MetricLineChart(
    chart: AnalysisMetricChartUiModel,
    duration: AnalysisDuration,
    selectedEpochMillis: Long?,
    onSelectedEpochMillisChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val points = chart.points
    val yAxisRange = chart.yAxisRange ?: return
    val modelProducer = remember(chart.definition.id) { CartesianChartModelProducer() }
    val xValues = remember(points) { points.map { it.epochMillis } }
    val yValues = remember(points) { points.map { it.value } }

    val unitSuffix = remember(chart.definition.unit) {
        chart.definition.unit.suffixWithLeadingSpace()
    }
    val xAxisValueFormatter = remember(duration) {
        CartesianValueFormatter { _, value, _ ->
            formatXAxisLabel(value.toLong(), duration)
        }
    }
    val yAxisValueFormatter = remember(unitSuffix) {
        CartesianValueFormatter.decimal(decimalCount = 2, suffix = unitSuffix)
    }
    val markerValueFormatter = remember(unitSuffix) {
        DefaultCartesianMarker.ValueFormatter.default(suffix = unitSuffix)
    }
    val markerLabel = rememberTextComponent(
        style = TextStyle(color = MaterialTheme.colorScheme.onSurface),
    )
    val pointColor = MaterialTheme.colorScheme.primary
    val selectedPointIndicator = rememberShapeComponent(
        fill = Fill(Color.White),
        shape = CircleShape,
        strokeFill = Fill(pointColor),
        strokeThickness = CHART_SELECTED_POINT_BORDER_WIDTH,
    )
    val marker = rememberDefaultCartesianMarker(
        label = markerLabel,
        valueFormatter = markerValueFormatter,
        indicator = { selectedPointIndicator },
        indicatorSize = CHART_SELECTED_POINT_SIZE,
    )
    val markerController = rememberTapSelectionMarkerController(
        onSelectedEpochMillisChange = onSelectedEpochMillisChange,
    )
    val selectedPersistentMarkerX = remember(selectedEpochMillis, xValues) {
        selectedEpochMillis
            ?.let { selected ->
                xValues.firstOrNull { it == selected }
                    ?: xValues.firstOrNull { candidate ->
                        candidate.toLocalDateInSystemZone() == selected.toLocalDateInSystemZone()
                    }
            }
    }
    val rangeProvider = remember(yAxisRange) {
        fixedYRangeProvider(yAxisRange)
    }
    val scrollState = rememberVicoScrollState(scrollEnabled = true)
    val zoomState = rememberVicoZoomState(
        zoomEnabled = true,
        initialZoom = Zoom.Content,
    )
    val lineLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
                fill = LineCartesianLayer.LineFill.single(Fill(pointColor)),
                pointProvider = LineCartesianLayer.PointProvider.single(
                    LineCartesianLayer.Point(
                        component = rememberShapeComponent(Fill(pointColor), CircleShape),
                        size = CHART_POINT_SIZE,
                    ),
                ),
            ),
        ),
        rangeProvider = rangeProvider,
    )

    val cartesianChart = rememberCartesianChart(
        lineLayer,
        startAxis = VerticalAxis.rememberStart(valueFormatter = yAxisValueFormatter),
        bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xAxisValueFormatter),
        marker = marker,
        persistentMarkers = { _ ->
            selectedPersistentMarkerX?.let { marker at it }
        },
        markerController = markerController,
    )

    LaunchedEffect(xValues, yValues) {
        modelProducer.runTransaction {
            lineSeries {
                series(xValues, yValues)
            }
        }
    }

    CartesianChartHost(
        chart = cartesianChart,
        modelProducer = modelProducer,
        modifier = modifier,
        scrollState = scrollState,
        zoomState = zoomState,
        animateIn = false
    )
}

private fun fixedYRangeProvider(yAxisRange: AnalysisYAxisRange): CartesianLayerRangeProvider =
    object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = yAxisRange.min

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = yAxisRange.max
    }

@Composable
private fun rememberTapSelectionMarkerController(
    onSelectedEpochMillisChange: (Long?) -> Unit,
): CartesianMarkerController {
    val latestOnSelectedEpochMillisChange = rememberUpdatedState(onSelectedEpochMillisChange)
    return remember {
        object : CartesianMarkerController {
            override fun shouldAcceptInteraction(
                interaction: Interaction,
                targets: List<CartesianMarker.Target>,
            ): Boolean = interaction is Interaction.Tap

            override fun shouldShowMarker(
                interaction: Interaction,
                targets: List<CartesianMarker.Target>,
            ): Boolean {
                latestOnSelectedEpochMillisChange.value(targets.firstOrNull()?.x?.roundToLong())
                return false
            }
        }
    }
}

private fun formatXAxisLabel(
    epochMillis: Long,
    duration: AnalysisDuration,
): String {
    val date = epochMillis.toLocalDateInSystemZone()
    val pattern = when (duration) {
        AnalysisDuration.OneMonth -> "dd.MM"
        AnalysisDuration.OneYear -> "MM.yy"
        AnalysisDuration.All -> "MM.yy"
        AnalysisDuration.ThreeMonths,
        AnalysisDuration.SixMonths,
        -> "dd.MM"
    }
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return formatter.format(date)
}

private val CHART_HEIGHT = 220.dp
private val CHART_POINT_SIZE = 6.dp
private val CHART_SELECTED_POINT_SIZE = 12.dp
private val CHART_SELECTED_POINT_BORDER_WIDTH = 2.dp

private fun Long.toLocalDateInSystemZone() =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun BodyMetricUnit.suffixWithLeadingSpace(): String = when (this) {
    BodyMetricUnit.Unitless -> ""
    else -> " $symbol"
}

@Preview(showBackground = true)
@Composable
private fun AnalysisScreenPreview() {
    BodyTrackerTheme {
        AnalysisScreen(
            state = AnalysisUiState(
                selectedDuration = AnalysisDuration.ThreeMonths,
                metricCharts = listOf(
                    AnalysisMetricChartUiModel(
                        definition = BodyMetric.entries.first(),
                        points = listOf(
                            AnalysisChartPoint(1_735_689_600_000, 82.1),
                            AnalysisChartPoint(1_736_467_200_000, 81.8),
                            AnalysisChartPoint(1_737_158_400_000, 81.2),
                            AnalysisChartPoint(1_737_936_000_000, 80.9),
                        ),
                        yAxisRange = AnalysisYAxisRange(80.7, 82.3),
                    ),
                    AnalysisMetricChartUiModel(
                        definition = BodyMetric.entries[14],
                        points = emptyList(),
                        yAxisRange = null,
                    ),
                ),
                isLoading = false,
            ),
            onDurationSelected = {},
            contentPadding = PaddingValues(),
        )
    }
}

private fun BodyMetric.analysisTitle(): String = when (this) {
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
        DerivedBodyMetric.NavyBodyFatPercent -> "Navy Body Fat %"
        DerivedBodyMetric.SkinfoldBodyFatPercent -> "Skinfold Body Fat %"
        DerivedBodyMetric.WaistHipRatio -> "Waist–Hip Ratio"
        DerivedBodyMetric.WaistHeightRatio -> "Waist–Height Ratio"
    }

    else -> id
}

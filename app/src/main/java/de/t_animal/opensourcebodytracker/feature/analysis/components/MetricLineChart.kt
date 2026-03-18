package de.t_animal.opensourcebodytracker.feature.analysis.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.Zoom
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
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisMetricChartUiModel
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisYAxisRange
import de.t_animal.opensourcebodytracker.feature.analysis.helpers.suffixWithLeadingSpace
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.coroutines.launch

private val CHART_POINT_SIZE = 6.dp
private val CHART_SELECTED_POINT_SIZE = 12.dp
private val CHART_SELECTED_POINT_BORDER_WIDTH = 2.dp

@Composable
internal fun MetricLineChart(
    chart: AnalysisMetricChartUiModel,
    duration: AnalysisDuration,
    selectedDate: LocalDate?,
    onSelectedDateChange: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val points = chart.points
    val yAxisRange = chart.yAxisRange ?: return
    val modelProducer = remember(chart.definition.id) { CartesianChartModelProducer() }
    // Use epoch days as x-values: vico's AlignedHorizontalAxisItemPlacer iterates every integer
    // between min and max x, so epoch milliseconds would produce trillions of allocations.
    val xValues = remember(points) { points.map { it.epochMillis.toLocalDateInSystemZone().toEpochDay() } }
    val yValues = remember(points) { points.map { it.value } }

    // Note marker data
    val notesByEpochDay = remember(points) {
        points.filter { !it.note.isNullOrBlank() }
            .associate { it.epochMillis.toLocalDateInSystemZone().toEpochDay() to it.note!! }
    }
    val noteEpochDays = remember(notesByEpochDay) { notesByEpochDay.keys }
    var noteCanvasPositions by remember { mutableStateOf(emptyMap<Long, Float>()) }
    var layerTopPx by remember { mutableFloatStateOf(0f) }

    val unitSuffix = remember(chart.definition.unit) {
        chart.definition.unit.suffixWithLeadingSpace()
    }
    val xAxisValueFormatter = remember(duration) {
        CartesianValueFormatter { _, value, _ ->
            formatXAxisLabel(LocalDate.ofEpochDay(value.toLong()), duration)
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
        onSelectedXValueChange = { epochDay ->
            onSelectedDateChange(epochDay?.let { LocalDate.ofEpochDay(it) })
        },
    )
    val selectedPersistentMarkerX = remember(selectedDate, xValues) {
        selectedDate?.let { xValues.firstOrNull { it == selectedDate.toEpochDay() } }
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

    val noteMarkerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val noteDecoration = remember(noteEpochDays, noteMarkerColor) {
        NoteMarkerDecoration(
            noteEpochDays = noteEpochDays,
            color = noteMarkerColor,
            onPositionsUpdated = { positions, top ->
                noteCanvasPositions = positions
                layerTopPx = top
            },
        )
    }

    val cartesianChart = rememberCartesianChart(
        lineLayer,
        startAxis = VerticalAxis.rememberStart(valueFormatter = yAxisValueFormatter),
        bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = xAxisValueFormatter),
        marker = marker,
        persistentMarkers = { _ ->
            selectedPersistentMarkerX?.let { marker at it }
        },
        decorations = listOf(noteDecoration),
        markerController = markerController,
    )

    LaunchedEffect(xValues, yValues) {
        modelProducer.runTransaction {
            lineSeries {
                series(xValues, yValues)
            }
        }
    }

    Box(modifier = modifier) {
        CartesianChartHost(
            chart = cartesianChart,
            modelProducer = modelProducer,
            modifier = Modifier.matchParentSize(),
            scrollState = scrollState,
            zoomState = zoomState,
            animateIn = false,
        )

        if (notesByEpochDay.isNotEmpty()) {
            NoteMarkerTooltipOverlay(
                noteCanvasPositions = noteCanvasPositions,
                notesByEpochDay = notesByEpochDay,
                layerTopPx = layerTopPx,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

private fun fixedYRangeProvider(yAxisRange: AnalysisYAxisRange): CartesianLayerRangeProvider =
    object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = yAxisRange.min

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = yAxisRange.max
    }

@Composable
private fun rememberTapSelectionMarkerController(
    onSelectedXValueChange: (Long?) -> Unit,
): CartesianMarkerController {
    val latestOnSelectedXValueChange = rememberUpdatedState(onSelectedXValueChange)
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
                latestOnSelectedXValueChange.value(targets.firstOrNull()?.x?.roundToLong())
                return false
            }
        }
    }
}

private fun formatXAxisLabel(
    date: LocalDate,
    duration: AnalysisDuration,
): String {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteMarkerTooltipOverlay(
    noteCanvasPositions: Map<Long, Float>,
    notesByEpochDay: Map<Long, String>,
    layerTopPx: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val triangleTapZonePx = with(density) { 24.dp.toPx() }
    val hitTolerancePx = with(density) { 24.dp.toPx() }

    var activeNoteEpochDay by remember { mutableStateOf<Long?>(null) }
    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .pointerInput(noteCanvasPositions, notesByEpochDay) {
                detectTapGestures { offset ->
                    // Check if tap is in the triangle zone (near the top of the chart layer)
                    if (offset.y in (layerTopPx - triangleTapZonePx / 2)..(layerTopPx + triangleTapZonePx)) {
                        val hitEpochDay = noteCanvasPositions.entries
                            .minByOrNull { abs(it.value - offset.x) }
                            ?.takeIf { abs(it.value - offset.x) < hitTolerancePx }
                            ?.key

                        if (hitEpochDay != null && notesByEpochDay.containsKey(hitEpochDay)) {
                            activeNoteEpochDay = hitEpochDay
                            scope.launch { tooltipState.show() }
                            return@detectTapGestures
                        }
                    }
                    // Tap was not on a triangle — dismiss any open tooltip
                    if (tooltipState.isVisible) {
                        scope.launch { tooltipState.dismiss() }
                        activeNoteEpochDay = null
                    }
                }
            },
    ) {
        val activeEpochDay = activeNoteEpochDay
        val activeNoteText = activeEpochDay?.let { notesByEpochDay[it] }
        val activeCanvasX = activeEpochDay?.let { noteCanvasPositions[it] }

        if (activeNoteText != null && activeCanvasX != null) {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    TooltipAnchorPosition.Above,
                ),
                tooltip = {
                    PlainTooltip {
                        Text(
                            text = activeNoteText,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                state = tooltipState,
                enableUserInput = false,
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                // Invisible anchor positioned at the active triangle
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = activeCanvasX.roundToInt(),
                                y = layerTopPx.roundToInt(),
                            )
                        }
                        .size(1.dp),
                )
            }
        }
    }
}

private fun Long.toLocalDateInSystemZone() =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

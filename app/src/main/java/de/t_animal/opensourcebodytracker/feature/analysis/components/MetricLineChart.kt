package de.t_animal.opensourcebodytracker.feature.analysis.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
import com.patrykandpatrick.vico.compose.common.DrawingContext
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.Component
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisChartPoint
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisMetricChartUiModel
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisYAxisRange
import de.t_animal.opensourcebodytracker.feature.analysis.helpers.suffixWithLeadingSpace
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToLong

private val CHART_POINT_SIZE = 6.dp
private val NOTE_TAP_TARGET_POINT_SIZE = 40.dp
private val CHART_SELECTED_POINT_SIZE = 12.dp
private val CHART_SELECTED_POINT_BORDER_WIDTH = 2.dp

private data class MetricLineChartRenderData(
    val xValues: List<Long>,
    val yValues: List<Double>,
    val pointVisualStateByEpochDay: Map<Long, PointVisualState>,
    val noteTextByEpochDay: Map<Long, String>,
    val noteTriangleXValues: List<Long>,
)

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
    val renderData = remember(points) {
        buildMetricLineChartRenderData(points)
    }

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
    val markerValueFormatter = rememberNoteAwareMarkerValueFormatter(
        unitSuffix = unitSuffix,
        noteTextByEpochDay = renderData.noteTextByEpochDay,
    )
    val markerLabel = rememberTextComponent(
        style = TextStyle(color = MaterialTheme.colorScheme.onSurface),
    )
    val pointColor = MaterialTheme.colorScheme.primary
    val pointProvider = rememberMetricPointProvider(
        pointColor = pointColor,
        pointVisualStateByEpochDay = renderData.pointVisualStateByEpochDay,
    )
    val noteTriangleMarkersByX = rememberNoteTriangleMarkersByX(
        xValues = renderData.noteTriangleXValues,
        color = pointColor,
    )
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
        shouldShowMarkerForXValue = { epochDay ->
            epochDay != null && renderData.noteTextByEpochDay.containsKey(epochDay)
        },
    )
    val selectedPersistentMarkerX = remember(selectedDate, renderData.xValues) {
        selectedDate?.let { renderData.xValues.firstOrNull { it == selectedDate.toEpochDay() } }
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
                pointProvider = pointProvider,
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
            renderData.noteTriangleXValues.forEach { epochDay ->
                noteTriangleMarkersByX[epochDay]?.let { noteMarker ->
                    noteMarker at epochDay
                }
            }
        },
        markerController = markerController,
    )

    LaunchedEffect(renderData.xValues, renderData.yValues) {
        modelProducer.runTransaction {
            lineSeries {
                series(renderData.xValues, renderData.yValues)
            }
        }
    }

    CartesianChartHost(
        chart = cartesianChart,
        modelProducer = modelProducer,
        modifier = modifier,
        scrollState = scrollState,
        zoomState = zoomState,
        animateIn = false,
    )
}

private fun fixedYRangeProvider(yAxisRange: AnalysisYAxisRange): CartesianLayerRangeProvider =
    object : CartesianLayerRangeProvider {
        override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = yAxisRange.min

        override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double = yAxisRange.max
    }

@Composable
private fun rememberMetricPointProvider(
    pointColor: Color,
    pointVisualStateByEpochDay: Map<Long, PointVisualState>,
): LineCartesianLayer.PointProvider {
    val markerComponent = rememberShapeComponent(Fill(pointColor), CircleShape)
    val noteMarkerTapTargetComponent = remember(markerComponent) {
        CenteredFixedSizeComponent(
            delegate = markerComponent,
            fixedSize = CHART_POINT_SIZE,
        )
    }
    val noteOnlyTapTargetComponent = rememberShapeComponent(Fill(Color.Transparent), CircleShape)

    val markerOnlyPoint = LineCartesianLayer.Point(
        component = markerComponent,
        size = CHART_POINT_SIZE,
    )
    val noteMarkerTapTargetPoint = LineCartesianLayer.Point(
        component = noteMarkerTapTargetComponent,
        size = NOTE_TAP_TARGET_POINT_SIZE,
    )
    val noteOnlyTapTargetPoint = LineCartesianLayer.Point(
        component = noteOnlyTapTargetComponent,
        size = NOTE_TAP_TARGET_POINT_SIZE,
    )

    return remember(
        markerOnlyPoint,
        noteMarkerTapTargetPoint,
        noteOnlyTapTargetPoint,
        pointVisualStateByEpochDay,
    ) {
        NoteAwarePointProvider(
            markerOnlyPoint = markerOnlyPoint,
            noteMarkerTapTargetPoint = noteMarkerTapTargetPoint,
            noteOnlyTapTargetPoint = noteOnlyTapTargetPoint,
            pointVisualStateByEpochDay = pointVisualStateByEpochDay,
        )
    }
}

@Composable
private fun rememberNoteAwareMarkerValueFormatter(
    unitSuffix: String,
    noteTextByEpochDay: Map<Long, String>,
): DefaultCartesianMarker.ValueFormatter {
    val defaultMarkerValueFormatter = remember(unitSuffix) {
        DefaultCartesianMarker.ValueFormatter.default(suffix = unitSuffix)
    }
    return remember(defaultMarkerValueFormatter, noteTextByEpochDay) {
        DefaultCartesianMarker.ValueFormatter { context, targets ->
            val selectedEpochDay = targets.firstOrNull()?.x?.roundToLong()
            val noteText = selectedEpochDay?.let(noteTextByEpochDay::get)
            val defaultLabel = defaultMarkerValueFormatter.format(context, targets)

            if (noteText == null) {
                defaultLabel
            } else {
                buildString {
                    append(defaultLabel)
                    if (defaultLabel.isNotEmpty()) {
                        append('\n')
                    }
                    append(noteText)
                }
            }
        }
    }
}

@Composable
private fun rememberTapSelectionMarkerController(
    onSelectedXValueChange: (Long?) -> Unit,
    shouldShowMarkerForXValue: (Long?) -> Boolean,
): CartesianMarkerController {
    val latestOnSelectedXValueChange = rememberUpdatedState(onSelectedXValueChange)
    val latestShouldShowMarkerForXValue = rememberUpdatedState(shouldShowMarkerForXValue)
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
                val selectedXValue = targets.firstOrNull()?.x?.roundToLong()
                latestOnSelectedXValueChange.value(selectedXValue)
                return latestShouldShowMarkerForXValue.value(selectedXValue)
            }
        }
    }
}

private data class NotePointMetadata(
    val hasNote: Boolean,
    val hasPhoto: Boolean,
    val normalizedNoteText: String?,
)

private class NoteAwarePointProvider(
    private val markerOnlyPoint: LineCartesianLayer.Point,
    private val noteMarkerTapTargetPoint: LineCartesianLayer.Point,
    private val noteOnlyTapTargetPoint: LineCartesianLayer.Point,
    private val pointVisualStateByEpochDay: Map<Long, PointVisualState>,
) : LineCartesianLayer.PointProvider {
    override fun getPoint(
        entry: com.patrykandpatrick.vico.compose.cartesian.data.LineCartesianLayerModel.Entry,
        seriesIndex: Int,
        extraStore: ExtraStore,
    ): LineCartesianLayer.Point? {
        val visualState = pointVisualStateByEpochDay[entry.x.roundToLong()] ?: PointVisualState.MarkerOnly
        return when (visualState) {
            PointVisualState.MarkerAndTriangle -> noteMarkerTapTargetPoint
            PointVisualState.TriangleOnly -> noteOnlyTapTargetPoint
            PointVisualState.MarkerOnly -> markerOnlyPoint
            PointVisualState.None -> null
        }
    }

    override fun getLargestPoint(extraStore: ExtraStore): LineCartesianLayer.Point? {
        val hasNoteTapTargets = pointVisualStateByEpochDay.values.any {
            it == PointVisualState.MarkerAndTriangle || it == PointVisualState.TriangleOnly
        }
        val hasMarkerOnlyPoints = pointVisualStateByEpochDay.values.any { it == PointVisualState.MarkerOnly }
        return when {
            hasNoteTapTargets -> noteMarkerTapTargetPoint
            hasMarkerOnlyPoints -> markerOnlyPoint
            else -> null
        }
    }
}

private class CenteredFixedSizeComponent(
    private val delegate: Component,
    private val fixedSize: Dp,
) : Component {
    override fun draw(
        context: DrawingContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) {
        val sizePx = fixedSize.value * context.density.density
        val halfSizePx = sizePx / 2f
        val centerX = (left + right) / 2f
        val centerY = (top + bottom) / 2f
        delegate.draw(
            context = context,
            left = centerX - halfSizePx,
            top = centerY - halfSizePx,
            right = centerX + halfSizePx,
            bottom = centerY + halfSizePx,
        )
    }
}

private enum class PointVisualState(
    val showsMarker: Boolean,
    val showsTriangle: Boolean,
) {
    MarkerAndTriangle(showsMarker = true, showsTriangle = true),
    TriangleOnly(showsMarker = false, showsTriangle = true),
    None(showsMarker = false, showsTriangle = false),
    MarkerOnly(showsMarker = true, showsTriangle = false),
}

private fun AnalysisChartPoint.toNotePointMetadata(): NotePointMetadata =
    NotePointMetadata(
        hasNote = hasNote,
        hasPhoto = hasPhoto,
        normalizedNoteText = normalizedNoteText,
    )

private fun buildMetricLineChartRenderData(points: List<AnalysisChartPoint>): MetricLineChartRenderData {
    // Use epoch days as x-values: vico's AlignedHorizontalAxisItemPlacer iterates every integer
    // between min and max x, so epoch milliseconds would produce trillions of allocations.
    val xValues = ArrayList<Long>(points.size)
    val yValues = ArrayList<Double>(points.size)
    val pointVisualStateByEpochDay = LinkedHashMap<Long, PointVisualState>(points.size)
    val noteTextByEpochDay = LinkedHashMap<Long, String>()

    points.forEach { point ->
        val epochDay = point.epochMillis.toLocalDateInSystemZone().toEpochDay()
        val metadata = point.toNotePointMetadata()

        xValues += epochDay
        yValues += point.value
        pointVisualStateByEpochDay[epochDay] = classifyPointVisualState(metadata)
        metadata.normalizedNoteText?.let { noteTextByEpochDay[epochDay] = it }
    }

    val noteTriangleXValues = xValues
        .filter { epochDay -> pointVisualStateByEpochDay[epochDay]?.showsTriangle == true }
        .distinct()

    return MetricLineChartRenderData(
        xValues = xValues,
        yValues = yValues,
        pointVisualStateByEpochDay = pointVisualStateByEpochDay,
        noteTextByEpochDay = noteTextByEpochDay,
        noteTriangleXValues = noteTriangleXValues,
    )
}

private fun classifyPointVisualState(metadata: NotePointMetadata): PointVisualState = when {
    metadata.hasNote && metadata.hasPhoto -> PointVisualState.MarkerAndTriangle
    metadata.hasNote -> PointVisualState.TriangleOnly
    metadata.hasPhoto -> PointVisualState.None
    else -> PointVisualState.MarkerOnly
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

private fun Long.toLocalDateInSystemZone() =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

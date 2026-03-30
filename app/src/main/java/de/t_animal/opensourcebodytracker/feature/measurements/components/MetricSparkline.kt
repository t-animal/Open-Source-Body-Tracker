package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.math.abs

private val SPARKLINE_HEIGHT = 30.dp
private const val SPARKLINE_AREA_ALPHA = 0.15f
private const val SPARKLINE_VERTICAL_PADDING_FRACTION = 0.1
private const val SPARKLINE_MIN_VERTICAL_PADDING = 0.5
private val SPARKLINE_POINT_SPACING = 8.dp
private val SPARKLINE_STROKE_THICKNESS = 1.dp


@Composable
internal fun MetricSparkline(
    points: List<Pair<Long, Double>>,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) return

    val xValues = remember(points) { points.indices.map(Int::toDouble) }
    val yValues = remember(points) { points.map { it.second } }
    val modelProducer = remember { CartesianChartModelProducer() }

    val lineColor = MaterialTheme.colorScheme.primary
    val areaColor = MaterialTheme.colorScheme.onSurface.copy(alpha = SPARKLINE_AREA_ALPHA)

    val rangeProvider = remember {
        object : CartesianLayerRangeProvider {
            override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
                val padding = computeVerticalPadding(minY, maxY)
                return minY - padding
            }

            override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore): Double {
                val padding = computeVerticalPadding(minY, maxY)
                return maxY
            }
        }
    }
    val lineLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
                fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
                stroke = LineCartesianLayer.LineStroke.Continuous(SPARKLINE_STROKE_THICKNESS),
                areaFill = LineCartesianLayer.AreaFill.single(Fill(areaColor)),
            ),
        ),
        pointSpacing = SPARKLINE_POINT_SPACING,
        rangeProvider = rangeProvider,
    )

    val chart = rememberCartesianChart(
        lineLayer,
    )

    LaunchedEffect(xValues, yValues) {
        modelProducer.runTransaction {
            lineSeries { series(xValues, yValues) }
        }
    }

    CartesianChartHost(
        chart = chart,
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(SPARKLINE_HEIGHT),
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        zoomState = rememberVicoZoomState(zoomEnabled = false),
        animateIn = false,
    )
}

private fun computeVerticalPadding(minY: Double, maxY: Double): Double {
    val range = abs(maxY - minY)
    return if (range == 0.0) {
        SPARKLINE_MIN_VERTICAL_PADDING
    } else {
        maxOf(range * SPARKLINE_VERTICAL_PADDING_FRACTION, SPARKLINE_MIN_VERTICAL_PADDING)
    }
}

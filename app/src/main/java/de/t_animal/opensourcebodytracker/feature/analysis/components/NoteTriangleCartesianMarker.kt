package de.t_animal.opensourcebodytracker.feature.analysis.components

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerDimensions
import com.patrykandpatrick.vico.compose.cartesian.layer.CartesianLayerMargins
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.Component
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent

private val NOTE_TRIANGLE_SIZE = 30.dp
private val NOTE_TRIANGLE_VERTICAL_GAP = 55.dp

@Composable
internal fun rememberNoteTriangleMarkersByX(
    xValues: List<Long>,
    color: Color,
    triangleSize: Dp = NOTE_TRIANGLE_SIZE,
    verticalGap: Dp = NOTE_TRIANGLE_VERTICAL_GAP,
): Map<Long, CartesianMarker> {
    val triangleShape = remember {
        GenericShape { size, _ ->
            moveTo(size.width / 2f, size.height)
            lineTo(0f, 0f)
            lineTo(size.width, 0f)
            close()
        }
    }
    val triangleComponent = rememberShapeComponent(
        fill = Fill(color),
        shape = triangleShape,
    )
    return remember(xValues, triangleComponent, triangleSize, verticalGap) {
        xValues.associateWith {
            NoteTriangleCartesianMarker(
                triangleComponent = triangleComponent,
                triangleSize = triangleSize,
                verticalGap = verticalGap,
            )
        }
    }
}

private class NoteTriangleCartesianMarker(
    private val triangleComponent: Component,
    private val triangleSize: Dp,
    private val verticalGap: Dp,
) : CartesianMarker {

    override fun updateLayerMargins(
        context: CartesianMeasuringContext,
        layerMargins: CartesianLayerMargins,
        layerDimensions: CartesianLayerDimensions,
        model: CartesianChartModel,
    ) {
        val requiredTopMargin =
            triangleSize.value * context.density.density +
                verticalGap.value * context.density.density
        layerMargins.ensureValuesAtLeast(top = requiredTopMargin)
    }

    override fun drawOverLayers(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>,
    ) {
        val triangleHeightPx = triangleSize.value * context.density.density
        val halfTriangleWidthPx = triangleHeightPx / 2f
        val verticalGapPx = verticalGap.value * context.density.density

        targets.forEach { target ->
            val lineTarget = target as? LineCartesianLayerMarkerTarget ?: return@forEach
            val point = lineTarget.points.firstOrNull() ?: return@forEach

            val bottom = point.canvasY - verticalGapPx
            triangleComponent.draw(
                context = context,
                left = target.canvasX - halfTriangleWidthPx,
                top = bottom - triangleHeightPx,
                right = target.canvasX + halfTriangleWidthPx,
                bottom = bottom,
            )
        }
    }
}

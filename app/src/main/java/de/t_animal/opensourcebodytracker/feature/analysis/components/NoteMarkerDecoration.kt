package de.t_animal.opensourcebodytracker.feature.analysis.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration

internal class NoteMarkerDecoration(
    private val noteEpochDays: Set<Long>,
    private val color: Color,
    private val onPositionsUpdated: (positions: Map<Long, Float>, layerTop: Float) -> Unit,
) : Decoration {

    override fun drawOverLayers(context: CartesianDrawingContext) {
        if (noteEpochDays.isEmpty()) return

        with(context) {
            val xStep = ranges.xStep
            if (xStep == 0.0) return

            val lineStrokeWidthPx = 1.dp.pixels
            val dashIntervalPx = 3.dp.pixels
            val triangleSizePx = 4.dp.pixels

            val drawingStart = layerBounds.left +
                layerDimensions.startPadding * layoutDirectionMultiplier

            val positions = mutableMapOf<Long, Float>()

            for (epochDay in noteEpochDays) {
                val canvasX = drawingStart +
                    ((epochDay.toDouble() - ranges.minX) / xStep).toFloat() *
                    layerDimensions.xSpacing * layoutDirectionMultiplier -
                    scroll

                if (canvasX < layerBounds.left || canvasX > layerBounds.right) continue

                positions[epochDay] = canvasX

                val lineTop = layerBounds.top + triangleSizePx
                val lineBottom = layerBounds.bottom

                // Draw vertical dotted line
                mutableDrawScope.drawLine(
                    color = color,
                    start = Offset(canvasX, lineTop),
                    end = Offset(canvasX, lineBottom),
                    strokeWidth = lineStrokeWidthPx,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashIntervalPx, dashIntervalPx),
                        0f,
                    ),
                )

                // Draw downward-pointing triangle at top
                val trianglePath = Path().apply {
                    moveTo(canvasX - triangleSizePx, layerBounds.top)
                    lineTo(canvasX + triangleSizePx, layerBounds.top)
                    lineTo(canvasX, layerBounds.top + triangleSizePx)
                    close()
                }
                // Filled triangle with rounded stroke for rounded corners
                mutableDrawScope.drawPath(
                    path = trianglePath,
                    color = color,
                )
                mutableDrawScope.drawPath(
                    path = trianglePath,
                    color = color,
                    style = Stroke(
                        width = lineStrokeWidthPx,
                        join = StrokeJoin.Round,
                    ),
                )
            }

            onPositionsUpdated(positions, layerBounds.top)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NoteMarkerDecoration) return false
        return noteEpochDays == other.noteEpochDays && color == other.color
    }

    override fun hashCode(): Int {
        var result = noteEpochDays.hashCode()
        result = 31 * result + color.hashCode()
        return result
    }
}

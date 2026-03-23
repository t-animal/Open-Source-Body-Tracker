package de.t_animal.opensourcebodytracker.feature.measurements.helpers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.ui.helpers.displaySymbol

/**
 * Worst-case display value per unit — physical extreme plus one extra digit of headroom.
 * Values are already in display units (i.e. post-conversion for imperial).
 */
private fun BodyMetricUnit.worstCaseDisplayValue(): Double = when (this) {
    BodyMetricUnit.Kilogram   -> 9999.99  // covers both ~9999 kg and ~9999 lbs
    BodyMetricUnit.Centimeter -> 999.99   // covers both ~999 cm and ~999 in
    BodyMetricUnit.Millimeter -> 999.99
    BodyMetricUnit.Percent    -> 100.00
    BodyMetricUnit.Unitless   -> 100.00
}

/** Worst-case date string — covers wide locale formats like "30/12/2026". */
private const val WORST_CASE_DATE = "30-12-20260"

/** Maximum number of lines a header label is allowed to wrap to. */
private const val MAX_HEADER_LINES = 2

@Immutable
internal data class TableColumnWidths(
    val date: Dp,
    val metrics: List<Dp>,
)

@Composable
internal fun rememberTableColumnWidths(
    dateHeaderText: String,
    visibleMetricHeaders: List<String>,
    visibleMetrics: List<BodyMetric>,
    unitSystem: UnitSystem,
    headerStyle: TextStyle,
    bodyStyle: TextStyle,
    cellPadding: Dp,
): TableColumnWidths {
    val metricWorstCaseTexts = visibleMetrics.map { metric ->
        val number = "%.2f".format(metric.unit.worstCaseDisplayValue())
        val symbol = metric.unit.displaySymbol(unitSystem)
        if (symbol.isEmpty()) number else "$number $symbol"
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    return remember(
        dateHeaderText, visibleMetricHeaders, metricWorstCaseTexts,
        headerStyle, bodyStyle, density,
    ) {
        val datePx = maxOf(
            textMeasurer.measure(dateHeaderText, headerStyle).size.width,
            textMeasurer.measure(WORST_CASE_DATE, bodyStyle).size.width,
        )
        val dateWidth = with(density) { datePx.toDp() } + cellPadding

        val metricWidths = visibleMetricHeaders.zip(metricWorstCaseTexts) { header, worstCase ->
            val valuePx = textMeasurer.measure(worstCase, bodyStyle).size.width
            val headerMinPx = textMeasurer.minWidthForLines(header, headerStyle, MAX_HEADER_LINES)
            with(density) { maxOf(valuePx, headerMinPx).toDp() } + cellPadding
        }

        TableColumnWidths(date = dateWidth, metrics = metricWidths)
    }
}

/**
 * Finds the narrowest width at which [text] fits within [maxLines] using binary search.
 *
 * The idea: we want headers to wrap (e.g. "Körperfett Hautfalten" → two lines) so columns
 * stay narrow. But we cap at [maxLines] to avoid cases like "fat of the body measured 
 * by skin folds" wrapping across 8 lines (languages are wild).
 *
 * - If the text already fits in [maxLines] at width 0 (i.e. it's a single short word),
 *   we just return its intrinsic width.
 * - Otherwise, we binary search between 0 and the unconstrained single-line width
 *   to find the smallest width where lineCount <= [maxLines]. This respects word
 *   boundaries because TextMeasurer performs real line-breaking at each candidate width.
 */
private fun TextMeasurer.minWidthForLines(
    text: String,
    style: TextStyle,
    maxLines: Int,
): Int {
    val unconstrained = measure(text, style)

    // Single word with no whitespace — can't wrap, so the full width is the minimum.
    if (!text.contains(Regex("\\s"))) return unconstrained.size.width

    // Binary search for the smallest width where the text fits in maxLines.
    // Even if the text already fits on one line unconstrained, we still search because
    // wrapping it into maxLines may yield a narrower column (e.g. "Körperfett Navy"
    // is one line unconstrained but can wrap to two lines at roughly half the width).
    // Low bound: width of the longest word — going narrower would force mid-word breaks
    // (e.g. "Körperfet\nt" instead of "Körperfett\nNavy").
    // High bound: full single-line width (always fits in 1 line).
    var lo = text.split(Regex("\\s+")).maxOf { measure(it, style).size.width }
    var hi = unconstrained.size.width
    while (lo < hi) {
        val mid = (lo + hi) / 2
        val result = measure(text, style, constraints = Constraints(maxWidth = mid))
        if (result.lineCount <= maxLines) {
            hi = mid
        } else {
            lo = mid + 1
        }
    }
    return lo
}

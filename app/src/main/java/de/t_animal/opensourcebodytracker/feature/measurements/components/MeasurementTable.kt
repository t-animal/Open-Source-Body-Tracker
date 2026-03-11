package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListItemUiModel
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.formattedValue
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.label
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate

private val TABLE_DATE_CELL_WIDTH = 90.dp

private fun BodyMetric.tableCellWidth(): Dp = when (unit) {
    BodyMetricUnit.Kilogram   -> 80.dp  // e.g. "300.00 kg"
    BodyMetricUnit.Centimeter -> 80.dp  // e.g. "200.00 cm"
    BodyMetricUnit.Millimeter -> 68.dp  // e.g. "50.00 mm"
    BodyMetricUnit.Percent    -> 70.dp  // e.g. "60.00 %"
    BodyMetricUnit.Unitless   -> 60.dp  // e.g. "60.00"
}

@Composable
internal fun MeasurementTable(
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

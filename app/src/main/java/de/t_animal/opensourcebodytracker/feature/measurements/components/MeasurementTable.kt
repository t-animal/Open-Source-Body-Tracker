package de.t_animal.opensourcebodytracker.feature.measurements.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.BodyMeasurement
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.core.model.BodyMetricUnit
import de.t_animal.opensourcebodytracker.core.model.DerivedMetrics
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.feature.measurements.MeasurementListItemUiModel
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.formattedValue
import de.t_animal.opensourcebodytracker.feature.measurements.helpers.label
import de.t_animal.opensourcebodytracker.ui.components.formatEpochMillisToLocalizedNumericDate
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import kotlinx.coroutines.launch

private val TABLE_DATE_CELL_WIDTH = 95.dp
private val TABLE_NOTE_CELL_WIDTH = 180.dp

private fun BodyMetric.tableCellWidth(): Dp = when (unit) {
    BodyMetricUnit.Kilogram   -> 80.dp  // e.g. "300.00 kg"
    BodyMetricUnit.Centimeter -> 80.dp  // e.g. "200.00 cm"
    BodyMetricUnit.Millimeter -> 68.dp  // e.g. "50.00 mm"
    BodyMetricUnit.Percent    -> 70.dp  // e.g. "60.00 %"
    BodyMetricUnit.Unitless   -> 60.dp  // e.g. "60.00"
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
internal fun MeasurementTable(
    items: List<MeasurementListItemUiModel>,
    visibleMetrics: List<BodyMetric>,
    onRowSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    selectedIds: Set<Long> = emptySet(),
    unitSystem: UnitSystem,
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
                text = stringResource(R.string.table_header_date),
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
            Text(
                text = stringResource(R.string.table_header_note),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .width(TABLE_NOTE_CELL_WIDTH)
                    .padding(horizontal = 8.dp),
            )
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
            val isSelected = item.measurement.id in selectedIds
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScroll)
                    .then(
                        if (isSelected) Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
                        else Modifier
                    )
                    .clickable { onRowSelect(item.measurement.id) }
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
                        text = column.formattedValue(item, unitSystem),
                        modifier = Modifier
                            .width(column.tableCellWidth())
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                NoteCell(
                    note = item.measurement.note.orEmpty(),
                    modifier = Modifier.width(TABLE_NOTE_CELL_WIDTH),
                )
            }

            HorizontalDivider()
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun NoteCell(
    note: String,
    modifier: Modifier = Modifier,
) {
    if (note.isBlank()) {
        Spacer(modifier = modifier)
        return
    }

    val tooltipState = rememberTooltipState(isPersistent = true)
    val scope = rememberCoroutineScope()
    Row(
        modifier = modifier
                    .padding(end = 4.dp)
                    .clip(shape=RoundedCornerShape(4.dp))
                    .clickable(role = Role.Button) {
                        scope.launch {
                            if (tooltipState.isVisible) {
                                tooltipState.dismiss()
                            } else {
                                tooltipState.show()
                            }
                        }
                    }
                    .padding(start = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = note,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above, 8.dp),
            tooltip = {
                PlainTooltip {
                    Text(note)
                }
            },
            state = tooltipState,
            enableUserInput = false,
        ) {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = stringResource(
                    if (tooltipState.isVisible) R.string.cd_hide_note else R.string.cd_show_full_note
                ),
                modifier = Modifier
                    .size(18.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 420)
@Composable
private fun MeasurementTablePreview() {
    BodyTrackerTheme {
        MeasurementTable(
            items = listOf(
                MeasurementListItemUiModel(
                    measurement = BodyMeasurement(
                        id = 1,
                        dateEpochMillis = 1_712_000_000_000,
                        weightKg = 82.4,
                        waistCircumferenceCm = 88.3,
                        note = "Fasted morning check-in",
                    ),
                    derivedMetrics = DerivedMetrics(),
                ),
                MeasurementListItemUiModel(
                    measurement = BodyMeasurement(
                        id = 2,
                        dateEpochMillis = 1_711_000_000_000,
                        weightKg = 82.1,
                        bodyFatPercent = 17.3,
                        waistCircumferenceCm = 88.0,
                    ),
                    derivedMetrics = DerivedMetrics(),
                ),
            ),
            visibleMetrics = listOf(
                MeasuredBodyMetric.Weight,
                MeasuredBodyMetric.WaistCircumference,
            ),
            onRowSelect = {},
            selectedIds = setOf(1L),
            unitSystem = UnitSystem.Metric,
        )
    }
}

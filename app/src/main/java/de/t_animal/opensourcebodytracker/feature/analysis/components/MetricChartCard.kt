package de.t_animal.opensourcebodytracker.feature.analysis.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.t_animal.opensourcebodytracker.R
import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.feature.analysis.AnalysisMetricChartUiModel
import de.t_animal.opensourcebodytracker.feature.analysis.helpers.analysisTitle
import java.time.LocalDate

private val CHART_HEIGHT = 220.dp

@Composable
internal fun MetricChartCard(
    chart: AnalysisMetricChartUiModel,
    duration: AnalysisDuration,
    selectedDate: LocalDate?,
    onSelectedDateChange: (LocalDate?) -> Unit,
    onNoteSelected: (String?) -> Unit,
    isCollapsed: Boolean,
    onToggleCollapsed: () -> Unit,
    dragHandleModifier: Modifier,
    unitSystem: UnitSystem,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = chart.definition.analysisTitle(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleCollapsed) {
                    Icon(
                        imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                        contentDescription = stringResource(if (isCollapsed) R.string.cd_expand else R.string.cd_collapse),
                    )
                }
                Icon(
                    imageVector = Icons.Default.DragHandle,
                    contentDescription = stringResource(R.string.cd_drag_to_reorder),
                    modifier = dragHandleModifier,
                )
            }

            if (!isCollapsed) {
                if (chart.points.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CHART_HEIGHT),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.analysis_chart_no_data),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    MetricLineChart(
                        chart = chart,
                        duration = duration,
                        selectedDate = selectedDate,
                        onSelectedDateChange = onSelectedDateChange,
                        onNoteSelected = onNoteSelected,
                        unitSystem = unitSystem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(CHART_HEIGHT),
                    )
                }
            }
        }
    }
}

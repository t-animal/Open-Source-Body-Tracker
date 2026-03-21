package de.t_animal.opensourcebodytracker.feature.analysis

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import de.t_animal.opensourcebodytracker.core.model.AnalysisDuration
import de.t_animal.opensourcebodytracker.core.model.BodyMetric
import de.t_animal.opensourcebodytracker.feature.analysis.components.MetricChartCard
import de.t_animal.opensourcebodytracker.ui.theme.BodyTrackerTheme
import java.time.LocalDate
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun AnalysisRoute(
    contentPadding: PaddingValues,
) {
    val vm: AnalysisViewModel = hiltViewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    AnalysisScreen(
        state = state,
        onDurationSelected = vm::onDurationSelected,
        onChartOrderChanged = vm::onChartOrderChanged,
        onToggleCollapsed = vm::onToggleCollapsed,
        contentPadding = contentPadding,
    )
}

@Composable
fun AnalysisScreen(
    state: AnalysisUiState,
    onDurationSelected: (AnalysisDuration) -> Unit,
    onChartOrderChanged: (List<BodyMetric>) -> Unit,
    onToggleCollapsed: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var displayedNoteText by remember { mutableStateOf<String?>(null) }

    NoteBottomSheetScaffold(
        noteText = displayedNoteText,
        onNoteTextCleared = { displayedNoteText = null },
        modifier = Modifier.padding(contentPadding),
    ) {
        val lazyListState = rememberLazyListState()
        val headerItemCount = 2
        val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
            val reordered = state.metricCharts.toMutableList().apply {
                add(to.index - headerItemCount, removeAt(from.index - headerItemCount))
            }
            onChartOrderChanged(reordered.map { it.definition })
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap a point to show value, long tap to show note",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(state.metricCharts, key = { it.definition.id }) { chart ->
                ReorderableItem(reorderableState, key = chart.definition.id) { _ ->
                    MetricChartCard(
                        chart = chart,
                        duration = state.selectedDuration,
                        selectedDate = selectedDate,
                        onSelectedDateChange = { selectedDate = it },
                        onNoteSelected = { displayedNoteText = it },
                        isCollapsed = chart.definition.id in state.collapsedChartIds,
                        onToggleCollapsed = { onToggleCollapsed(chart.definition.id) },
                        dragHandleModifier = Modifier.draggableHandle(),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteBottomSheetScaffold(
    noteText: String?,
    onNoteTextCleared: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.Hidden,
        skipHiddenState = false,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState)

    LaunchedEffect(sheetState.currentValue) {
        if (sheetState.currentValue == SheetValue.Hidden || sheetState.currentValue == SheetValue.PartiallyExpanded) {
            onNoteTextCleared()
        }
    }

    LaunchedEffect(noteText) {
        if (noteText != null) {
            sheetState.expand()
        } else {
            sheetState.hide()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            Text(
                text = noteText ?: "",
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        modifier = modifier,
    ) {
        content()
    }
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
            onChartOrderChanged = {},
            onToggleCollapsed = {},
            contentPadding = PaddingValues(),
        )
    }
}

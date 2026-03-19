# Feature Specification: Chart Note Markers — Bottom Sheet Display

**Platform Context**: Jetpack Compose / Min SDK 26 / Material 3

## Purpose

When a user taps a data point on an analysis chart that has an associated note, a bottom sheet appears displaying that note text. This replaces the previous tooltip overlay approach (`NoteMarkerTooltipOverlay` using `PlainTooltip`/`TooltipBox`) with a single screen-level Material 3 bottom sheet. The visual markers on the chart (dotted vertical line and triangle rendered by `NoteMarkerDecoration`) remain unchanged and are decorative only.

## Functional Requirements

- **FR-1**: When a user taps a chart data point that has a non-null, non-blank `note` field on its `AnalysisChartPoint`, a bottom sheet shall appear containing only the note text.
- **FR-2**: When a user taps a chart data point that has no note (null or blank), no bottom sheet shall appear. The existing selected-point behavior (marker label near the point) continues to work as before.
- **FR-3**: The bottom sheet shall be a single instance owned by `AnalysisScreen`, shared across all `MetricChartCard` composables in the `LazyColumn`.
- **FR-4**: If a bottom sheet is already showing for a data point on Chart A and the user taps a data point with a note on Chart B, the bottom sheet shall be replaced with the note from Chart B.
- **FR-5**: If a bottom sheet is showing and the user taps a data point that has no note (on any chart), the bottom sheet shall be dismissed.
- **FR-6**: The bottom sheet shall be dismissed when the user taps anywhere that is not a data point (including tapping the scrim, swiping down, or tapping empty chart area).
- **FR-7**: The existing `NoteMarkerTooltipOverlay` composable and all tooltip-related code for note display shall be removed.
- **FR-8**: The existing selected-point summary display (marker label in `MetricChartCard`) shall not be modified.
- **FR-9**: The visual `NoteMarkerDecoration` (dotted line and triangle) shall remain unchanged and purely decorative (not a tap target).

## Non-Functional Requirements

- **NFR-1**: The bottom sheet shall use standard Material 3 bottom sheet animation (slide up/down).
- **NFR-2**: The bottom sheet shall be as small as possible to display the note.
- **NFR-3**: The bottom sheet maximum height shall be at most 30% of the screen height (exact value TBD). If note text exceeds the visible area, the content shall be vertically scrollable.
- **NFR-4**: The bottom sheet shall appear and dismiss without perceptible lag (under 100ms to begin animation after tap).

## Android Technical Constraints

- **Lifecycle**: The bottom sheet state is ephemeral UI state. On configuration change (rotation), it is acceptable for the bottom sheet to be dismissed. The underlying selected-date state in each `MetricChartCard` already survives recomposition via `remember`/`mutableStateOf`.
- **Permissions**: No additional permissions required.
- **Storage**: No storage changes. Notes are already loaded into `AnalysisChartPoint.note` from the existing data layer.
- **State Flow**: `AnalysisScreen` holds state representing the currently displayed note text (or null if no bottom sheet is shown). Each `MetricChartCard` communicates "a data point with this note was selected" up to `AnalysisScreen` via a callback. The existing `onSelectedDateChange`/`selectedDate` pattern provides a model for this.

## Edge Cases

- **No notes exist**: No bottom sheet ever appears. Charts render identically to the pre-feature state (except for the decorative markers, which also won't appear if no notes exist).
- **Very long note text**: Handled by the scrollable content area within the ~30% max-height constraint.
- **Rapid tapping across charts**: Only the most recent note is displayed (no stacking or queuing).
- **Blank note**: Empty string or whitespace-only note is treated the same as null — no bottom sheet.
- **Originating chart scrolled off-screen**: If the LazyColumn scrolls such that the originating chart is no longer visible, the bottom sheet may remain visible until explicitly dismissed or replaced.
- **Single data point**: The chart currently returns early when fewer than 2 data points exist, so no markers or bottom sheet interaction occurs.

## Test Scenarios

| ID | Type | Scenario | Expected Result |
|----|------|----------|-----------------|
| T-1 | Unit | Tap a data point where `note` is non-null and non-blank | Bottom sheet state is set to the note text; bottom sheet becomes visible |
| T-2 | Unit | Tap a data point where `note` is null | Bottom sheet state remains null; no bottom sheet shown |
| T-3 | Unit | Bottom sheet showing for Chart A note; tap a noted data point on Chart B | Bottom sheet updates to Chart B note text |
| T-4 | Unit | Bottom sheet showing; tap a data point with no note on any chart | Bottom sheet is dismissed |
| T-5 | Instrumented | Tap a noted data point, verify bottom sheet appears with correct text, then tap outside | Bottom sheet appears with expected note, then dismisses on outside tap |

## Key Files

- `feature/analysis/AnalysisScreen.kt` — screen-level composable that will own the bottom sheet state
- `feature/analysis/components/MetricChartCard.kt` — card composable, needs callback for note selection
- `feature/analysis/components/MetricLineChart.kt` — chart composable where tap detection occurs; tooltip overlay to be removed
- `feature/analysis/components/NoteMarkerDecoration.kt` — decorative markers, no changes needed
- `feature/analysis/AnalysisModels.kt` — `AnalysisChartPoint` with `note: String?` field

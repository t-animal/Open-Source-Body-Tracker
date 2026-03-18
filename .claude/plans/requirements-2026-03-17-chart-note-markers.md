# Feature Specification: Chart Note Markers

**Platform Context**: Jetpack Compose / Min SDK 26 / Material 3

## Purpose

Users who attach notes to measurement entries currently can only see those notes in the table view. This feature adds visual indicators on the chart so users can spot which dates have notes and read them without leaving the chart. The design uses a vertical dotted line capped by a downward-pointing triangle, with a Material 3 tooltip to display the full note text on tap.

## Functional Requirements

- **FR-1**: For every measurement in the chart whose `note` field is non-empty, a vertical dotted line shall be drawn from the bottom of the chart area upward to near the top of the chart area, at the x-coordinate corresponding to that measurement's date.
- **FR-2**: The dotted line shall be thinner than the 3dp data line (target approximately 1-1.5dp) and drawn in a subdued color (e.g., `MaterialTheme.colorScheme.outlineVariant` or similar) so it does not visually compete with the data line.
- **FR-3**: At the top of each dotted line, a small downward-pointing triangle with rounded corners shall be drawn. Material Shapes should be used if practical; otherwise a Canvas path with rounded joins is acceptable. The triangle uses the same subdued color as the dotted line.
- **FR-4**: Tapping the triangle indicator shall display a Material 3 `PlainTooltip` inside a `TooltipBox` showing the full, untruncated note text. This follows the same pattern used by `NoteCell` in the table view (`rememberTooltipState`, `scope.launch { tooltipState.show() }`).
- **FR-5**: The tooltip shall dismiss when the user taps anywhere outside the tooltip.
- **FR-6**: If a tooltip for note A is visible and the user taps the triangle for note B, tooltip A shall auto-dismiss. A second tap on triangle B to open its tooltip is acceptable (i.e., the first tap may only dismiss A).
- **FR-7**: Tapping on the data point circle on the chart line shall continue to show the existing value/date selection label. Tapping the triangle indicator shall show the note tooltip. These are two independent, non-overlapping tap targets.
- **FR-8**: The triangle tap target shall be large enough for comfortable touch interaction (minimum 48dp effective touch target per Material accessibility guidelines), even if the visible triangle is smaller.

## Non-Functional Requirements

- **NFR-1**: The note markers shall not introduce visible jank. Drawing additional paths on the Canvas must not degrade scroll or render performance for charts with up to 365 data points.
- **NFR-2**: The visual design of the markers (line + triangle) shall be consistent with Material 3 theming, adapting correctly to light and dark modes via `MaterialTheme.colorScheme` tokens.
- **NFR-3**: The tooltip text style shall match the existing `NoteCell` tooltip (`MaterialTheme.typography.bodySmall`).

## Android Technical Constraints

- **Lifecycle**: The tooltip state is ephemeral (in-memory via `rememberTooltipState`). On configuration change (rotation, theme toggle), any open tooltip may dismiss and the chart recomposes; this is acceptable since the markers will re-render and can be tapped again. The selected data point index already resets on recomposition in the current chart.
- **Permissions**: None required. All data is already available in the `Measurement` domain model passed to the chart.
- **Storage**: The `note` field already exists on the `Measurement` data class (source of truth: Room database). No schema or query changes are needed. The chart composable receives `List<Measurement>` which already contains the note data.

## Edge Cases

- **No notes exist**: No markers are drawn. The chart renders identically to today.
- **All entries have notes**: Every date position gets a marker line and triangle. No clutter-management or aggregation is required per the user's decision.
- **Very long note text**: The tooltip displays the full text without truncation. The `PlainTooltip` component handles wrapping and overflow natively.
- **Single data point**: The chart currently returns early when `measurements.size < 2`, so no markers are drawn in that case.
- **Data point and note on the same date**: Both the data-point circle tap target and the triangle tap target exist at the same x-coordinate but at different y-positions (triangle is near chart top; data point is at the value's y-position). They must not overlap. If the data value is near the chart top and the targets risk overlapping, the triangle tap target takes precedence in its region (upper area) and the data point tap target takes precedence in its region (at the point).

## Test Scenarios

| ID | Type | Scenario | Expected Result |
|----|------|----------|-----------------|
| T-1 | Unit | Given a list of measurements where none have notes, verify that note marker data (line coordinates, triangle positions) is empty. | No marker geometry is produced. |
| T-2 | Unit | Given a list of measurements where entries at indices 1 and 3 have non-empty notes, verify that exactly 2 marker positions are computed at the correct x-coordinates. | Two marker positions matching the x-coordinates of indices 1 and 3. |
| T-3 | Instrumented | Display a chart with at least one noted entry. Tap the triangle indicator. | A tooltip appears showing the full note text. |
| T-4 | Instrumented | Display a chart with two noted entries. Tap triangle A to show tooltip A, then tap triangle B. | Tooltip A dismisses. A second tap on triangle B shows tooltip B. |
| T-5 | Instrumented | Display a chart with a noted entry. Tap the data point circle at the same date. | The existing value/date selection label appears. No note tooltip is shown. |

## Key Files

- `app/src/main/java/com/nicholasfragiskatos/openbt/ui/chart/MeasurementChart.kt` — the Canvas composable where markers will be added
- `app/src/main/java/com/nicholasfragiskatos/openbt/ui/table/NoteCell.kt` — reference implementation for the PlainTooltip/TooltipBox pattern
- `app/src/main/java/com/nicholasfragiskatos/openbt/domain/model/Measurement.kt` — domain model already containing the `note` field

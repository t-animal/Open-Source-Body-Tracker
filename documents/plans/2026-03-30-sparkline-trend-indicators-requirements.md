# Feature Specification: Sparkline Trend Visualization in LastMeasurementsCard

**Platform Context**: Compose / Min SDK 26 / Material3

---

## Purpose

Users need a quick visual sense of whether a tracked metric is trending up, down, or staying flat over recent recordings. A compact, read-only sparkline embedded directly in the `LastMeasurementsCard` provides this at a glance without requiring navigation to a separate analysis screen.

---

## Functional Requirements

- **FR-1**: The `MetricValueDisplay` composable shall render a sparkline chart directly below the metric value text, within the same vertically-arranged column.
- **FR-2**: The sparkline shall appear only when the filtered set of measurements for that metric contains 2 or more data points. If the count is 0 or 1, the area below the value text shall remain empty with no placeholder or label.
- **FR-3**: The sparkline shall use the last 10 measurements for the given metric. These shall be sourced by filtering `LogUiState.measurements` by `metricId`, taking the first 10 entries (which are already ordered most-recent-first due to `timestamp DESC` ordering from the DAO), and then reversing that subset so the oldest entry appears on the left and the newest on the right.
- **FR-4**: The x-axis shall be time-proportional. Each data point's horizontal position shall be derived from its raw Unix timestamp in milliseconds, so that uneven recording intervals are accurately represented as uneven spacing along the x-axis.
- **FR-5**: The sparkline shall be rendered as a filled area chart: a solid line along the top edge with a filled region below it. The line color shall be the Material3 `primary` color. The fill color shall be the Material3 `onSurface` color at reduced alpha. The exact alpha value is an implementation detail but shall be defined as a named constant for easy adjustment.
- **FR-6**: The sparkline height shall be approximately 38dp (equivalent to ~1 cm on a standard display). This value shall be defined as a named constant to allow easy future adjustment.
- **FR-7**: The sparkline shall be rendered using the Vico charting library, which is already declared as a dependency.
- **FR-8**: The sparkline shall be entirely non-interactive. There shall be no tap handling, no selection state, no tooltip, and no highlight on touch.
- **FR-9**: Linear interpolation between recorded data points (for rendering the chart line and fill) is delegated to Vico's default behavior. No custom interpolation logic is required.
- **FR-10**: The horizontal `Row` layout of `LastMeasurementsCard` shall be preserved as-is. No changes to card-level layout are required.

---

## Data Model Changes

- **DM-1**: `MeasurementUiModel` must gain a `timestamp: Long` field representing the raw Unix timestamp in milliseconds. This field is already present on `MeasurementEntity` and must be mapped through to the UI model so that `MetricValueDisplay` can compute time-proportional x positions.
- **DM-2**: All existing construction sites of `MeasurementUiModel` must be updated to supply the new `timestamp` value explicitly. No default parameter value shall be introduced for backward compatibility.

---

## Non-Functional Requirements

- **NFR-1**: The sparkline shall not trigger any new database queries or network calls. It shall derive its data entirely from the `measurements` list already present in `LogUiState`.
- **NFR-2**: Filtering and subsetting of measurements per metric (FR-3) shall occur in the ViewModel or a mapper layer, not inside the composable. The composable shall receive a pre-filtered, pre-ordered list of `MeasurementUiModel` values.
- **NFR-3**: The sparkline height constant and fill alpha constant shall each be defined in a single location and referenced by name, not hardcoded inline.
- **NFR-4**: The feature shall support both light and dark themes. `primary` and `onSurface` are Material3 dynamic color roles and shall be resolved from the active `MaterialTheme.colorScheme` at runtime.

---

## Android Technical Constraints

- **Lifecycle**: The sparkline is a stateless, data-driven composable. It holds no ViewModel state of its own. It recomposes when `LogUiState.measurements` changes.
- **Permissions**: None required.
- **Storage**: Read-only. No writes to Room or any other storage layer.
- **Minimum SDK**: Min SDK 26. No API-level guards are needed for this feature.

---

## Edge Cases and Acceptance Criteria

| ID | Scenario | Preconditions | Action | Expected Result |
|----|----------|---------------|--------|-----------------|
| T-1 | No measurements for metric | Metric exists, zero `MeasurementUiModel` entries with matching `metricId` | Log screen loads | No sparkline rendered below the metric value; column shows only name and value text |
| T-2 | Exactly one measurement | One `MeasurementUiModel` entry matches `metricId` | Log screen loads | No sparkline rendered; behavior identical to T-1 |
| T-3 | Exactly two measurements | Two entries match `metricId` | Log screen loads | Sparkline renders; a single line segment connects the two time-proportional x positions |
| T-4 | More than 10 measurements | 15 entries match `metricId`, all ordered DESC by timestamp | Log screen loads | Sparkline uses only the 10 most recent entries; oldest 5 are ignored |
| T-5 | Uneven time gaps | 5 entries with non-uniform intervals between timestamps | Log screen loads | X positions are visually unequal, proportional to actual elapsed time between recordings |
| T-6 | `MeasurementUiModel` timestamp mapping | `MeasurementEntity` has a known timestamp value | ViewModel maps entity to UI model | `MeasurementUiModel.timestamp` equals the entity's `timestamp` field exactly |
| T-7 | Theme change | System switches between light and dark mode while Log screen is visible | Theme toggle applied | Line color and fill color update to reflect the new `MaterialTheme.colorScheme` values without restart |

---

## Out of Scope

- No changes to the full measurement log table on the Log screen.
- No changes to the Analysis screen or any other screen.
- No per-metric color coding or semantic trend colors (e.g., red for decline, green for growth).
- No tap-to-expand, tooltip, or detail-on-tap interaction.
- No axis labels, gridlines, or legend on the sparkline.
- No animation on initial appearance or on data change.
- No new DAO queries or per-metric LIMIT queries; the existing `getAllMeasurements()` call is the sole data source.

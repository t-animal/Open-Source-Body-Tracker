# Feature Concept: Chart Note Markers on Analysis Screen

## Executive Summary

Display visual markers on the analysis screen charts for entries that contain notes, allowing users to correlate their textual annotations (e.g., "started new diet", "was sick, no sports") with visible changes in their measurement data over time. This bridges the gap between the qualitative context users already capture in per-entry notes and the quantitative trends shown on charts.

## The "Why"

- **User Problem**: Users record notes on individual body measurement entries to capture context about lifestyle changes, illness, or routine shifts. However, when reviewing trends on the analysis charts, those notes are invisible. Users cannot easily answer the question "why did my weight spike here?" without leaving the chart and manually cross-referencing entry dates.
- **Business Value**: This increases the value of the existing notes feature by surfacing context where it matters most — during trend analysis. It encourages users to write more notes (knowing they will be useful later) and deepens engagement with the analysis screen, supporting long-term retention.
- **Success Metric**: Increased usage of the per-entry notes field, measured by the proportion of new entries that include a note. Secondary metric: increased time spent on the analysis screen, indicating users are engaging more deeply with their data.

## User Journey

- **Persona**: A health-conscious individual who tracks body measurements regularly (multiple times per week) and occasionally annotates entries with lifestyle context. They review their charts periodically to understand trends and make decisions about diet or exercise adjustments.
- **The "Happy Path"**:
  1. User opens the analysis screen and views a measurement chart (e.g., weight over time).
  2. User notices one or more subtle vertical dotted marker lines on the chart at specific dates.
  3. User recognizes these indicate entries with notes.
  4. User taps the small downward-pointing triangle at the top of a marker line.
  5. A tooltip appears displaying the note text (e.g., "Started keto diet").
  6. User correlates the note with the visible trend change on the chart.
  7. User taps anywhere else on the chart to dismiss the tooltip.

## Proposed Solution

### Visual Design

- **Marker Line**: A vertical dotted line drawn from the bottom of the chart area to near the top, positioned at the x-coordinate corresponding to the date of each entry that has a non-null, non-empty note. The line must be thinner than the main chart data line and rendered with a dotted stroke pattern so it reads as secondary information.
- **Marker Indicator**: A small triangle with rounded corners, positioned at the top of the dotted line, pointing downward toward the line. This serves as both a visual anchor and a tap target. The rounded corners should use Material Shapes conventions if the custom Canvas rendering supports it; otherwise, a manual rounded-triangle path is acceptable.
- **Color Treatment**: The marker line and triangle use a single distinct but subdued color — visually closer to a background element than a highlight. The color must not compete with the primary data line or the chart grid. It should remain legible in both light and dark themes. Think of it as annotation-layer coloring: present but not dominant.

### Interaction Model

- **Tap to Reveal**: Tapping the triangle indicator at the top of a marker line displays a tooltip containing the full note text for that entry.
- **Tap to Dismiss**: Tapping anywhere else on the chart dismisses the currently visible tooltip. Only one tooltip is visible at a time.
- **No Hover State**: Since this is a touch-only Android context, there is no hover interaction. The triangle itself is the affordance that signals interactivity.

## Scope and Constraints

### In-Scope (MVP)

- Render vertical dotted marker lines on the existing `MeasurementChart` composable for all entries where `notes` is non-null and non-empty.
- Render a small rounded-corner downward-pointing triangle at the top of each marker line.
- Implement tap detection on the triangle area to show a tooltip with the note text.
- Implement tap-anywhere-else dismissal of the tooltip.
- Ensure markers render correctly across different chart zoom levels and date ranges.
- Support both light and dark themes with appropriate marker color.

### Out of Scope

- **Clutter management**: No filtering, collapsing, or limiting of markers when many notes exist. The current assumption is that users do not annotate frequently enough for visual clutter to be a problem. This will be revisited if usage patterns change.
- **Note editing from the chart**: Users cannot create or edit notes from the analysis screen. Note management remains on the entry screen.
- **Multi-metric correlation**: Markers appear only on the chart for the metric associated with the entry. No cross-chart marker synchronization.
- **Search or filter by notes**: No ability to search for specific note text or filter the chart to show only annotated entries.
- **Animation**: No animated transitions for marker appearance or tooltip display in the first version.

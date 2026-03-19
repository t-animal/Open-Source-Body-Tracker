# Feature Concept: Chart Note Markers on Analysis Screen

## Executive Summary

Display visual markers on the analysis screen charts for entries that contain notes, allowing users to correlate their textual annotations (e.g., "started new diet", "was sick, no sports") with visible changes in their measurement data over time. When a user taps a data point that has a note, a bottom sheet slides up showing the note text — in addition to the existing marker label.

## The "Why"

- **User Problem**: Users record notes on individual body measurement entries to capture context about lifestyle changes, illness, or routine shifts. However, when reviewing trends on the analysis charts, those notes are invisible. Users cannot easily answer the question "why did my weight spike here?" without leaving the chart and manually cross-referencing entry dates.
- **Business Value**: This increases the value of the existing notes feature by surfacing context where it matters most — during trend analysis. It encourages users to write more notes (knowing they will be useful later) and deepens engagement with the analysis screen, supporting long-term retention.
- **Success Metric**: Increased usage of the per-entry notes field, measured by the proportion of new entries that include a note. Secondary metric: increased time spent on the analysis screen, indicating users are engaging more deeply with their data.

## User Journey

- **Persona**: A health-conscious individual who tracks body measurements regularly (multiple times per week) and occasionally annotates entries with lifestyle context. They review their charts periodically to understand trends and make decisions about diet or exercise adjustments.
- **The "Happy Path"**:
  1. User opens the analysis screen and views a measurement chart (e.g., weight over time).
  2. User notices small triangle markers with dotted vertical lines on certain dates, signaling that notes exist there.
  3. User spots a weight spike near one of these markers and taps the **data point dot on the chart line** at that date.
  4. The existing Vico marker label appears near the point showing the value and date. Simultaneously, a bottom sheet slides up from the bottom of the screen displaying the full note text for that entry.
  5. User reads the note (e.g., "Started keto diet") and understands the trend change.
  6. User taps anywhere outside a data point to dismiss the bottom sheet and continues reviewing the chart.

## Proposed Solution

### Visual Design

- **Marker Line**: A vertical dotted line drawn from the bottom of the chart area to near the top, positioned at the x-coordinate corresponding to the date of each entry that has a non-null, non-empty note. The line must be thinner than the main chart data line and rendered with a dotted stroke pattern so it reads as secondary information.
- **Marker Indicator**: A small triangle with rounded corners, positioned at the top of the dotted line, pointing downward toward the line. This serves as a visual cue that a note exists at that date. The rounded corners should use Material Shapes conventions if the custom Canvas rendering supports it; otherwise, a manual rounded-triangle path is acceptable.
- **Color Treatment**: The marker line and triangle use a single distinct but subdued color — visually closer to a background element than a highlight. The color must not compete with the primary data line or the chart grid. It should remain legible in both light and dark themes.
- **Important**: The triangles and lines are **purely decorative and informational**. They are not tap targets.

### Interaction Model

- **Tap Data Point**: The user taps a normal data point dot on the chart line. Vico's existing marker controller handles the tap detection and displays its standard marker label (value and date) near the tapped point.
- **Bottom Sheet for Notes**: If the tapped data point has an associated note, a Material 3 bottom sheet slides up displaying the note text. This appears **in addition to** the standard marker label, not as a replacement. The bottom sheet must not exceed ~30% of screen height (exact value TBD). If the note text requires more space, it should be scrollable within the sheet.
- **One at a Time**: Only one bottom sheet is visible at a time. Tapping a different data point dismisses the current sheet and opens a new one if that point also has a note.
- **Dismissal**: Tapping anywhere that is not a data point dismisses the bottom sheet.
- **Animation**: The bottom sheet uses standard Material 3 sheet animation (slide up on appear, slide down on dismiss).

## Scope and Constraints

### In-Scope (MVP)

- Render vertical dotted marker lines on the chart for all entries where `note` is non-null and non-empty.
- Render a small rounded-corner downward-pointing triangle at the top of each marker line (decorative only).
- Display note text in a bottom sheet when a noted data point is tapped.
- Ensure markers render correctly across different chart zoom levels and date ranges.
- Support both light and dark themes with appropriate marker color.
- Standard Material 3 bottom sheet animation.

### Out of Scope

- **Clutter management**: No filtering, collapsing, or limiting of markers when many notes exist. Deferred to a future iteration if needed.
- **Note editing from the chart**: Users cannot create or edit notes from the analysis screen. Note management remains on the entry screen.
- **Multi-metric correlation**: Markers appear only on the chart for the metric associated with the entry. No cross-chart marker synchronization.
- **Search or filter by notes**: No ability to search for specific note text or filter the chart to show only annotated entries.
- **Custom animation**: No animated transitions for the triangle or dotted-line markers themselves.

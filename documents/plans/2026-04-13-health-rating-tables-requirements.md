# Feature Specification: Health Rating Tables

## Purpose

Users need a way to understand the health classification scales shown in the Latest Measurements grid. Two entry points are provided:

1. **Per-metric bottom sheet**: An info icon on each rated metric card opens a bottom sheet showing the rating table for that specific metric, with the user's current rating highlighted.
2. **All Ratings screen**: A full-screen page accessible from the top app bar overflow menu lists rating tables for all metrics.

---

## Functional Requirements

### FR-1 — Info icon on metric cards

Each metric card in `LatestMeasurementGrid` (inside `LatestMeasurementCard.kt`) that currently displays a non-null rating label must show a small info icon (e.g. `Icons.Outlined.Info`). The icon is hidden when no rating is computed for that card at runtime (i.e. the rating field is `null`).

### FR-2 — Per-metric bottom sheet

Tapping the info icon opens a `ModalBottomSheet` (using `ExperimentalMaterial3Api`) for that specific metric. The sheet:

- Shows the metric's display name as a title.
- Shows the rating table for that metric.
- For sex-specific metrics (e.g. body fat percentage): shows only the table for the user's biological sex, as stored in the user's profile. The user's sex is fetched from `ProfileRepository`; it is available at the time the measurements screen is loaded.
- For sex-neutral metrics: shows the single table.
- Each row contains: the level name (coloured to match the existing colour for that rating's `RatingSeverity`), the numeric range, and the meaning text from `HEALTH_RATINGS.md`.
- The row matching the user's current computed rating is visually highlighted (bold text).
- Dismissed by swiping down or tapping the scrim.

### FR-3 — No navigation from bottom sheet to full-screen page

The bottom sheet contains no button or link to the All Ratings screen. The two entry points are independent.

### FR-4 — "Health rating guide" overflow menu item

A new `DropdownMenuItem` labelled (string TBD, e.g. `menu_health_rating_guide`) is added to the `DropdownMenu` in `MainScreenScaffold.kt`, alongside the existing "Settings", "About", and "Measurement guidance" items. Tapping it navigates to the All Ratings screen.

### FR-5 — All Ratings screen structure

The All Ratings screen follows the same structural pattern as the existing Measurement Guidance screen: `Scaffold` + `TopAppBar` with a back-navigation icon + scrollable content. A new route is registered in `BodyTrackerNavHost.kt`. Tapping back returns the user to the previous screen via `popBackStack()`.

### FR-6 — All Ratings screen content

The screen stacks rating tables for all metrics in sequence, with a section header per metric. For sex-specific metrics, both the male and female tables are shown, each with a clear sex label (e.g. "Male", "Female"). No row is highlighted on this screen.

### FR-7 — Colour consistency

The colour used to render each level name in the bottom sheet table must be the same colour already used for that `RatingSeverity` level in `LatestMeasurementGrid` (i.e. drawn from `MaterialTheme.colorScheme`: `tertiary` for Good, `secondary` for Fair, `error` for Poor/Severe).

---

## Non-Functional Requirements

- **NFR-1**: All user-facing strings (menu item label, sheet title, screen title, sex labels, empty-state text) are defined in `strings.xml`, not hard-coded in composables.
- **NFR-2**: No new network calls or database queries are introduced. Rating table data and meaning texts are static (hard-coded or loaded from constants).
- **NFR-3**: The screen and bottom sheet must display correctly in both light and dark themes using the existing Material3 theme.
- **NFR-4**: Any new ViewModel follows the `@HiltViewModel` + `StateFlow` pattern used throughout the app.

---

## Test Scenarios

| ID | Type | Scenario | Expected Result |
|----|------|----------|-----------------|
| T-1 | Compose / UI | Card with a non-null rating is displayed | Info icon is visible on the card |
| T-2 | Compose / UI | Card with a null rating is displayed | Info icon is not present |
| T-3 | Compose / UI | User taps info icon on a sex-neutral metric card | Bottom sheet opens showing one table for that metric; the user's current rating row is highlighted |
| T-4 | Compose / UI | User taps info icon on a sex-specific metric card (e.g. body fat %) | Bottom sheet shows only the table for the user's stored biological sex; no table for the other sex |
| T-5 | Compose / UI | User opens overflow menu and taps "Health rating guide" | Navigates to All Ratings screen |
| T-6 | Compose / UI | All Ratings screen is open | All metrics are listed; sex-specific metrics show both male and female tables with labels; no row is highlighted |
| T-7 | Compose / UI | User presses back from All Ratings screen | Returns to the screen the user navigated from |
| T-8 | Unit | Rating table data for a sex-specific metric is requested for a male user | Only the male table rows are returned |
| T-9 | Unit | Rating table data for a sex-neutral metric is requested | All rows for that metric are returned regardless of user sex |

---

## Out of Scope

- Rating guidance on any screen other than the Dashboard measurements grid (e.g. Detail screen, Analysis screen).
- Filtering or searching within the All Ratings screen.
- Any changes to how rating colours or severity levels are defined or themed.

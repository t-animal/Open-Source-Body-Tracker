# Feature Concept: Health Rating Tables

## Executive Summary

Users currently see a coloured text label (e.g. "Fit", "Overweight") on each metric card in the Latest Measurements grid, but have no way to understand what that label means or how it compares to the other rating levels. This feature gives users a clear, in-context reference: a table for each metric showing all rating levels with their colour, name, numeric range, and meaning. For users who want to review all metrics at once, a dedicated full-screen page is accessible from the existing overflow menu.

## The "Why"

- **User Problem**: The coloured labels on each card in `LatestMeasurementGrid` tell the user their current rating (e.g. "Fit"), but give no context — what does "Fit" mean, what are the other levels, and how far are they from the next level up or down? The data exists in `HEALTH_RATINGS.md` but is invisible inside the app.
- **Business Value**: Transparency about how the app classifies health data increases trust and encourages ongoing engagement. Users who understand their rating are more likely to log measurements consistently to track progress between levels.
- **Success Metric**: Reduction in user confusion about what the rating labels represent, measured qualitatively through feedback.

## User Journey

- **Persona**: A user who has already entered at least one measurement and is reviewing their Dashboard. They see a coloured label like "Fit" on a metric card and want to know what it means and how it compares to other levels.

**Happy Path A — Per-metric (from the card's info icon):**
1. User notices a small info icon on a metric card in `LatestMeasurementGrid`.
2. User taps the info icon.
3. A `ModalBottomSheet` slides up showing a table for that specific metric.
4. The table has one row per level: level name (in the same colour as shown on the card), numeric range, and a short meaning text. The row corresponding to the user's current rating is visually highlighted (e.g. bold text or a filled/tinted row background).
5. User reads the table and dismisses the sheet by swiping or tapping outside.

**Happy Path B — All metrics (from the overflow menu):**
1. User taps the three-dot overflow menu in `MainScreenScaffold`'s top app bar.
2. A new menu item (e.g. "Health rating guide") appears alongside the existing "Settings", "About", and "Measurement guidance" items.
3. Tapping it navigates to a new full-screen page registered in `BodyTrackerNavHost`.
4. The page stacks the rating table for every metric in sequence, using the same table layout as the per-metric bottom sheet.
5. No row is highlighted on this page because it is a reference view, not a personal status view.
6. The user navigates back via the system back gesture or a back arrow in the screen's top app bar.

## MVP Boundaries

**In-Scope:**
- Add a small info icon to each metric card in `LatestMeasurementGrid` (only on cards that have a rating). Tapping it opens a `ModalBottomSheet` for that specific metric.
- The bottom sheet shows a table: level name (coloured to match the card), numeric range, and meaning text from `HEALTH_RATINGS.md`. The user's current level row is visually highlighted.
- A new full-screen page registered in `BodyTrackerNavHost`, navigable from the `MainScreenScaffold` overflow menu. It stacks all metric tables with clear section headers, no highlighting.
- Colour usage must match what is already used to colour the rating text in `LatestMeasurementGrid` (driven by `RatingSeverity`).

**Out-of-Scope:**
- The same reference view on any other screen (future iteration).
- Filtering or searching within the all-metrics page.
- Any changes to how rating colours or severity levels are defined or themed.

---

## Relevant Files

- `app/src/main/java/de/t_animal/opensourcebodytracker/feature/measurements/components/LatestMeasurementCard.kt` — contains `LatestMeasurementGrid`, where the info icon and bottom sheet trigger will live
- `app/src/main/java/de/t_animal/opensourcebodytracker/ui/components/MainScreenScaffold.kt` — overflow menu where the new "Health rating guide" item is added
- `app/src/main/java/de/t_animal/opensourcebodytracker/ui/navigation/BodyTrackerNavHost.kt` — where the new full-screen route is registered
- `documents/HEALTH_RATINGS.md` — source of rating levels, ranges, and meaning texts

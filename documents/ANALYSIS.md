# Phase 4.3 – Analysis Screen (Implemented)

This document describes the currently implemented Analysis tab behavior.
It reflects the code in `feature/analysis/*` and navigation wiring in `BodyTrackerNavHost`.

---

## Purpose

The Analysis tab visualizes historical values for raw measurements and derived metrics over selectable time ranges.

---

## Screen Composition

The Analysis route is hosted inside the shared main scaffold (`MainScreenScaffold`), which already provides:

- top app bar
- overflow menu
- bottom navigation

`AnalysisScreen` renders only its content area:

1. duration segmented control
2. metric chart cards in a `LazyColumn`

---

## Duration Selector

Available durations:

- `1M`
- `3M` (default)
- `6M`
- `1Y`
- `All`

Behavior:

- single selection only
- changing duration recalculates all chart datasets
- filtering window is `[now - duration, now]`
- `All` includes entries up to `now`

---

## Metrics Rendered

The screen currently renders 17 metric cards, each with one line chart (or empty state):

- Weight
- Neck
- Chest
- Waist
- Abdomen
- Hip
- Chest Skinfold
- Abdomen Skinfold
- Thigh Skinfold
- Triceps Skinfold
- Suprailiac Skinfold
- BMI
- Navy Body Fat %
- Skinfold Body Fat %
- Waist–Hip Ratio
- Waist–Height Ratio
- Hip–Height Ratio

Metric definitions are centralized in `AnalysisModels.kt`.

---

## Data Flow

`AnalysisViewModel` combines:

- all measurements (`MeasurementRepository.observeAll()`)
- current profile (`ProfileRepository.profileFlow`)
- selected duration (`MutableStateFlow`)

For each measurement, derived metrics are computed with `CalculateMeasurementDerivedMetricsUseCase`.
Then `AnalysisTransform`:

1. filters by selected duration
2. sorts points chronologically
3. builds one chart model per metric
4. computes Y-axis range with padding

---

## Charting Implementation

Library: **Vico 3.0.1** (`compose-android`, `compose-m3-android`)

Current chart behavior:

- line chart per metric
- tap marker toggle
- horizontal scroll/pan enabled
- pinch zoom enabled
- epoch millis as X values
- duration-aware X-axis labels (`dd.MM` for short windows, `MM.yy` for `1Y`/`All`)

Y-axis behavior:

- auto range based on visible points
- +5% padding above and below
- flat series fallback padding: `max(abs(value) * 0.05, 0.5)`

---

## Empty State

If a metric has no datapoints in the selected duration, the metric card is shown with:

`no data yet`

---

## Validation and Tests

`AnalysisTransformTest` currently verifies:

- duration filtering
- chart count == metric definition count
- y-axis padding behavior (flat + non-flat)
- chronological point ordering

---

## Non-Implemented Items

Not implemented in Phase 4.3:

- moving averages / trend overlays
- metric grouping or collapsing
- per-metric custom interaction behavior

These can be added incrementally without changing the existing ViewModel/transform separation.

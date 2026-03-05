# AI Development Handoff (Phase 1-5.4)

This document describes the current implementation state and the key constraints/gotchas so another coding agent can continue work quickly.

## Project Goal (README phases through 5.4)
Implemented:
- Phase 1: profile onboarding (DataStore) + measurement CRUD (Room) with Compose UI + MVVM.
- Phase 2: derived metrics in the measurement list via a domain calculation layer.
- Phase 3: skinfold-based derived body-fat support in add/edit and domain metrics.
- Phase 4.1 (navigation shell): top app bar + overflow menu + bottom navigation structure with placeholder Analysis/Photos/Settings screens.
- Phase 4.2 (measurements redesign): latest measurement card + preview table (20) + full-list "More" screen.
- Phase 4.3 (analysis charts): fully implemented Analysis screen with duration filtering and Vico line charts for raw + derived metrics.
- Phase 4.4 (settings integration): implemented Settings screen + persisted configuration + settings-driven visibility/input behavior across Analysis and Measurements.
- Phase 5.1 (photo capture + storage): camera capture in add/edit with deferred persistence on Save and private internal app storage.
- Phase 5.2 (photo gallery): photos tab shows newest→oldest thumbnail feed for measurements with photos.
- Phase 5.3 (compare mode): 2-photo selection mode and compare slider screen.
- Phase 5.4 (animate mode): multi-select animation mode with looping playback screen.

## Status Summary
**Build:** `assembleDebug` succeeds.
**Tests:** `test` succeeds.
**Lint:** `ktlintCheck` succeeds.

**Phase 4.4 feature coverage (implemented):**
- Settings persistence via DataStore (`SettingsRepository` / `PreferencesSettingsRepository`).
- Settings UI sections implemented:
  - Analysis Methods (`BMI`, `Navy Body Fat %`, `Skinfold Body Fat %`)
  - Measurement Collection (required measurements locked)
  - Display Configuration (placement: `Hidden`, `Only in Table`, `Only in Analysis`, `In both`)
- Shared metric registry split into:
  - `MeasuredBodyMetric`
  - `DerivedBodyMetric`
  - unified `BodyMetric.entries`
- Settings-driven visibility is applied to:
  - Analysis chart cards
  - Latest measurement card
  - Table preview and full-list table
  - Add/Edit measurement input fields
- Profile save interaction:
  - when sex changes and profile is saved, newly required measurements for enabled analyses are auto-enabled additively (never auto-disabled).

**Phase 1 feature coverage:**
- Profile gate on startup (if profile missing → onboarding).
- Profile persistence via Preferences DataStore: sex, date of birth, height.
- Measurement persistence via Room: list + add/edit.
- Validation rules:
  - Profile: sex required; DOB must be a valid ISO `LocalDate` string (`YYYY-MM-DD` in state/storage); height > 0.
  - Measurement: at least one numeric value required.

**Input UX (Phase 1 polish):**
- Profile + Measurement edit no longer allow arbitrary text input for dates; they use a Material3 date picker.
- Date text is displayed in the user’s locale using a numeric full-year format (e.g. `31.12.2026` / `12/31/2026`).
- Decimal inputs (height, weight, circumferences) use a decimal keyboard and normalize input to the current locale’s decimal separator.
- Date picker selection is converted UTC↔local-midnight to avoid off-by-one-day errors in some time zones.
- Measurement list shows dates (and displayed decimals like weight) using locale-aware numeric formatting.

**UI screens present (Compose):**
- Onboarding/Profile setup screen
- Profile screen (editable)
- Measurement list screen
- Add/Edit measurement screen
- Settings screen (implemented)
- Analysis screen (implemented charts)
- Photos screen (gallery + compare + animate mode)

All screens have Compose previews.

**Phase 2 feature coverage (implemented):**
- Domain-derived metrics are calculated dynamically from profile + measurement input.
- Metrics currently shown on measurement list rows:
  - BMI
  - Estimated body-fat percentage (Navy method)
  - WHR
  - WHtR
  - Hip/Height ratio
- Missing/invalid inputs do not show placeholder text; unavailable metrics are hidden.
- Derived values are not persisted in Room (computed at runtime in domain layer).
- Existing locale-aware numeric/date display behavior is retained.

**Phase 3 feature coverage (implemented):**
- Add/Edit measurement now supports sex-specific 3-site skinfold inputs:
  - Male: chest, abdomen, thigh
  - Female: triceps, suprailiac, thigh
- Derived metrics now expose two separate body-fat outputs:
  - Navy body-fat %
  - Skinfold body-fat % (Jackson & Pollock 3-site + Siri conversion)
- Skinfold output is exposed as a dedicated 3-site metric field (`skinfold3SiteBodyFatPercent`).
- Skinfold calculations use age at measurement date (derived from profile DOB + measurement date).
- Minimal validation for skinfold inputs is enforced in the edit flow (> 0 for provided values).
- Room schema version was intentionally not bumped in this phase.

**Phase 4.1 navigation shell coverage (implemented):**
- Main screens now share a common scaffold structure:
  - top app bar with dynamic title
  - overflow menu (Profile, Settings)
  - bottom navigation tabs (M/A/P)
- Added placeholder screens:
  - Analysis ("Analysis Coming Soon")
  - Photos ("Photos Coming Soon")
  - Settings ("Settings Coming Soon")
- Added dedicated route for Profile (editable profile form).
- Measurement list was refactored to render inside shared scaffold content; FAB was extracted as reusable composable.
- Onboarding gate behavior was tightened to auto-navigate to the list only when currently on onboarding.

**Phase 4.2 measurements screen redesign coverage (implemented):**
- Measurements main tab now contains:
  - "Latest Measurement" card
  - 2-column metrics grid with strong value emphasis
  - "All Measurements" table preview (first 20 newest entries)
  - conditional "More" button when additional entries exist
- Added explicit placeholder rendering (`--`) for missing raw/derived values in the latest card and table cells.
- Added dedicated full-list route/screen opened via "More":
  - top app bar with back button
  - full measurements table
  - no bottom nav and no FAB
- Main Measurements tab still keeps the Add FAB and supports tap-to-edit from preview table rows.

**Phase 4.3 analysis chart coverage (implemented):**
- Analysis tab now renders one chart card per metric using Vico (`compose-android`, `compose-m3-android`).
- Duration selector is available at top of content:
  - `1M`, `3M` (default), `6M`, `1Y`, `All`.
- Data pipeline:
  - ViewModel combines measurement flow + profile flow + selected duration.
  - Derived metrics are computed once per measurement in the ViewModel layer.
  - Duration filtering is done in transform layer using `[now - duration, now]`; `All` includes data up to `now`.
- Chart behavior:
  - X-axis uses epoch millis and duration-dependent date labels.
  - Y-axis range uses dynamic 5% padding (`0.5` minimum padding for flat series).
  - Tap toggles marker visibility on data points.
  - Zoom + pan are enabled.
- Empty metric data in selected duration renders card message: `no data yet`.
- Covered by unit tests in `feature/analysis/AnalysisTransformTest.kt`.

**Phase 5 photo feature coverage (implemented):**
- Add/Edit measurement includes camera capture, preview, delete-before-save behavior, and save-time persistence only.
- Photos tab supports compare mode and animate mode with explicit mode entry/exit behavior.
- Compare mode enforces a max selection of 2 and opens the compare slider route.
- Animate mode supports multi-select, requires at least 2 photos to play, and opens a dedicated animation route.
- Animation playback loops at fixed `ANIMATION_FRAME_RATE = 5` and preserves image aspect ratio.

## How to Build / Lint
This environment requires sourcing `/etc/profile` before running Gradle:

```bash
source /etc/profile && ./gradlew assembleDebug --console=plain
source /etc/profile && ./gradlew test --console=plain
source /etc/profile && ./gradlew ktlintCheck --console=plain
```

If root `ktlintCheck` is unavailable in a given environment setup, use:

```bash
source /etc/profile && ./gradlew :app:ktlintCheck --console=plain
```

## Toolchain Constraints / Gotchas
### AGP 9 “built-in Kotlin”
- The project uses AGP 9.x which can run with built-in Kotlin.
- Do **not** assume `org.jetbrains.kotlin.android` is applied in the app module.
- Root `ktlint` config applies the ktlint plugin when Android plugins are present (`com.android.application` / `com.android.library`) to keep ktlint tasks available.

### KSP + Room
- Room was upgraded to a Kotlin 2.x/KSP2 compatible version.
- `ksp { arg("room.generateKotlin", "true") }` is set in the app module to force Kotlin code generation (avoids javac signature issues seen with older Room versions).
- `android.disallowKotlinSourceSets=false` is set in `gradle.properties` (experimental) to allow KSP-generated sources to be registered under the current setup.

## Code Map (What’s Where)
### App entry + DI
- `de.t_animal.opensourcebodytracker.BodyTrackerApplication`
  - Creates an `AppContainer` instance.
- `de.t_animal.opensourcebodytracker.AppContainer`
  - Simple manual DI:
    - `PreferencesProfileRepository` (DataStore)
    - `PreferencesSettingsRepository` (DataStore)
    - `AppDatabase` + `RoomMeasurementRepository` (Room)
    - `DerivedMetricsCalculator`
    - `CalculateMeasurementDerivedMetricsUseCase`
- `de.t_animal.opensourcebodytracker.MainActivity`
  - Hosts Compose content + navigation.

### Navigation
- `de.t_animal.opensourcebodytracker.ui.navigation.Routes`
  - Route constants and helper for edit route.
  - Includes onboarding, measurement list/add/edit/full-list, profile, settings, analysis, photos, compare, and animation routes.
- `de.t_animal.opensourcebodytracker.ui.navigation.MainDestination`
  - Main-tab model for Measurements/Analysis/Photos.
- `de.t_animal.opensourcebodytracker.ui.navigation.MainScreenScaffold`
  - Shared main-screen scaffold (top bar, overflow menu, bottom nav, optional FAB slot).
- `de.t_animal.opensourcebodytracker.ui.navigation.BodyTrackerNavHost`
  - NavHost for onboarding/profile/settings/measurements/analysis/photos/add/edit/full-list/compare/animation.
  - Gating logic: starts on onboarding; when profile becomes non-null **and current route is onboarding**, navigates to measurement list and pops onboarding.
  - Wires shared scaffold to main tabs and keeps add/edit flows separate.

### Profile (DataStore)
- Model:
  - `core/model/Sex`, `core/model/UserProfile`
- Data:
  - `data/profile/ProfileRepository`
  - `data/profile/PreferencesProfileRepository`
- Feature UI:
  - `feature/profile/ProfileMode` (Onboarding vs Settings)
  - `feature/profile/ProfileViewModel`
  - `feature/profile/ProfileScreen`
- Interaction note:
  - `ProfileViewModel.onSaveClicked()` now also syncs settings by enabling newly required measurements according to `DerivedMetricsDependencyResolver`.

### Settings (DataStore + UI)
- Model:
  - `core/model/SettingsState`
  - `core/model/AnalysisMethod`
  - `core/model/MetricRegistry` (`BodyMetric`, `MeasuredBodyMetric`, `DerivedBodyMetric`)
- Data:
  - `data/settings/SettingsRepository`
  - `data/settings/PreferencesSettingsRepository`
- Domain:
  - `domain/metrics/DerivedMetricsDependencyResolver`
  - `SettingsState.enabledAnalysisMethods()`
  - shared visibility helpers:
    - `SettingsState.visibleInAnalysisOrdered()`
    - `SettingsState.visibleInTableOrdered()`
- Feature UI:
  - `feature/settings/SettingsViewModel`
  - `feature/settings/SettingsScreen`

### Shared UI components
- `ui/components/InputFields.kt`
  - `DateInputField`: read-only text field + date picker dialog, tap-anywhere opens picker, locale-numeric display, UTC/local conversion.
  - `DecimalNumberInputField`: decimal keyboard + locale-aware input normalization.

### Measurements (Room)
- Model:
  - `core/model/BodyMeasurement`
  - `core/model/DerivedMetrics`
- Data:
  - `data/measurements/MeasurementEntity`
  - `data/measurements/MeasurementDao`
  - `data/measurements/AppDatabase`
  - `data/measurements/MeasurementRepository`
  - `data/measurements/RoomMeasurementRepository`
- Domain:
  - `domain/metrics/DerivedMetricsCalculator`
  - `domain/metrics/CalculateMeasurementDerivedMetricsUseCase`
- Feature UI:
  - `feature/measurements/MeasurementListViewModel` + `MeasurementListScreen`
    - List state combines profile + measurements and exposes per-row derived metrics.
    - Also combines settings and filters visible table/latest-card metrics.
    - Rendered as scaffold content (no local top app bar); `MeasurementListAddButton` hosts FAB UI.
  - `feature/measurements/MeasurementEditViewModel` + `MeasurementEditScreen`
    - Field visibility is settings-driven.
    - Disabled metrics are persisted as `null` on save.

### Analysis feature map (Phase 4.3)
- `feature/analysis/AnalysisRoute` + `AnalysisScreen`
  - Hosts duration selector and chart cards.
- `feature/analysis/AnalysisViewModel`
  - Owns selected duration state and builds `AnalysisUiState`.
  - Combines settings and applies `visibleInAnalysis` filtering via shared helper.
- `feature/analysis/AnalysisTransform`
  - Duration filtering, metric chart mapping, y-range calculation.
- `feature/analysis/AnalysisModels`
  - Definitions for durations, metric catalog, chart point/range/state models.

### Remaining placeholders
- No major placeholder screen remains in the primary product flow.

### Tests
- `app/src/test/java/de/t_animal/opensourcebodytracker/domain/metrics/DerivedMetricsCalculatorTest.kt`
  - Covers BMI/ratio calculations, separate navy/skinfold body-fat paths, sex-specific 3-site requirements, age-at-measurement constraints, and invalid navy log-input handling.
- `app/src/test/java/de/t_animal/opensourcebodytracker/domain/metrics/DerivedMetricsDependencyResolverTest.kt`
  - Covers method→required-measurement mapping for BMI/Navy/Skinfold and sex-specific requirements.

### Theme
- `ui/theme/Theme`

## Data Model Notes
- Dates are stored as **epoch millis**.
- `UserProfile` contains (sex, DOB epoch millis, height cm).
- `BodyMeasurement` contains:
  - date epoch millis
  - optional numeric metrics (weight/circumferences)
- `DerivedMetrics` contains nullable dynamic outputs:
  - BMI
  - navy body-fat percentage
  - skinfold 3-site body-fat percentage
  - WHR
  - WHtR
  - hip-height ratio

## Known Warnings
- Gradle warning about `android.disallowKotlinSourceSets=false` being experimental.
- Packaging warning: some native libs can’t be stripped (does not fail the build).

## Recommended Next Steps
1. Analysis UX polish:
  - Move chart/metric labels to string resources for EN/DE consistency.
  - Add optional marker label formatting per metric type if needed.
2. Navigation polish:
  - Decide and document explicit tab back-stack behavior (currently single-stack + `launchSingleTop`).
3. Testing:
  - Add ViewModel tests for duration switching and filtered chart content.
  - Add UI tests for empty/non-empty chart card rendering and duration button state.
4. Phase 3/4 metric polish:
  - Review ratio/percent precision rules for display consistency across list and charts.
5. Future analysis extensions:
  - Optional trend line (WMA) and metric grouping/folding if card list grows too long.

## Quick Sanity Checklist for Future Agents
- `source /etc/profile && ./gradlew assembleDebug --console=plain` passes.
- `source /etc/profile && ./gradlew test --console=plain` passes.
- `source /etc/profile && ./gradlew ktlintCheck --console=plain` passes.
- On fresh install: app shows onboarding until profile saved.
- After profile save: Measurements tab shows; FAB opens add; tap row opens edit.
- Measurements tab shows Latest card + preview table; More opens full-list screen with back button only.
- Bottom nav switches between Measurements/Analysis/Photos and updates title.
- Overflow on main tabs opens Profile and Settings screens.
- Photos tab renders captured photos in newest→oldest order.
- Compare mode allows max 2 selections and opens compare slider screen.
- Animate mode requires at least 2 selections and opens looping playback screen.
- Missing raw/derived values in latest card/table render as `--`.
- Settings changes immediately affect:
  - Analysis chart list
  - Latest card/table columns
  - Add/Edit input field visibility
- Changing sex in Profile and saving auto-enables newly required measurements for active analysis methods.
- Analysis tab shows duration segmented controls (`1M/3M/6M/1Y/All`).
- Analysis cards render Vico line charts for populated metrics and `no data yet` when empty.
- Analysis charts support tap marker toggle, pan, and pinch zoom.

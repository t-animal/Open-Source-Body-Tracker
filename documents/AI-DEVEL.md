# AI Development Handoff (Phase 1-4.1)

This document describes the current implementation state and the key constraints/gotchas so another coding agent can continue work quickly.

## Project Goal (README phases through 4.1)
Implemented:
- Phase 1: profile onboarding (DataStore) + measurement CRUD (Room) with Compose UI + MVVM.
- Phase 2: derived metrics in the measurement list via a domain calculation layer.
- Phase 3: skinfold-based derived body-fat support in add/edit and domain metrics.
- Phase 4.1 (navigation shell): top app bar + overflow menu + bottom navigation structure with placeholder Analysis/Photos/Settings screens.

## Status Summary
**Build:** `assembleDebug` succeeds.
**Tests:** `test` succeeds.
**Lint:** `ktlintCheck` succeeds.

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
- Settings screen (placeholder)
- Analysis screen (placeholder)
- Photos screen (placeholder)

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
    - `AppDatabase` + `RoomMeasurementRepository` (Room)
    - `DerivedMetricsCalculator`
    - `CalculateMeasurementDerivedMetricsUseCase`
- `de.t_animal.opensourcebodytracker.MainActivity`
  - Hosts Compose content + navigation.

### Navigation
- `de.t_animal.opensourcebodytracker.ui.navigation.Routes`
  - Route constants and helper for edit route.
  - Includes onboarding, measurement list/add/edit, profile, settings, analysis, photos.
- `de.t_animal.opensourcebodytracker.ui.navigation.MainDestination`
  - Main-tab model for Measurements/Analysis/Photos.
- `de.t_animal.opensourcebodytracker.ui.navigation.MainScreenScaffold`
  - Shared main-screen scaffold (top bar, overflow menu, bottom nav, optional FAB slot).
- `de.t_animal.opensourcebodytracker.ui.navigation.BodyTrackerNavHost`
  - NavHost for onboarding/profile/settings/measurements/analysis/photos/add/edit.
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
    - Rendered as scaffold content (no local top app bar); `MeasurementListAddButton` hosts FAB UI.
  - `feature/measurements/MeasurementEditViewModel` + `MeasurementEditScreen`

### New placeholder features
- `feature/analysis/AnalysisScreen`
- `feature/photos/PhotosScreen`
- `feature/settings/SettingsScreen`

### Tests
- `app/src/test/java/de/t_animal/opensourcebodytracker/domain/metrics/DerivedMetricsCalculatorTest.kt`
  - Covers BMI/ratio calculations, separate navy/skinfold body-fat paths, sex-specific 3-site requirements, age-at-measurement constraints, and invalid navy log-input handling.

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
1. Navigation polish:
   - Move new top-bar/overflow/bottom-nav labels to string resources for EN/DE consistency.
   - Decide final behavior for tab back stack (currently simple single-stack behavior).
2. UX consistency:
   - Confirm Profile/Settings app bar behavior (e.g., optional back affordance/title consistency).
   - Verify desired system bar styling behavior across gesture and 3-button navigation modes.
3. Testing:
   - Add unit tests for parsing/validation logic (profile + measurement).
   - Add basic repository tests where feasible.
4. Phase 2 completion/polish:
   - Optionally move new list labels (BMI/WHR/WHtR/etc.) to string resources for i18n consistency.
5. Testing:
   - Add targeted tests for metric formatting/presentation mapping in list UI state.
   - Add additional edge-case tests (zero/negative values across all metrics).
6. Phase 3 follow-up:
   - Optionally localize new Phase 3 labels/errors to string resources for EN/DE consistency.
   - Consider range warnings (2–50mm/site) as non-blocking UX feedback.
   - If existing local installs must be supported, add a proper Room migration and bump DB version.

## Quick Sanity Checklist for Future Agents
- `source /etc/profile && ./gradlew assembleDebug --console=plain` passes.
- `source /etc/profile && ./gradlew test --console=plain` passes.
- `source /etc/profile && ./gradlew ktlintCheck --console=plain` passes.
- On fresh install: app shows onboarding until profile saved.
- After profile save: Measurements tab shows; FAB opens add; tap row opens edit.
- Bottom nav switches between Measurements/Analysis/Photos and updates title.
- Overflow on main tabs opens Profile and Settings screens.
- Measurement list row shows only computable derived metrics (no placeholder for unavailable ones).

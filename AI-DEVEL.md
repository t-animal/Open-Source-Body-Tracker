# AI Development Handoff (Phase 1)

This document describes the current implementation state and the key constraints/gotchas so another coding agent can continue work quickly.

## Project Goal (README Phase 1)
Implemented Phase 1: profile onboarding (DataStore) + measurement CRUD (Room) with Compose UI + MVVM.

## Status Summary
**Build:** `assembleDebug` succeeds.

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
- Settings/Profile screen
- Measurement list screen
- Add/Edit measurement screen

All screens have Compose previews.

## How to Build / Lint
This environment requires sourcing `/etc/profile` before running Gradle:

```bash
source /etc/profile && ./gradlew assembleDebug --console=plain
source /etc/profile && ./gradlew ktlintCheck --console=plain
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
- `de.t_animal.opensourcebodytracker.MainActivity`
  - Hosts Compose content + navigation.

### Navigation
- `de.t_animal.opensourcebodytracker.ui.navigation.Routes`
  - Route constants and helper for edit route.
- `de.t_animal.opensourcebodytracker.ui.navigation.BodyTrackerNavHost`
  - NavHost for onboarding/settings/list/add/edit.
  - Gating logic: starts on onboarding; when profile becomes non-null, navigates to measurement list and pops onboarding.

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
- Data:
  - `data/measurements/MeasurementEntity`
  - `data/measurements/MeasurementDao`
  - `data/measurements/AppDatabase`
  - `data/measurements/MeasurementRepository`
  - `data/measurements/RoomMeasurementRepository`
- Feature UI:
  - `feature/measurements/MeasurementListViewModel` + `MeasurementListScreen`
  - `feature/measurements/MeasurementEditViewModel` + `MeasurementEditScreen`

### Theme
- `ui/theme/Theme`

## Data Model Notes
- Dates are stored as **epoch millis**.
- `UserProfile` contains (sex, DOB epoch millis, height cm).
- `BodyMeasurement` contains:
  - date epoch millis
  - optional numeric metrics (weight/circumferences)

## Known Warnings
- Gradle warning about `android.disallowKotlinSourceSets=false` being experimental.
- Packaging warning: some native libs can’t be stripped (does not fail the build).

## Recommended Next Steps
1. Phase 1 polish (if desired):
   - Ensure navigation UX is exactly as intended (e.g., Settings returns to list).
   - Consider adding small unit tests for the date/number parsing helpers (locale separators, date conversions).
2. Add derived metrics (Phase 2):
   - Add calculation layer using profile + measurement.
   - Display derived values in list/details.
3. Testing:
   - Add unit tests for parsing/validation logic (profile + measurement).
   - Add basic repository tests where feasible.

## Quick Sanity Checklist for Future Agents
- `./gradlew assembleDebug` passes.
- `./gradlew ktlintCheck` passes.
- On fresh install: app shows onboarding until profile saved.
- After profile save: measurement list shows; FAB opens add; tap row opens edit.

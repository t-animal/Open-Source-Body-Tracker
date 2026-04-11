# Feature Specification: Measurement Guidance

## Scope

This feature adds contextual measurement guidance across three surfaces:

1. **Add Measurement screen** — trailing info icon on each input field (shown when field is focused); tapping shows a measurement-specific popup
2. **Settings screen** — always-visible info icon per measurement row; tapping shows the same popup
3. **Help screen** — "How to measure" entry in overflow menu; full navigation destination with accordion listing all measurements

---

## Popup: Add Measurement Screen

### Functional Requirements

- **FR-1**: On the Add Measurement screen, a help/info icon (`IconButton`) is displayed as the `trailingIcon` of the `OutlinedTextField` for each measurement input row, visible when the field has focus.
- **FR-2**: The icon is rendered for all 12 `MeasuredBodyMetric` values: `Weight`, `BodyFat`, `NeckCircumference`, `ChestCircumference`, `WaistCircumference`, `AbdomenCircumference`, `HipCircumference`, `ChestSkinfold`, `AbdomenSkinfold`, `ThighSkinfold`, `TricepsSkinfold`, `SuprailiacSkinfold`.
- **FR-3**: Tapping the icon presents a Compose `Dialog` (or `Popup`) showing guidance text for the relevant `MeasurementType`. Only one popup can be visible at a time.
- **FR-4**: The popup is dismissed by tapping anywhere outside it (`onDismissRequest`). No explicit close button is required.
- **FR-5**: After dismissal, the numeric input field that was focused before the popup appeared retains focus. Any typed value is unchanged.
- **FR-6**: Popup visibility is tracked in local Compose state (`remember { mutableStateOf(...) }`). No state is added to `AddMeasurementViewModel`.

### Popup: Settings Screen

- **FR-7**: On the Settings screen, each measurement row (name + toggle Switch) displays a permanently visible info `IconButton`.
- **FR-8**: Tapping the icon presents the same measurement-specific popup as on the Add Measurement screen (same content, same dismiss behaviour).
- **FR-9**: Popup visibility is tracked in local Compose state in the Settings screen composable. No state is added to `SettingsViewModel`.

---

## Help Screen

- **FR-10**: A "How to measure" menu item is added to the overflow menu on the Measurements screen.
- **FR-11**: Tapping "How to measure" navigates to a new full-screen destination pushed onto the back stack via `NavController`. Route constant: `"measurement_guidance"`, defined as a `const val` in the relevant screen or navigation object.
- **FR-12**: The Help screen has a top app bar with a back button that pops the back stack (using secondary screen scaffold).
- **FR-13**: The Help screen lists all 12 `MeasuredBodyMetric` values as accordion items, in the same order as defined in the enum.
- **FR-14**: Each accordion item is collapsed by default, showing only the measurement name.
- **FR-15**: Tapping an accordion item expands it to reveal the guidance text (same string resource as the popup). Multiple items may be expanded simultaneously; tapping an expanded item collapses it.
- **FR-16**: The Help screen has no ViewModel. All content is resolved from string resources at composition time.

---

## Content / String Resources

- **FR-17**: Guidance text is stored as Android string resources with HTML bold markup (`<b>...</b>`). Each of the 12 `MeasuredBodyMetric` values maps to one dedicated string resource (e.g., `measurement_guidance_weight`, `measurement_guidance_body_fat`, `measurement_guidance_neck_circumference`, etc.).
- **FR-18**: The same string resource is used in both the popup and the accordion Help screen for any given `MeasurementType`.
- **FR-19**: All 12 guidance strings are placeholder text at first delivery. Content will be replaced in a follow-up content pass.
- **FR-20**: Guidance strings are rendered using `HtmlCompat.fromHtml()` converted to `AnnotatedString`, so `<b>` tags render as bold within Compose `Text`. (`HtmlCompat` is available via `androidx.core:core-ktx`, already in the project.)
- **FR-21**: All guidance strings must reside in `res/values/strings.xml` and any future locale-specific equivalents. No guidance text is hardcoded in Kotlin/Compose source.

---

## Non-Functional Requirements

- **NFR-1**: Popups and accordion expansion must appear within one frame of interaction (no async loading).
- **NFR-2**: Neither the popup nor the Help screen may modify any ViewModel state, SavedStateHandle, or navigation back-stack entry beyond their own navigation (FR-11).
- **NFR-3**: All info icons must meet the Material Design minimum touch-target size (48dp × 48dp).
- **NFR-4**: All info icons must have a content description string resource for accessibility (e.g., "Show measurement guidance").
- **NFR-5 (minSdk note)**: The exact minSdk integer is applied via the convention plugin and was not resolved during elicitation. The implementer must confirm the minSdk before choosing `HtmlCompat` flags — on API 24+ use `HtmlCompat.FROM_HTML_MODE_LEGACY`.

---

## Lifecycle / Configuration Behaviour

- Popup visibility is held in `remember { mutableStateOf(false) }` (not `rememberSaveable`), so rotating the device while a popup is open closes it. The underlying input field value is preserved by the existing ViewModel.
- The Help screen is a stateless composable; no lifecycle concerns apply beyond standard Compose navigation.

---

## Test Scenarios

No tests to be added.

---

## Source Files Verified During Elicitation

- `app/src/main/java/de/t_animal/opensourcebodytracker/core/model/MetricRegistry.kt` — confirmed `MeasuredBodyMetric` enum with 12 values and `DerivedBodyMetric` (derived metrics are out of scope for guidance)
- `app/src/main/java/de/t_animal/opensourcebodytracker/core/model/BodyMeasurement.kt` — confirmed data class fields matching the 12 measured metrics
- `app/src/main/java/de/t_animal/opensourcebodytracker/feature/measurements/components/MetricInputField.kt` — confirmed measurement input field component
- `app/src/main/java/de/t_animal/opensourcebodytracker/ui/navigation/MainDestination.kt` — confirmed navigation destination pattern
- `app/src/main/java/de/t_animal/opensourcebodytracker/feature/settings/components/SettingsSections.kt` — confirmed Settings screen structure
- `app/src/main/res/values/strings.xml` — existing label strings present; guidance strings do not yet exist and must be added

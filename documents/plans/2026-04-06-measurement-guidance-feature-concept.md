# Feature Concept: Measurement Guidance System

## Executive Summary

Users currently have no in-app guidance on how to take body measurements correctly, leading to inconsistent data over time or even worse - incorrect analysis results. This feature adds contextual help at the point of input — an info icon on each measurement field that surfaces a measurement-specific popup — and a dedicated Help screen in the Measurements tab listing all measurements with instructions in an accordion layout.

## The "Why"

- **User Problem**: A user logging "Waist" for the first time has no way to know whether to measure at the navel, above the hip bone, or at the narrowest point. Without consistent technique, their data becomes unreliable and the app loses its core value of tracking progress meaningfully.
- **Business Value**: Reducing measurement ambiguity increases the accuracy and consistency of logged data, which directly improves chart and history value — the features that drive long-term retention. It also lowers the barrier to entry for new users unfamiliar with body measurement conventions.
- **Success Metric**: Reduction in single-session drop-off on the Measurements tab (proxy: users who open the tab and do not save a measurement). Secondary metric: increased number of distinct measurement types filled in per session, indicating users feel confident enough to log measurements they previously skipped.

## User Journey

- **Persona**: A new or returning user who is setting up their tracked measurements or logging a measurement type they have not used before. They are likely adding data at home or after a workout, using the app one-handed in a brief session.

- **The "Happy Path" — Contextual Info Popup (Measurements Screen)**:
  1. User opens the Measurements tab and sees their enabled `NumberField` input fields (one per enabled `MeasurementType`).
  2. User taps into a field (e.g., Waist). The field gains focus and an info icon (question mark overlay) becomes visible on the field.
  3. User taps the info icon. A modal popup appears with a placeholder title and placeholder descriptive text specific to the Waist measurement.
  4. User taps anywhere on screen. The popup dismisses and the field still has focus. The user continues entering their value.

- **The "Happy Path" — Contextual Info Icon (Settings Screen)**:
  1. User opens the Settings screen from the Measurements tab.
  2. Each measurement row (name + toggle switch) displays an info icon.
  3. User taps the info icon for a measurement. The same measurement-specific popup appears with placeholder content.
  4. User taps anywhere. Popup dismisses. User continues toggling measurements.

- **The "Happy Path" — Help Screen**:
  1. User taps a new "How to measure" entry in the overflow menu.
  2. A Help screen opens showing all 12 `MeasuredBodyMetric` values (`Weight`, `BodyFat`, `NeckCircumference`, `ChestCircumference`, `WaistCircumference`, `AbdomenCircumference`, `HipCircumference`, `ChestSkinfold`, `AbdomenSkinfold`, `ThighSkinfold`, `TricepsSkinfold`, `SuprailiacSkinfold`) in an accordion list.
  3. Each accordion item is collapsed by default, showing only the measurement name.
  4. User taps an item to expand it, revealing placeholder content (text and an image placeholder) describing how to take that measurement. The most important parts of the text are highlighted in bold.
  5. User taps the expanded item again to collapse it, or taps another item to expand that one.

## MVP Boundaries

- **In-Scope**:
  - Info icon overlaid on each `NumberField`, visible when the field has focus (Measurements screen).
  - Info icon displayed inline on each measurement row (Settings screen), always visible (not focus-dependent, since there is no input focus concept on toggle rows).
  - Tap-to-dismiss modal popup triggered by either info icon, showing measurement-specific placeholder content (title + body text placeholder, image placeholder). The most important parts of the explanatory text (e.g., exact measurement location, posture requirement) are highlighted in bold.
  - Popup applies to all 12 `MeasuredBodyMetric` values from day one.
  - New Help screen accessible from the Measurements tab, listing all 12 `MeasuredBodyMetric` values in an accordion layout with placeholder content.
  - All content (text descriptions, measurement images) is placeholder and intended to be replaced in a follow-up.

- **Out-of-Scope**:
  - Real instructional copy or photography (deferred to a content pass).
  - Video or animated guidance.
  - Localization of help content beyond what already exists (placeholder strings only for now).
  - Filtering or searching the Help screen.
  - Personalizing help content based on user skill level or history.
  - Any changes to how measurements are saved or validated.

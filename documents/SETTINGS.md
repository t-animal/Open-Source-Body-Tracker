# Settings Configuration – Metrics & Analysis Control

This document defines the structure and behavior of the **Settings** screen related to:

1. Configuring which **analyses** are enabled
2. Configuring which **measurements are collected**
3. Configuring which values are **displayed in Analysis and Tables**

The goal is to make the app modular and user-configurable while maintaining logical consistency between required and optional measurements.

---

# 🎯 Purpose of the Settings Screen

The Settings screen allows the user to:

* Enable or disable specific body fat analysis methods
* Automatically control required measurement inputs
* Choose which measurements should be displayed in:

  * The Analysis screen (charts)
  * The “All Measurements” table
* Hide irrelevant measurement inputs from the Add/Edit Measurement screen

---

# 🧱 Settings Structure Overview

```text
Settings
 ├── Analysis Methods
 ├── Measurement Collection
 └── Display Configuration
```

---

# 1️⃣ Analysis Methods

This section controls which body fat estimation methods are active.

## Available Analysis Methods

* ☐ BMI
* ☐ Navy Body Fat %
* ☐ Skinfold Body Fat %
* ☐ Waist–Hip Ratio
* ☐ Waist–Height Ratio

Each method can be enabled or disabled independently.

---

## Behavior Rules

### If Navy Body Fat is ENABLED:

Required measurements:

* Neck circumference
* Waist circumference
* (Hip circumference required for females)

These measurements:

* Must be collected
* Cannot be disabled in Measurement Collection
* Must be visible in Add/Edit Measurement screen

---

### If Skinfold Body Fat is ENABLED:

Required measurements (depending on sex):

**Male (3-site):**

* Chest skinfold
* Abdomen skinfold
* Thigh skinfold

**Female (3-site):**

* Triceps skinfold
* Suprailiac skinfold
* Thigh skinfold

These measurements:

* Must be collected
* Cannot be disabled while Skinfold is active

### If Waist–Hip Ratio is ENABLED:

Required measurements:

* Waist circumference
* Hip circumference

These measurements:

* Must be collected
* Cannot be disabled while Waist–Hip Ratio is active

### If Waist–Height Ratio is ENABLED:

Required measurements:

* Waist circumference

These measurements:

* Must be collected
* Cannot be disabled while Waist–Height Ratio is active

---

## UI Example – Analysis Section

```text
Analysis Methods

[✓] Navy Body Fat %
[ ] Skinfold Body Fat %
```

---

# 2️⃣ Measurement Collection

This section controls which raw measurements are recorded when creating or editing a measurement entry.

## Adjustable Measurement Types

### Circumferences

* ☐ Neck
* ☐ Waist
* ☐ Abdomen
* ☐ Hip
* ☐ Chest

### Skinfolds

* ☐ Chest Skinfold
* ☐ Abdomen Skinfold
* ☐ Thigh Skinfold
* ☐ Triceps Skinfold
* ☐ Suprailiac Skinfold

---

## Dependency Rules

If a measurement is required for an enabled analysis:

* The toggle is **disabled (locked)**
* It cannot be turned off
* It is visually marked as required

### Example

If:

```
Navy Body Fat = enabled
```

Then:

```
Neck → locked ON
Waist → locked ON
Hip → locked ON (for female)
```

But:

```
Chest circumference → freely selectable
Abdomen circumference → freely selectable
```

---

## UI Behavior Example

```text
Measurement Collection

Circumferences
[✓] Neck        (required for Navy)
[✓] Waist       (required for Navy)
[ ] Chest
[✓] Hip
[ ] Abdomen

Skinfolds
[ ] Chest Skinfold
[ ] Abdomen Skinfold
[ ] Thigh Skinfold
```

If Skinfold analysis is enabled, the corresponding skinfold entries become locked ON.

### If BMI is ENABLED:

Required measurements:

* Weight

Weight is locked ON in Measurement Collection while BMI is enabled.

---

# 3️⃣ Display Configuration

This section determines which measurements and metrics appear in:

* 📊 Analysis screen (charts)
* 📋 “All Measurements” table

This does **not** affect data collection — only visibility.

---

## Display Options

Each measurement/metric has one placement selector:

* `In both`
* `Only in Analysis`
* `Only in Table`
* `Hidden`

Example configuration:

```text
Display Configuration

Neck: Only in Analysis
Waist: In both
```

---

## Important Rules

1. If a measurement is required for an enabled analysis:

   * It must still be collected
   * But it may be hidden from charts or tables

2. Derived metrics (e.g., Navy Body Fat %, Skinfold Body Fat %):

   * Can also have visibility toggles
   * If the analysis method is disabled:

     * Metric automatically hidden
     * No toggle visible

3. The same visibility configuration is applied consistently to:

   * Analysis chart cards
   * Latest measurement card
   * Measurement table preview and full-list table

---

# 🔁 Interaction Between Sections

### Example Scenario

User enables:

* Navy Body Fat %

Then:

* Neck and Waist become required
* They cannot be disabled in Measurement Collection
* They appear in Add Measurement screen
* User can still choose whether:

  * Neck is shown in Analysis
  * Neck is shown in Table

---

# 🔁 Interaction With Profile

When the user changes their sex on the profile screen and saves, measurements
needed for enabled analysis methods are auto-enabled (but none are disabled).

### Example Scenario

User sets sex from male to female and has Navy Body Fat % enabled

Then:

* Neck and Waist stay required
* Hip becomes required

Implementation detail:

* The additive sync runs on profile save in `ProfileViewModel`.
* `SettingsViewModel` still enforces required measurements in effective UI state
   while editing settings.

---


# 🧠 Add/Edit Measurement Screen Impact

The Measurement input form dynamically adapts based on:

* Enabled analyses
* Selected measurements

If a measurement is disabled in Settings:

* It does not appear in the Add/Edit screen
* It is not stored in future measurement entries

---

# 🗂 Recommended Data Model Structure

```kotlin
data class SettingsState(
      val bmiEnabled: Boolean,
    val navyBodyFatEnabled: Boolean,
    val skinfoldBodyFatEnabled: Boolean,
      val waistHipRatioEnabled: Boolean,
      val waistHeightRatioEnabled: Boolean,

      val enabledMeasurements: Set<MeasuredBodyMetric>,

      val visibleInAnalysis: Set<BodyMetric>,
      val visibleInTable: Set<BodyMetric>
)
```

Persistence:

* Settings are stored in `PreferencesSettingsRepository` (DataStore)
   and include all analysis switches, enabled-measurement set, and both
   visibility sets.

---

# 🔐 Validation Rules

Before saving Settings:

1. Required measurements must remain enabled
2. Required derived metrics are only visible when their analysis method is
   enabled

Note: there is currently no hard validation enforcing a minimum number of
visible metrics in Analysis/Table.

---

# 🎯 UX Goals

* Clear dependency visibility
* No invalid configurations possible
* Immediate UI feedback
* Minimal cognitive overload
* High flexibility for advanced users

---

# ✅ Summary of Requirements

✔ Enable/Disable Navy Body Fat
✔ Enable/Disable Skinfold Body Fat
✔ Enable/Disable Waist–Hip Ratio
✔ Enable/Disable Waist–Height Ratio
✔ Automatically lock required measurements
✔ Allow optional measurements to be toggled freely
✔ Separate display visibility controls
✔ Dynamic Add/Edit form behavior
✔ Prevent invalid configurations


# Completed Phases

The following phases have already been completed and are kept for reference:

## Phase 1 – Core Tracking & User Profile

The first phase focuses on implementing basic measurement tracking along with a persistent user profile.

### User Profile (Settings Screen)

The following data is collected **once** and stored in a settings/profile screen:

* Biological sex
* Date of birth (used to calculate age dynamically)
* Height

These values are not re-entered when creating a new measurement entry.
They are globally available for derived metric calculations (e.g., BMI, body fat formulas).
If the app is started when this data is not present, the user is presented with the corresponding
settings screen where they have to enter the data first.
This data is simply stored in the `DataStore`, not in the sqlite database.

---

### Measurement Entry

Each measurement record contains:

* Date (auto-generated timestamp)
* Weight (kg)
* Neck circumference (cm)
* Chest circumference (cm)
* Waist circumference (cm)
* Abdomen circumference (cm)
* Hip circumference (cm)

This data is stored in the sqlite database using Room. Entering values is optional but at least one value must be entered.

---

### Features

* Create new measurement entries
* Display all entries in a list
* Tap an entry to edit (which overwrites the entry)
* Input validation
* Separation of profile data and measurement data

---

### UI Screens (Phase 1)

* Onboarding/Profile setup screen
* Settings/Profile screen
* Measurement list screen
* Add/Edit measurement screen

---

## Phase 2 – Derived Metrics

In this phase, calculated metrics are introduced.

### Automatically Calculated Values

* Body Mass Index (BMI)
* Estimated Body-Fat using Navy Method
* Waist–Hip Ratio (WHR)
* Waist–Height Ratio (WHtR)

These values are calculated dynamically based on:

* Profile data (height, sex, age)
* Measurement-specific values (weight, circumferences)

The exact formulas are documented in [FORMULAS.md](./FORMULAS.md)

---

## Phase 3 – Advanced Body Fat Estimation

This phase introduces more advanced body composition analysis.

### Skinfold-Based Body Fat Estimation

Implementation of the 3-site skinfold method based on:

* Jackson & Pollock skinfold equations

### Planned Features

* Input for skinfold measurements
* Gender- and age-specific formulas
* Body fat percentage estimation
* Extended body composition analytics

---

## Phase 4 – Historical Trends & Analytics

This phase introduces visual data analysis and statistical summaries.

### Features

* Historical line charts for:
  * Weight progression
  * Body fat percentage
  * Circumference changes
  * Ratios (WHR, WHtR, etc.)
* Moving averages
* Trend indicators
* Min / Max / Mean values
* Time range filtering (e.g., 1 month, 3 months, 1 year, all time)
* Interactive chart exploration (tap marker, zoom, pan)

### Technical Considerations

* Vico charting integration (Compose)
* Aggregation logic in domain layer
* Efficient data querying
* Smooth Compose-based chart rendering

#### Phase 4.4 Settings-driven metric configuration

Implemented:

* Analysis method switches (BMI, Navy body fat, Skinfold body fat)
* Measurement-collection switches with required-measurement locking
* Display placement per metric (`Hidden`, `Only in Table`, `Only in Analysis`, `In both`)
* Visibility applied to:
  * Analysis charts
  * Latest measurement card
  * Table preview + full measurement list
  * Add/Edit measurement form fields
* Profile-save interaction: when sex changes, newly required measurements are auto-enabled additively (never auto-disabled)

### Subphases

#### Phase 4.1 Main Navigation Layout

See [NAVIGATION.md](./NAVIGATION.md)

#### Phase 4.2 Measurements screen redesign

See [MEASUREMENT-SCREEN.md](./MEASUREMENT-SCREEN.md)

#### Phase 4.3 Chart generation

See [ANALYSIS.md](./ANALYSIS.md)

Status: Implemented (duration-based filtering + interactive line chart
s).
Caveat: The ratios (e.g. waist to hip) are also analyses and need to be added - but which of those are actually practically useful?

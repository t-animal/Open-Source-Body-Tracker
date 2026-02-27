# Phase 4.2 – Measurements Screen Design

This document describes the detailed UI and behavior of the **Measurements** tab.

The screen serves two primary purposes:

1. Provide a quick overview of the **latest measurement**
2. Offer structured access to **historical measurement entries**

The layout is implemented using **Jetpack Compose** and follows Material 3 design principles.

---

# 🧱 Screen Structure Overview

```text
Scaffold
 ├── TopAppBar ("Measurements")
 ├── Content
 │     ├── Latest Measurement Card
 │     ├── All Measurements Table (Preview: 20 items)
 │     └── "More" Button (if needed)
 ├── FloatingActionButton ("Add")
 └── BottomNavigationBar
```

---

# 1️⃣ Latest Measurement Card

## Purpose

Displays:

* All measurement values from the most recent entry
* All derived metrics (BMI, Navy Body Fat, WHR, WHtR, HHR)

If no measurement exists:

* Show onboarding-style empty state
* Provide quick access to create the first entry

The displayed metrics in this card are settings-driven:

* Uses `SettingsState.visibleInTable`
* Applies analysis-method gating for derived metrics
* Preserves metric order from shared registry (`BodyMetric.entries`)

---

## 📦 Card Layout

### Header

```
Latest Measurement
```

### Content

The content is displayed as a **2-column grid layout**.

Each item in the grid consists of:

* Large value (with unit)
* Centered label underneath
* Vertical spacing between items

---

## 🧮 Display Rules

If a value is missing or cannot be calculated:

```
--
```

Example:

* If hip circumference not recorded → show `--`
* If derived metric cannot be calculated → show `--`

---

## 📐 Grid Layout Rules

* 2 columns
* Responsive spacing
* Items vertically stacked in rows
* Even distribution

---

## 📊 Example Grid Content

Each measurement cell:

```
82.4 kg
Weight
```

```
24.7
BMI
```

---

## 🖥 ASCII Mockup – Latest Measurement Card

```text
+--------------------------------------------------+
| Latest Measurement                               |
|--------------------------------------------------|
|                                                  |
|   82.4 kg             18.3 %                     |
|   Weight              Body Fat                   |
|                                                  |
|   24.7                 0.92                     |
|   BMI                  WHR                      |
|                                                  |
|   89 cm                1.02                     |
|   Waist                WHtR                     |
|                                                  |
|   95 cm                --                       |
|   Hip                  HHR                      |
|                                                  |
+--------------------------------------------------+
```

---

## 🚫 Empty State (No Measurements)

If no measurements exist:

```text
+--------------------------------------------------+
| Latest Measurement                               |
|--------------------------------------------------|
|                                                  |
|     No measurements yet – create your           |
|     first measurement                            |
|                                                  |
|               [ Add ]                           |
|                                                  |
+--------------------------------------------------+
```

### Behavior

* "Add" button opens **New Measurement Screen**
* Floating Action Button is still visible
* Displayed metrics are configurable in Settings.
* In the future, some metrics will have a third line with an indicator message (e.g. "overweight"/"underweight" for bmi)

---

# 2️⃣ All Measurements Table

Below the Latest Measurement Card:

```
All Measurements
```

## Behavior

* Sorted **newest → oldest**
* Only first **20 measurements** shown
* If more exist:

  * Show text-button: `"More"`

---

## 📋 Table Structure

| Column   | Description           |
| -------- | --------------------- |
| Date     | Measurement date      |
| Weight   | Weight                |
| BMI      | Derived BMI           |
| Body Fat Navy | Derived Navy Body Fat |
| Body Fat Skinfold | Derived Skinfold Body Fat |
| ... all other metrics            |

In the future, the columns will be configurable. For now we display all measurements.

Current behavior:

* Table columns are configurable via Settings display placement
* Date column is always shown
* Metric columns follow `visibleInTable`

---

## 🖥 ASCII Mockup – Table Preview

```text
All Measurements

+------------+---------+------+----------+
| Date       | Weight  | BMI  | BF Navy %     |
+------------+---------+------+----------+
| 2026-02-20 | 82.4 kg | 24.7 | 18.3 %   |
| 2026-02-15 | 83.1 kg | 25.0 | 18.7 %   |
| 2026-02-10 | 84.0 kg | 25.3 | --       |
| ...                                        |
+------------+---------+------+----------+

                 [ More ]
```

---

# 3️⃣ “More” Screen – Full Measurement List

When user taps **More**:

Open a new screen containing:

* Same table layout
* All measurements
* Back button in top app bar
* No Latest Measurement card
* No FAB

---

## 🖥 ASCII Mockup – Full List Screen

```text
+--------------------------------------------------+
| All Measurements                          ← Back |
+--------------------------------------------------+

+------------+---------+------+----------+
| Date       | Weight  | BMI  | BF %     |
+------------+---------+------+----------+
| 2026-02-20 | 82.4 kg | 24.7 | 18.3 %   |
| 2026-02-15 | 83.1 kg | 25.0 | 18.7 %   |
| 2026-02-10 | 84.0 kg | 25.3 | 19.1 %   |
| 2026-02-05 | 84.5 kg | 25.5 | 19.4 %   |
| 2026-01-28 | 85.2 kg | 25.8 | 19.8 %   |
| ...                                              |
+--------------------------------------------------+
```

---

# 4️⃣ Floating Action Button (FAB)

Position:

* Bottom-right corner
* Standard Material 3 FAB placement

Label:

```
Add
```

Behavior:

* Opens **New Measurement Screen**
* Always visible on main Measurements tab
* Hidden on full list screen (recommended)

---

# 🧠 UI Behavior Rules

### Data Loading

* Latest measurement = most recent by date
* Derived metrics calculated in domain layer
* Missing values rendered as `"--"`
* Disabled metrics in settings are excluded from card/table layouts

### Add/Edit form integration

* Input fields are shown only for enabled measurements
* Required measurements from active analyses are always enabled
* Disabled fields are saved as `null` (not persisted as active values)

### Performance

* Use LazyColumn for:

  * Table preview
  * Full list screen

### State Handling

* Loading state (optional)
* Empty state
* Populated state

---

# 🎯 UX Goals

* Fast overview of current physical state
* Clear distinction between:

  * Current status
  * Historical data
* Minimal scrolling
* Clean and scannable layout
* Strong numeric emphasis

---

# ✅ Summary of Requirements

✔ Latest Measurement Card
✔ 2-column measurement grid
✔ Derived metrics included
✔ "--" for missing values
✔ Empty state with Add button
✔ Table preview (20 entries)
✔ “More” navigation to full list
✔ FAB for quick entry

---

This completes the detailed specification for the **Measurements Screen (Phase 4.2)**.

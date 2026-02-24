# Phase 4.3 – Analysis Screen (Charts)

This document defines the structure and behavior of the **Analysis** tab.
The purpose of this screen is to visualize historical measurement data over a selectable time range.

The screen focuses on clarity, scalability, and consistent visual representation of all tracked metrics.


# 🧱 Screen Structure Overview

```text id="6o2jrd"
Scaffold
 ├── TopAppBar ("Analysis")
 ├── Content
 │     ├── Duration Segmented Control
 │     └── Metric Chart Cards (LazyColumn)
 └── BottomNavigationBar
```

---

# 1️⃣ Duration Selector (Segmented Button)

At the top of the screen, a segmented button control allows the user to select the timeframe to analyze.

## Available Durations

* 1 Month
* 3 Months (default)
* 6 Months
* 1 Year
* All

## Default Selection

```text id="oh87oj"
3 Months
```

## Behavior

* Only one duration can be selected at a time.
* Changing the selection:

  * Recalculates the filtered dataset
  * Updates all charts immediately
* Filtering is based on measurement date:

  * `now - duration` to `now`

---

## 🖥 ASCII Mockup – Duration Selector

```text id="3klemi"
+--------------------------------------------------+
| [ 1M ] [ 3M* ] [ 6M ] [ 1Y ] [ All ]            |
+--------------------------------------------------+
```

(* = selected)

---

# 2️⃣ Metric Chart Cards

Below the segmented control, a scrollable list (`LazyColumn`) displays one card per metric.

Each card represents **one metric over time**.

---

## 📦 Card Structure

Each metric card contains:

1. Title (metric name)
2. Line chart
3. Automatic axis scaling

---

## Supported Metrics (Initial Scope)

* Weight
* BMI
* Navy Body Fat %
* Skinfold Body Fat %
* Waist
* Hip
* Waist–Hip Ratio
* Waist–Height Ratio
* Hip–Height Ratio

The list can easily be extended in the future.

---

# 📈 Chart Specifications

Each chart must:

* Use a **line chart**
* Show datapoints connected chronologically
* Use:

  * X-axis → Time (date)
  * Y-axis → Metric value
* Automatically scale Y-axis to fit visible data
* Dynamically adjust to selected duration

---

## Axis Behavior

### X-Axis

* Represents measurement date
* Sorted ascending (oldest → newest)
* Formatted depending on timeframe:

  * 1M → show day labels
  * 1Y → show month labels
  * All → adaptive spacing

### Y-Axis

* Automatically calculated min/max from filtered data
* Add small vertical padding (e.g., 5%) for visual clarity

---

## 🖥 ASCII Mockup – Single Metric Card

```text id="5060rl"
+--------------------------------------------------+
| Weight                                           |
|--------------------------------------------------|
|                                                  |
|   85 |                         *                |
|      |                     *                    |
|   83 |                 *                        |
|      |             *                            |
|   81 |         *                                |
|      |______________________________________    |
|        01.01 01.02 01.03 01.04                  |
|                                                  |
+--------------------------------------------------+
```

Note: Data points should be visible on the chart but also
connected with a line.

---

# 3️⃣ Empty Data Handling

If a metric has **no data** in the selected timeframe:

* The card still shows the title
* Instead of a chart, display:

```text id="kph49n"
no data yet
```

---

## 🖥 ASCII Mockup – Empty Metric Card

```text id="44nqj7"
+--------------------------------------------------+
| Waist–Hip Ratio                                 |
|--------------------------------------------------|
|                                                  |
|               no data yet                       |
|                                                  |
+--------------------------------------------------+
```

---

# 4️⃣ Performance Considerations

* Use `LazyColumn` for metric cards
* Memoize filtered data when duration changes
* Avoid recalculating derived metrics inside composables
* Compute in domain layer

---

# 5️⃣ Future Extension (Not Implemented Yet)

A **Weighted Moving Average (WMA)** line will be added later:

* Displayed as a secondary line
* Slightly transparent
* Smoothed trend representation

For now:

✔ Only raw datapoints
✔ No smoothing
✔ No average line

---

# 6️⃣ UI Behavior Rules

* Maintain consistent card spacing
* Charts should have consistent height across metrics

---

# 🎯 UX Goals

* Quick identification of trends
* Clear separation per metric
* No visual overload
* Responsive time filtering
* Clean scaling behavior

---

# ✅ Summary of Requirements

✔ Segmented duration selector
✔ Default selection = 3 Months
✔ One chart card per metric
✔ Automatic axis scaling
✔ X-axis = time
✔ Y-axis = value
✔ No data → “no data yet”
✔ No smoothing (yet)



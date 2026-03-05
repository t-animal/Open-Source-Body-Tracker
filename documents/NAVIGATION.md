# Navigation Structure

This document describes the currently implemented navigation behavior.

---

## Main Shell

The app uses a shared `MainScreenScaffold` for main tabs:

- top app bar with dynamic title
- overflow menu
- bottom navigation

Main destinations:

- Measurements (`measurement_list`)
- Analysis (`analysis`)
- Photos (`photos`)

Tab navigation uses a single `NavController` stack with `launchSingleTop = true` for tab selections.

---

## Overflow Menu

Main tabs expose overflow actions:

- Profile
- Settings

In debug builds, a fake data generator action is also available.

---

## Onboarding Gate

Start destination is onboarding.

If a profile exists while the current route is onboarding, navigation automatically moves to Measurements and removes onboarding from back stack.

---

## Route Overview

### Main-tab routes

- `measurement_list`: implemented measurement overview with latest card, table preview, and FAB.
- `analysis`: implemented chart screen with duration filtering and Vico line charts.
- `photos`: implemented photo gallery with compare/animate mode entry.

### Secondary routes

- `profile`: editable profile form.
- `settings`: implemented settings configuration screen.
- `measurement_add`: add measurement flow.
- `measurement_edit/{id}`: edit measurement flow.
- `measurement_list_all`: full measurement table screen with back button.
- `photo_compare/{leftMeasurementId}/{rightMeasurementId}`: compare screen with draggable before/after slider.
- `photo_animate`: animation playback screen for selected photos.
- `fake_data_generator` (debug only): fake data screen.

`BodyTrackerNavHost` now passes `SettingsRepository` into:

- `ProfileRoute`
- `SettingsRoute`
- `MeasurementListRoute` / `MeasurementListFullRoute`
- `MeasurementEditRoute`
- `AnalysisRoute`

---

## Back Behavior

- Add/edit/full-list screens are pushed onto the main stack and return with system/back icon.
- Profile/settings are opened from overflow and return to previous screen on back.
- Main tabs remain top-level destinations selected via bottom navigation.

---

## Screen Status Summary

- Measurements: implemented
- Analysis: implemented
- Photos: implemented
- Profile: implemented
- Settings: implemented

---

# 🧭 Navigation Structure

The app uses:

* **Top App Bar**
* **Overflow Menu (3-dot menu)**
* **Bottom Navigation Bar**

All main screens share the same layout structure.

---

## 🔝 Top App Bar

Each main screen contains a top navigation bar with:

* **Dynamic page title**

  * "Measurements"
  * "Analysis"
  * "Photos"
* **Three-dot overflow menu (⋮)**

### Overflow Menu Entries

* **Profile**
* **Settings**

### Navigation Behavior

| Menu Item | Destination                                   |
| --------- | --------------------------------------------- |
| Profile   | Profile screen                                |
| Settings  | Settings configuration screen                 |

---

## 🔽 Bottom Navigation Bar

The bottom navigation contains three entries:

| Label | Meaning      | Destination                         |
| ----- | ------------ | ----------------------------------- |
| M     | Measurements | Measurement list screen             |
| A     | Analysis     | Analysis screen                     |
| P     | Photos       | Photos gallery + compare/animate modes |

Icons will later replace the letters.

---

# 📱 Screens (Current Scope)

## 1️⃣ Measurements Screen (Default Start Destination)

* Top bar: Title = "Measurements"
* Overflow menu available
* Bottom navigation visible
* Content: Measurement list (implemented)

---

## 2️⃣ Analysis Screen

* Top bar: Title = "Analysis"
* Overflow menu available
* Bottom navigation visible
* Content: duration selector + chart cards based on settings visibility

---

## 3️⃣ Photos Screen (Implemented)

* Top bar: Title = "Photos"
* Overflow menu available
* Bottom navigation visible
* Content:
   - scrollable photo gallery
   - compare mode selection + compare route
   - animate mode selection + animation route

---

## 4️⃣ Profile Screen

* Accessible via overflow menu. 
* Same top bar layout
* No bottom navigation (optional design decision — recommended: no bottom nav)

Contains:

* Sex
* Date of birth
* Height

Editable form layout.

---

## 5️⃣ Settings Screen

* Accessible via overflow menu. 
* Same top bar layout
* No bottom navigation (optional design decision — recommended: no bottom nav)
* Configurable sections:

   * Analysis Methods
   * Measurement Collection
   * Display Configuration

---

# 🖥 ASCII Mockup – Measurements Screen

```
+--------------------------------------------------+
| Measurements                              ⋮     |
+--------------------------------------------------+

|                                                  |
|   [ 2026-02-20 ]                                |
|   Weight: 82.4 kg                                |
|   Waist: 89 cm                                   |
|                                                  |
|   --------------------------------------------   |
|                                                  |
|   [ 2026-02-15 ]                                |
|   Weight: 83.1 kg                                |
|   Waist: 90 cm                                   |
|                                                  |
|                                                  |
|                                                  |
|                                                  |
+--------------------------------------------------+
|     M              A              P             |
+--------------------------------------------------+
```

---

# 🖥 ASCII Mockup – Analysis Screen (Empty Placeholder)

```
+--------------------------------------------------+
| Analysis                                   ⋮    |
+--------------------------------------------------+

|                                                  |
|                                                  |
|              Analysis Coming Soon                |
|                                                  |
|                                                  |
|                                                  |
|                                                  |
|                                                  |
+--------------------------------------------------+
|     M              A              P             |
+--------------------------------------------------+
```

---

# 🖥 ASCII Mockup – Photos Screen (Gallery)

```
+--------------------------------------------------+
| Photos                                     ⋮    |
+--------------------------------------------------+

|                                                  |
|                                                  |
|              [Photo feed content]               |
|                                                  |
|                                                  |
|                                                  |
|                                                  |
|                                                  |
+--------------------------------------------------+
|     M              A              P             |
+--------------------------------------------------+
```

---

# 🖥 ASCII Mockup – Overflow Menu

```
        ⋮
        |
        |  Profile
        |  Settings
```

---

# 🧠 Navigation Behavior Rules

1. Bottom navigation switches between:

   * Measurements
   * Analysis
   * Photos

2. Overflow menu is available on:

   * Measurements
   * Analysis
   * Photos

3. Profile and Settings:

   * Open as separate screens
   * Accessible from any main screen
   * Do not replace bottom navigation state

4. Back navigation:

   * Bottom navigation maintains its own back stack (recommended)
   * System back returns to previous screen

---

# 🎯 Goals of Phase 3.1

* Establish consistent navigation structure
* Prepare placeholder screens for upcoming features
* Ensure scalability for future feature expansion
* Follow Material 3 navigation guidelines
* Maintain clean MVVM separation

---

This reflects the current navigation structure, including the implemented Photos compare and animation routes.

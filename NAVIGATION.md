
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
| Settings  | Settings screen (currently empty placeholder) |

---

## 🔽 Bottom Navigation Bar

The bottom navigation contains three entries:

| Label | Meaning      | Destination                         |
| ----- | ------------ | ----------------------------------- |
| M     | Measurements | Measurement list screen             |
| A     | Analysis     | Analysis screen (empty placeholder) |
| P     | Photos       | Photos screen (empty placeholder)   |

Icons will later replace the letters.

---

# 📱 Screens (Phase 3.1 Scope)

## 1️⃣ Measurements Screen (Default Start Destination)

* Top bar: Title = "Measurements"
* Overflow menu available
* Bottom navigation visible
* Content: Measurement list (implemented)

---

## 2️⃣ Analysis Screen (Placeholder)

* Top bar: Title = "Analysis"
* Overflow menu available
* Bottom navigation visible
* Content: Empty placeholder layout

---

## 3️⃣ Photos Screen (Placeholder)

* Top bar: Title = "Photos"
* Overflow menu available
* Bottom navigation visible
* Content: Empty placeholder layout

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

## 5️⃣ Settings Screen (Placeholder)

* Accessible via overflow menu. 
* Same top bar layout
* No bottom navigation (optional design decision — recommended: no bottom nav)
* Empty screen (placeholder)

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

# 🖥 ASCII Mockup – Photos Screen (Empty Placeholder)

```
+--------------------------------------------------+
| Photos                                     ⋮    |
+--------------------------------------------------+

|                                                  |
|                                                  |
|              Photos Coming Soon                  |
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

This completes the structural navigation design required before expanding Analysis and Photos functionality in later phases.

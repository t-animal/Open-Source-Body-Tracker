# Body Metrics Tracker (Android)

An Android application for tracking personal body measurements over time.
The app enables users to record key anthropometric data, monitor changes, and calculate derived health indicators such as BMI and body composition ratios.
It is translated into English and German.

The project is built using **MVVM architecture**, **Jetpack Compose** for the UI layer and **Room** for storage.

This is also a test for AI-driven development, so don't expect much. It might kill your cat or whatever.

---

## 📱 Project Overview

**Body Metrics Tracker** helps users:

* Track core body measurements
* Monitor physical progress over time
* Calculate derived health metrics
* Visualize historical trends
* Compare progress visually with photos
* Extend functionality incrementally through clearly defined development phases

## Implementation hints

The development follows a structured, multi-phase approach, starting with a minimal core feature set and expanding toward advanced analytics and visualization.

The minimal android version to support is Android 10 (API level 29) target is Android 16.

All UI elements (components and screens) have a preview.

Code is linted using ktlint.
---

## 🏗 Architecture

The application is implemented using:

* **MVVM (Model–View–ViewModel)** architectural pattern
* Android Jetpack components
* Jetpack Compose for UI
* Repository pattern for data handling
* Local persistence (Room)
* State management using StateFlow or LiveData

### Architecture Layers

**Model**

* Data classes representing user profile and body measurements
* Business logic for derived metric calculations

**View**

* Jetpack Compose UI
* Stateless composables observing ViewModel state

**ViewModel**

* State management
* Validation logic
* Coordination between UI and domain layer

---

# 🚀 Development Roadmap

---

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
* Hip–Height Ratio

These values are calculated dynamically based on:

* Profile data (height, sex, age)
* Measurement-specific values (weight, circumferences)

The exact formulas are documented in [FORMULAS.md](FORMULAS.md)

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

### Technical Considerations

* Charting library integration
* Aggregation logic in domain layer
* Efficient data querying
* Smooth Compose-based chart rendering

---

## Phase 5 – Photo Progress Tracking

This phase enables visual comparison of physical progress.

### Features

* Attach a photo to each measurement entry
* Secure local storage of images
* Thumbnail preview in list view
* Side-by-side comparison mode
* Timeline-based photo gallery
* Before/After comparison view

### Technical Considerations

* Camera integration
* Image storage handling
* File management and caching
* Permission handling
* Performance optimization for image loading

---

# 📊 Data Model (Simplified Draft)

```kotlin
data class UserProfile(
    val sex: Sex,
    val dateOfBirth: LocalDate,
    val heightCm: Double
)

data class BodyMeasurement(
    val id: Long,
    val date: Instant,
    val weightKg: Double? = null,
    val neckCircumferenceCm: Double? = null,
    val chestCircumferenceCm: Double? = null,
    val waistCircumferenceCm: Double? = null,
    val abdomenCircumferenceCm: Double? = null,
    val hipCircumferenceCm: Double? = null,
    val photoUri: String? = null
)
```

Derived metrics are computed in the domain layer and not redundantly stored. Internally, all values are stored using SI unit (or derived values)
like cm and kg. In the future there may be a switch for the user to display imperial units, but that is not planned right now.z

---

# 🛠 Tech Stack

* Kotlin
* Android SDK
* Jetpack Compose
* ViewModel
* StateFlow / LiveData
* Room
* Material Design 3

---

# 🎯 Project Goals

* Clean architecture
* Incremental development
* Strong separation of concerns
* Testable calculation logic
* Modern Android best practices
* Extensible data model for future metrics

---

# 📄 License

License to be defined.

---

# 📌 Status

🚧 In Active Development – Phase 1, 2 and 3 (JP 3-site skinfold) implemented
If you're an AI Agent, check AI-DEVEL.md for details.

# Body Metrics Tracker (Android)

An Android application for tracking personal body measurements over time.
The app enables users to record key anthropometric data, monitor changes, and calculate derived health indicators such as BMI and body composition ratios.
It is translated into English and German.

The project is built using **MVVM architecture**, **Jetpack Compose** for the UI layer,
**Room** for measurement storage and **DataStore** for profile/settings persistence.

This is also a test for AI-driven development, so don't expect much. It might kill your cat or whatever.

---

## 📱 Project Overview

**Body Metrics Tracker** helps users:

* Track core body measurements
* Monitor physical progress over time
* Calculate derived health metrics
* Visualize historical trends
* Compare progress visually with photos
* Configure enabled analyses, collected measurements, and display visibility
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

The following phases have already [been completed](documents/COMPLETED.md):

- Phase 1 – Core Tracking & User Profile 
- Phase 2 – Derived Metrics
- Phase 3 – Advanced Body Fat Estimation
- Phase 4 – Historical Trends & Analytics

---

## Phase 5 – Photo Progress Tracking

This phase enables visual comparison of physical progress.

### Features

* Attach a photo to each measurement entry
* Secure local storage of images
* Thumbnail preview in list view
* Side-by-side comparison mode
* Multi-photo animation playback mode
* Timeline-based photo gallery
* Before/After comparison view

### Technical Considerations

* Camera integration
* Image storage handling
* File management and caching
* Permission handling
* Performance optimization for image loading

Due to its size and complexity, this phase is divided into multiple subphases:
    - Phase 5.1 – Photo Capture & Secure Storage
    - Phase 5.2 – Optimized Photo Gallery
    - Phase 5.3 – Photo Comparison Mode (2-Image Slider)
    - Phase 5.4 – Compare Mode & Animation Mode (Advanced Interaction)

For details see [documents/PHOTOS.md](documents/PHOTOS.md)

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
* Vico (Compose charts)
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

🚧 In Active Development – Phase 1-4 implemented, Phase 5 in progress
If you're an AI Agent, check [AI-DEVEL.md](documents/AI-DEVEL.md) for details.


# Ideas for the future and tech debt

* Allow reordering of measurements for table and analysis
* Tapping photo preview closes it
* paths (e.g. in internalphotostorage) should be provided by a central instance, not hardcoded
* reminders to enter measurement
* translation
* geburtstag als datum speichern, nicht als timestamp
* prevent android built-in backups (https://developer.android.com/identity/data/autobackup?hl=de) at least for photos, add custom backup logic
# App Overview

This app is a **body measurement and body composition tracking tool** designed to help users monitor physical progress over time. It allows users to record anthropometric measurements and apply established analysis methods to estimate body composition and related metrics.

## Purpose

The primary purpose of the app is to provide a **simple, structured, and privacy-focused way** to track body measurements and analyze body composition using scientifically recognized formulas.

It is intended for:

* Fitness tracking
* Body recomposition monitoring
* Research or self-quantification
* Long-term physical progress tracking

## Core Functionalities

**1. Measurement Tracking**

Users can record a wide range of body measurements, such as:

* Body weight
* Circumference measurements (e.g., waist, hips, chest, arms)
* Skinfold measurements for body fat estimation

Measurements are stored chronologically to allow **progress tracking over time**.

---

**2. Body Composition Analysis**

The app supports multiple **analysis methods** that use recorded measurements to estimate body composition.

Examples include:

* Skinfold-based body fat estimation methods (e.g., Jackson & Pollock formulas)
* Other measurement-based body composition calculations

Users can choose **which analysis methods they want to use**, and the app automatically activates the required measurements.

---

**3. Configurable Measurement System**

When enabling analysis methods, the app automatically:

* Activates all measurements required for those methods
* Allows users to enable **additional optional measurements** in the settings

This ensures that the measurement workflow remains **minimal but flexible**.

---

**4. Progress Monitoring**

By storing measurements over time, the app enables users to:

* Track physical changes
* Monitor body composition estimates
* Maintain a historical log of their measurements

---

**5. Measurement Reminders**

Optional reminders can be configured to help users maintain **consistent measurement routines**.
Users can select specific weekdays and times for reminders.

---

## Privacy and Data Security

**Data confidentiality is a core design principle of the app.**

* **All data is stored locally on the device.**
* **No measurement data is transmitted to external servers.**
* **No cloud synchronization is required.**
* **No user accounts are needed.**

This ensures that **sensitive personal health and body data remains fully private and under the user’s control**.

---

## Key Design Principles

The app is built around the following principles:

* **Privacy-first architecture**
* **Local data storage**
* **Transparency of calculations**
* **Scientific measurement methods**
* **Minimal and focused user interface**

---

## Summary

The app provides a **private, reliable, and scientifically grounded tool for body measurement tracking and body composition analysis**, while ensuring that **all sensitive user data remains securely stored on the device itself**.


# Development

An Android application for tracking personal body measurements over time.
The app enables users to record key anthropometric data, monitor changes, and calculate derived health indicators such as BMI and body composition ratios.
It is translated into English and German.

The project is built using **MVVM architecture**, **Jetpack Compose** for the UI layer,
**Room** for measurement storage and **DataStore** for profile/settings persistence.

This is also a test for AI-driven development, so don't expect much. It might kill your cat or whatever.


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

# 📊 Data Model

Derived metrics are computed in the domain layer and not redundantly stored. Internally, all values are stored using SI unit (or derived values)
like cm and kg. In the future there may be a switch for the user to display imperial units, but that is not planned right now.

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

If you're an AI Agent, check [AGENTS.md](documents/AGENTS.md) for details.


# Ideas for the future and tech debt

* Allow reordering of measurements for table
* translation
* fab button colors (primary/secondary photos screen and measurement edit)
* lazycolumn (?) ruckler auf measurement list und visibility settings
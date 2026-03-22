# Architecture

## Overview

Single-module Android application using MVVM with Kotlin, Jetpack Compose, Hilt, Room, and DataStore. Not a modular monolith — all code lives in one Gradle module.

## Package Layout

```
app/src/main/java/de/t_animal/opensourcebodytracker/
├── core/       # Shared models, value types, utilities
├── data/       # Persistence, storage, and infrastructure implementations
├── domain/     # Use cases, derived metric formulas, business logic
├── feature/    # Screen-level ViewModels
├── ui/         # Compose UI, navigation, theme
└── di/         # Hilt modules (BindingsModule, DatabaseModule, etc.)
```

## Layer Responsibilities

**core/** — Pure data carriers and shared types. `BodyMeasurement`, `UserProfile`, settings data classes, photo path value classes, metric descriptors. No business logic.

**data/** — Repository interfaces and their implementations. Room DAOs, DataStore persistence, photo file storage, export archive writing, document tree access, reminder scheduling/notifications, background export worker. Contains both the interface definitions and the concrete classes that implement them. Also contains infrastructure classes for interfacing with android.

**domain/** — Use cases and business logic. Derived metric calculation, measurement save/delete orchestration, export coordination, backup import, required-measurements resolution. Depends on data/ repository interfaces and some infrastructure classes.

**feature/** — One ViewModel per screen. Subscribes to repositories and domain use cases, manages UI state. Depends on both data/ and domain/.

**ui/** — Shared ui components, navigation host, theme. Stateless where possible; state comes from ViewModels.

**di/** — Hilt wiring.

## Dependency Direction

The dependency flow is practical, not strictly layered:

```
ui/ → feature/ → domain/ → data/ → core/
                  feature/ → data/ → core/
                  domain/ → core/
```

domain/ imports repository interfaces from data/. This is a known deviation from clean/hexagonal architecture where domain would own its own port interfaces. See the reasoning below for why this is intentional.

## Key Design Decisions

### Repository interfaces live in data/, not domain/

In textbook clean architecture, domain owns its port interfaces and data/ implements them. In this app, the repository interfaces (`MeasurementRepository`, `ProfileRepository`, settings repositories) live in data/ alongside their implementations.

This is a deliberate trade-off:
- The app is a single Gradle module. There is no compile-time boundary enforcement between packages, so moving interfaces to domain/ would only change package names without providing real protection.
- The interfaces are already pure Kotlin with no Android dependencies — they only reference core/ model types. The "violation" is cosmetic (an import path), not structural.
- Adding indirection to satisfy an architecture diagram creates files, mental overhead, and churn without improving testability, flexibility, or safety for an app this size.
- If the app later grows to multiple Gradle modules, interfaces can be relocated at that point when the boundary enforcement becomes real and valuable.

### domain/ has some Android and infrastructure dependencies

Several domain/ classes import Android framework types or concrete data/ classes:
- `ImportBackupUseCase` uses `android.content.Context` and `android.net.Uri` for file access
- `AutomaticExportScheduler` uses `androidx.work.WorkManager`
- `DemoDataPhotoSeeder` uses `android.content.res.AssetManager`
- Several use cases depend on `InternalPhotoStorage` (concrete class) rather than an interface

These could be abstracted behind domain-owned interfaces, but the same reasoning applies: in a single-module app, the indirection adds cost without meaningful benefit. If unit testing these classes without Android becomes a priority, interfaces can be extracted at that point — the refactoring is straightforward since Hilt already manages the wiring.

## When to Revisit

Consider investing in stricter layering if:
- The app moves to a multi-module Gradle setup (boundary enforcement becomes real)
- Domain use cases need extensive unit testing without Android instrumentation
- A second platform target (e.g. KMP) requires a pure-Kotlin domain module
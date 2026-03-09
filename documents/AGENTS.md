# AI Agent Handoff

This file is the minimal, high-signal handoff for coding agents working in this repository.
Do not use it as a changelog.

## Goal

Maintain and extend the Android app safely and quickly with consistent architecture, behavior, and validation.

## Tech Stack

- Kotlin + Jetpack Compose + MVVM
- Room for measurement persistence
- DataStore for profile/settings persistence
- Domain layer for derived metrics (computed at runtime, not stored)
- Navigation Compose for all app routes
- Vico for chart rendering

## Source of Truth (Code Areas)

- App/DI: `app/src/main/java/de/t_animal/opensourcebodytracker/AppContainer.kt`
- Entry/UI host: `app/src/main/java/de/t_animal/opensourcebodytracker/MainActivity.kt`
- Navigation: `app/src/main/java/de/t_animal/opensourcebodytracker/ui/navigation/BodyTrackerNavHost.kt`
- Routes: `app/src/main/java/de/t_animal/opensourcebodytracker/ui/navigation/Routes.kt`
- Profile + onboarding flows: `app/src/main/java/de/t_animal/opensourcebodytracker/feature/settings/`
- Measurements feature: `app/src/main/java/de/t_animal/opensourcebodytracker/feature/measurements/`
- Analysis feature: `app/src/main/java/de/t_animal/opensourcebodytracker/feature/analysis/`
- Photos feature: `app/src/main/java/de/t_animal/opensourcebodytracker/feature/photos/`
- Core models: `app/src/main/java/de/t_animal/opensourcebodytracker/core/model/`

## Non-Negotiable Runtime Rules

- Measurement save requires at least one numeric metric.
- Derived metrics must remain deterministic and nullable when input is insufficient.
- Photos are private/internal and attached to measurements through app flow (must not show up in public gallery).
- User's data confidentiality is key

## Build & Validation Commands

Use these commands to build and validate:

```bash
./gradlew :app:compileDebugKotlin --console=plain
./gradlew :app:lintDebug --console=plain
./gradlew :app:testDebugUnitTest --console=plain
./gradlew ktlintCheck --console=plain
```

All code must compile and lint with both linters (`lintDebug` and `ktlintCheck`). **There are no exceptions**.

Test must succeed and may only be changed if they fail because the tested behavior has changed on purpose.

## Known Toolchain Constraints

- AGP 9 built-in Kotlin setup is expected; do not assume Kotlin plugin wiring from older templates.
- KSP + Room generation is required for DB layer; keep KSP configuration intact.
- `android.disallowKotlinSourceSets=false` warning is expected in this project.
- Native library strip warnings may appear during packaging and are currently non-fatal.

## Agent Working Rules

- Prefer minimal, targeted diffs; avoid broad refactors unless required.
- Keep behavior-compatible with existing navigation/data contracts.
- Update tests when changing domain logic or selection/state behavior.
- Do not edit generated outputs (`build/`, `app/build/`, generated sources).
- Keep this file focused on stable execution guidance (no phase/progress history).

## Documents Index

- [AI-DEVEL.md](AI-DEVEL.md): Minimal AI-agent handoff and operating constraints.
- [ANALYSIS.md](ANALYSIS.md): Analysis tab behavior, data flow, chart rules, and transforms.
- [EXPORT.md](EXPORT.md): Data export for backup and synchronization.
- [FORMULAS.md](FORMULAS.md): Forward formulas for derived body metrics and implementation notes.
- [FORMULAS_INVERTED.md](FORMULAS_INVERTED.md): Inverted formula math for generating target-compatible measurements.
- [MEASUREMENT-SCREEN.md](MEASUREMENT-SCREEN.md): Measurements UI structure, layout rules, and behavior details.
- [NAVIGATION.md](NAVIGATION.md): Route map, scaffold behavior, gating, and back-stack notes.
- [ONBOARDING.md](ONBOARDING.md): Onboarding flow, demo mode, reset behavior, and completion conditions.
- [PHOTOS.md](PHOTOS.md): Photo capture/storage/gallery/compare/animate feature specification.
- [REMINDER.md](REMINDER.md): Reminder phases and implementation spec (settings UI, manual trigger, scheduling).
- [SETTINGS.md](SETTINGS.md): Settings model, method toggles, required measurement dependencies, and display config.
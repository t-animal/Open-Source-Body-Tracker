# AI Agent Handoff

This file is the minimal, high-signal handoff for coding agents working in this repository.
Do not use it as a changelog.

## Goal

Maintain and extend the Android app safely and quickly with consistent architecture, behavior, and validation.

## Tech Stack

- Kotlin + Jetpack Compose + MVVM
- Hilt for dependency injection
- Room for measurement persistence
- DataStore for profile/settings persistence
- Domain layer for derived metrics (computed at runtime, not stored)
- Navigation Compose for all app routes
- Vico for chart rendering

## Source of Truth (Code Areas)

- DI modules: `app/src/main/java/de/t_animal/opensourcebodytracker/di/` (DatabaseModule, DataStoreModule, BindingsModule, AppModule)
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
- KSP processes both Room and Hilt annotations; keep KSP configuration intact.
- `android.disallowKotlinSourceSets=false` warning is expected in this project.
- Native library strip warnings may appear during packaging and are currently non-fatal.

## Agent Working Rules

- Prefer minimal, targeted diffs; avoid broad refactors unless required.
- **Do not add default parameter values for backward compatibility.** Since we own the entire codebase, make new parameters explicit and update all call sites. Only use defaults when they represent a genuinely sensible default for the parameter's semantics (e.g., `Modifier = Modifier`), not to avoid updating callers.
- Keep behavior-compatible with existing navigation/data contracts.
- Update tests when changing domain logic or selection/state behavior.
- Do not edit generated outputs (`build/`, `app/build/`, generated sources).
- Keep this file focused on stable execution guidance (no phase/progress history).
- **When adding, removing, or changing UI strings**, read [TRANSLATIONS.md](TRANSLATIONS.md) first and follow its rules to keep all locale files in sync.
- **Every new `@Composable` function must include a `@Preview`-annotated preview** in the same file, wrapped in `BodyTrackerTheme`. If the component behaves differently based on state (e.g., unit system, empty vs filled), add one preview per meaningful variant.
- When I ask a question, **answer the question**. Do not interpret questions as requests to change code. I will use imperative language when I want changes made.

## Documents Index

- [INDEX.md](INDEX.md): Project overview, glossary, feature summaries, and links to all documentation.
- [ARCHITECTURE.md](ARCHITECTURE.md): Package layout, layer responsibilities, dependency direction, and design trade-offs.
- [NAVIGATION.md](NAVIGATION.md): Route map, scaffolds, overflow menu, and back behavior.
- [FORMULAS.md](FORMULAS.md): Derived metric formulas (BMI, Navy, Skinfold, WHR, WHtR).
- [HEALTH_RATINGS.md](HEALTH_RATINGS.md): Health rating thresholds and severity levels for derived metrics.
- [SETTINGS.md](SETTINGS.md): Settings hub, analysis methods, measurement dependencies, and visibility config.
- [EXPORT.md](EXPORT.md): Export archive format, encryption, auto-export scheduling, and import flow.
- [TRANSLATIONS.md](TRANSLATIONS.md): Rules for adding, removing, and syncing string resources across all locale files.

- **Very important**: If you change something so that these documents don't align any more, **adjust the documents**. If you spot that the documents have drifted from the implementation, suggest updating them.
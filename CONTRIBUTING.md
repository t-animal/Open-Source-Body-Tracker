# Contributing

## Requirements

- Android Studio (latest stable recommended)
- Android SDK installed
- JDK 17+

Minimum supported Android version: **Android 10 (API 29)**
Target SDK: **Android 16 (API 36)**

## Build & Validation

Before submitting changes, run:

```bash
./gradlew :app:compileDebugKotlin --console=plain
./gradlew :app:lintDebug --console=plain
./gradlew :app:testDebugUnitTest --console=plain
./gradlew ktlintCheck --console=plain
```

All code must compile and pass both linters (`lintDebug` and `ktlintCheck`). Tests must succeed and may only be changed if the tested behavior has intentionally changed.

## Code Conventions

- UI is built with **Jetpack Compose**; code is linted with **ktlint**
- Every new `@Composable` function must include a `@Preview`-annotated preview in the same file, wrapped in `BodyTrackerTheme`. Add one preview per meaningful variant (e.g. different unit systems, empty vs. filled state)
- When adding, removing, or changing UI strings, read [documents/TRANSLATIONS.md](documents/TRANSLATIONS.md) and keep all locale files in sync if possible. Explicitly hint in PR if you couldn't do that.

## Architecture

See [documents/ARCHITECTURE.md](documents/ARCHITECTURE.md) for package layout, layer responsibilities, and key design decisions.

## License

License to be defined.

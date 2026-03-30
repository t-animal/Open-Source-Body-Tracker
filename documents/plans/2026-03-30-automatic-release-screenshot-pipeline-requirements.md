## 📋 Feature Specification
**Feature Name**: Automatic Release Screenshot Pipeline  
**Platform Context**: Android app using Jetpack Compose, minSdk 29, CI-driven instrumented screenshot capture

## Purpose
Create an automated screenshot pipeline that keeps public-facing app screenshots current with `main`. On every push to `main`, CI should launch the real app, seed demo mode, capture a fixed set of showcase screens in English on one phone size in both light and dark mode, publish them as CI artifacts, and publish stable “latest” copies to GitHub Pages so the README can display the current images without being rewritten on each run.

## Functional Requirements
- **FR-1**: The screenshot workflow shall run automatically on every push to `main`.
- **FR-2**: The workflow shall launch the runtime app, enter or seed demo mode through the app’s real behavior, and use demo-mode data as the source of truth for all captures.
- **FR-3**: The workflow shall capture exactly 8 screenshots per successful full run: Measurement List, Analysis, Photo, and Photo Compare, each in light mode and dark mode.
- **FR-4**: The workflow shall use one fixed phone configuration for all screenshots.
- **FR-5**: The workflow shall capture screenshots in English only for v1.
- **FR-6**: The workflow shall use a fixed public filename scheme based on screen and theme so the stable README image URLs do not change between runs.
- **FR-7**: The workflow shall upload all produced screenshots as CI artifacts for the triggering run.
- **FR-8**: The workflow shall publish the latest successful screenshot set to GitHub Pages under a stable path intended for README consumption.
- **FR-9**: The workflow should also publish best-effort versioned copies grouped by commit SHA in GitHub Pages storage.
- **FR-10**: The README integration shall be implemented once to reference the stable GitHub Pages “latest” image URLs, and normal CI runs shall not modify the README file.
- **FR-11**: The workflow shall attempt all configured screenshots even if one or more captures fail.
- **FR-12**: The workflow shall report which screenshots succeeded and which failed for the run.

## Non-Functional Requirements
- **NFR-1**: Screenshot generation shall be deterministic enough that equivalent UI on equivalent input produces stable public assets across `main` runs.
- **NFR-2**: The solution shall prefer the simplest operational approach over a more complex custom harness, as long as it uses the real running app.
- **NFR-3**: Failures in one capture target shall not prevent later targets from being attempted.
- **NFR-4**: The workflow shall use bounded waits and retries for slow app startup, demo-data seeding, and navigation steps, rather than indefinite waiting.
- **NFR-5**: The screenshot job is informational and asset-oriented for v1; it is not intended to block delivery solely because one or more screenshots failed.
- **NFR-6**: Published latest assets shall be safe for public display and must not depend on short-lived CI artifact URLs.
- **NFR-7**: The screenshot set shall remain limited to the defined v1 scope: one locale, one phone size, four screens, two themes.

## Android Technical Constraints
- **Lifecycle**: The capture flow shall run in a single portrait-oriented device profile for v1. Theme variants may be produced by separate runs or separate passes, whichever is simpler. If the app is slower than expected or a target screen is not ready, the workflow shall use bounded waits/retries and then mark only that screenshot as failed before continuing. No special rotation handling is required for v1.
- **Permissions**: No runtime permission prompts should appear during screenshot generation. The flow shall assume screenshot targets are chosen so that permissions are not required in the capture path.
- **Storage**: Screenshot outputs shall be stored as CI artifacts for each run and published to GitHub Pages as a stable “latest” set. Best-effort versioned copies by commit SHA are acceptable for historical access.

## Test Scenarios
| ID | Type | Scenario | Expected Result |
|----|------|----------|-----------------|
| T-1 | Instrumented/CI | Push a commit to `main` with the screenshot workflow enabled | CI starts the screenshot job automatically and begins capture without manual triggering |
| T-2 | Instrumented/CI | Run the workflow from a clean state and seed demo mode in the real app | The app reaches the expected demo-data state and captures the 4 target screens in both themes, producing 8 screenshots |
| T-3 | Instrumented/CI | Verify published output after a successful run | CI artifacts contain the generated screenshots, GitHub Pages updates the stable latest paths, and README image links still resolve without README file changes |
| T-4 | Instrumented/CI | Force one screenshot target to fail after earlier captures succeed | The workflow records that target as failed, continues attempting remaining targets, and still uploads all successfully generated screenshots plus failure status |
| T-5 | Instrumented/CI | Run two successful `main` builds on different commits | The stable latest paths are overwritten with the newest screenshots, and best-effort commit-SHA versioned copies are retained if enabled |
| T-6 | Instrumented/CI | Validate filename and theme mapping for the published set | Each public image uses the fixed filename scheme and uniquely maps to one screen-theme pair |
| T-7 | Instrumented/CI | Simulate slower startup or navigation in CI within configured bounds | The workflow waits or retries within limits, captures screenshots when readiness is achieved, and does not hang indefinitely |
| T-8 | Unit/Workflow Logic | Resolve summary/report data after a partial-success run | The run summary correctly lists succeeded and failed screenshot targets and exposes that information to CI output |

**Requirements elicitation complete.** The specification is ready for implementation planning.
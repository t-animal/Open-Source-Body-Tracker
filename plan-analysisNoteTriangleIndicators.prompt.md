## Plan: Analysis Note Triangle Indicators

Add note/photo-aware chart-point metadata in the analysis transform, then make `MetricLineChart` render per-point visuals by rule (`marker+triangle`, `triangle-only`, `none`, `marker-only`) and show note text in a tap tooltip using Vico marker interactions. This reuses existing analysis data flow, keeps chart ordering/filtering behavior unchanged, and avoids non-interactive `Decoration` for tooltip behavior.

**Steps**
1. Phase 1 (Data model) Update `AnalysisChartPoint` to carry note/photo metadata needed by rendering and interactions: `hasNote`, `hasPhoto`, and normalized note text for tooltip. This is the minimal extension that avoids extra repository calls. *blocks steps 2-9*
2. Phase 1 (Transform wiring) Update `buildMetricCharts` in `AnalysisTransform.kt` to populate new fields from `BodyMeasurement.note` and `BodyMeasurement.photoFilePath` while preserving existing metric value filtering and chronological ordering. *depends on 1*
3. Phase 1 (Preview/test compile fixes) Update all `AnalysisChartPoint(...)` call sites (including preview data in `AnalysisScreen.kt` and tests) for the new constructor shape. *depends on 1*
4. Phase 2 (Rendering rules) In `MetricLineChart.kt`, add a local visual-state classifier from chart point metadata implementing the confirmed matrix: note+photo -> marker+triangle, note-only -> triangle only, photo-only -> no marker, neither -> marker only. *depends on 2*
5. Phase 2 (Per-point marker visibility) Replace `LineCartesianLayer.PointProvider.single(...)` with a custom `PointProvider` (`getPoint(entry, ...)`) that returns a circle point only for states that require a marker and returns `null` for states that require no marker. This enforces “photo-only: no marker” and “note-only: no marker.” *depends on 4*
6. Phase 2 (Triangle drawing) Add a custom persistent marker (new helper in analysis components) that draws a small downward-pointing triangle if possible using material shapes above note targets with a fixed vertical gap from the point y-position. Wire it via `persistentMarkers` for all note x-values in each chart. *depends on 4*
7. Phase 2 (Selected marker coexistence) Merge persistent note-triangle markers with the existing selected-date persistent marker so selected-date behavior remains unchanged and note triangles still render. *depends on 5,6*
8. Phase 3 (Tap-to-tooltip behavior) Update `rememberTapSelectionMarkerController` and marker formatter usage so tap interactions still update `selectedDate`, and note taps also show tooltip text for the tapped note. Use Vico marker APIs (`CartesianMarkerController` + `DefaultCartesianMarker.ValueFormatter`) rather than `Decoration`, because `Decoration` is draw-only and non-interactive. *depends on 2,6*
9. Phase 3 (Tooltip content mapping) Add a stable map from x-value (epoch day) to note metadata inside `MetricLineChart` so marker/tooltip content resolves directly from tapped target x without extra state in `AnalysisScreen`. *depends on 2,8*
10. Phase 4 (Regression hardening) Keep axis formatting, y-range provider, zoom/scroll behavior, reorder/collapse behavior, and duration filtering untouched; verify no behavior drift from Phase 4.3 analysis baseline. *parallel with 8-9 after 5*
11. Phase 5 (Tests) Extend `AnalysisTransformTest` to assert note/photo metadata propagation and unchanged chronological order/range behavior. Keep existing tests for y-axis padding and duration filtering. *depends on 2*
12. Phase 5 (Manual interaction QA) Validate the four visual states and tooltip interaction in app runtime across durations and reordered cards, including tap behavior after scrolling/zooming. *depends on 7-10*

**Relevant files**
- `/home/tilman/projekte/AndroidStudio/OpenSourceBodyTracker/app/src/main/java/de/t_animal/opensourcebodytracker/feature/analysis/AnalysisModels.kt` — extend `AnalysisChartPoint` metadata fields.
- `/home/tilman/projekte/AndroidStudio/OpenSourceBodyTracker/app/src/main/java/de/t_animal/opensourcebodytracker/feature/analysis/AnalysisTransform.kt` — propagate note/photo metadata from `BodyMeasurement` into chart points.
- `/home/tilman/projekte/AndroidStudio/OpenSourceBodyTracker/app/src/main/java/de/t_animal/opensourcebodytracker/feature/analysis/components/MetricLineChart.kt` — custom point provider, note triangle persistent marker integration, tap controller/tooltip mapping.
- `/home/tilman/projekte/AndroidStudio/OpenSourceBodyTracker/app/src/main/java/de/t_animal/opensourcebodytracker/feature/analysis/AnalysisScreen.kt` — update preview `AnalysisChartPoint` constructors (and only add runtime state if tooltip anchoring requires it).
- `/home/tilman/projekte/AndroidStudio/OpenSourceBodyTracker/app/src/test/java/de/t_animal/opensourcebodytracker/feature/analysis/AnalysisTransformTest.kt` — metadata propagation and regression coverage.
- `/home/tilman/projekte/AndroidStudio/OpenSourceBodyTracker/app/src/main/java/de/t_animal/opensourcebodytracker/feature/measurements/components/MeasurementTable.kt` — visual/UX reference for note tooltip content behavior.

**Verification**
1. Run `./gradlew :app:compileDebugKotlin --console=plain` and confirm `BUILD SUCCESSFUL`.
2. Run `./gradlew :app:testDebugUnitTest --console=plain` and ensure `AnalysisTransformTest` passes with new assertions.
3. Run `./gradlew :app:lintDebug --console=plain` and `./gradlew ktlintCheck --console=plain` to verify lint/format compliance.
4. Manual scenario: entry with note+photo and metric value shows circle marker plus triangle above with visible gap and downward apex.
5. Manual scenario: entry with note-only and metric value shows triangle only (no circle marker).
6. Manual scenario: entry with photo-only and metric value shows no point marker.
7. Manual scenario: tap triangle on note entry shows note text tooltip and still updates selected date behavior.
8. Manual scenario: behavior remains correct after duration switch, chart reorder, collapse/expand, and horizontal scroll/zoom.

**Decisions**
- Confirmed UX matrix from user alignment: note+photo -> marker+triangle; note-only -> triangle only; photo-only -> no marker; neither -> marker only.
- Confirmed assumption: single measurement per day (`same-day collisions ignored for now`).
- Use Vico marker/controller APIs for tap tooltip behavior; do not rely on `Decoration` for interaction because `Decoration` is draw-only.
- Scope boundary: this plan annotates entries that already produce a chart point for that metric; synthetic annotation anchors for measurements with no value in a given metric chart are excluded in this iteration.
- HEAD context used for planning: current branch head is `28926f3c3f580a94bb34643bac81049886ec84a4` with subject `feat: add per-entry notes with csv export and list preview`, which aligns with reusing existing note tooltip patterns.

**Further Considerations**
1. If you later want note-only measurements with no metric value to appear in every chart, add a dedicated annotation series/layer anchored by x-value only (separate from metric point rendering) in a follow-up.

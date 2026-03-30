# Feature Concept: Sparkline Trend Indicators on Last Measurements Card

## Executive Summary

Users of the log screen currently see only a single static value per metric in the "last measurements" summary card, which provides no sense of direction or progress. This feature adds a small sparkline graph beneath each metric's current value, giving users an immediate visual sense of their trend without requiring them to navigate to the analysis screen.

## The "Why"

- **User Problem**: A single measurement number in isolation lacks context. Users cannot tell at a glance whether a metric is improving, worsening, or holding steady, which reduces the motivational value of opening the app.
- **Business Value**: Surfacing trend context directly on the log screen increases the perceived value of consistent logging and reduces the need to navigate away to the analysis screen for basic progress feedback.
- **Success Metric**: Reduction in navigation events from the log screen to the analysis screen for users who have at least two logged entries per metric, indicating that their trend curiosity is being satisfied earlier in the flow.

## User Journey

- **Persona**: A returning user who logs measurements regularly and opens the app to record a new entry or simply check in on their progress.
- **The "Happy Path"**:
  1. User opens the app and lands on the log screen.
  2. The "last measurements" summary card is visible at the top.
  3. For each active metric, the user sees the most recent value as before.
  4. Beneath that value, a compact sparkline displays the shape of their recent trend across up to 10 data points.
  5. If only one data point exists for a metric, no sparkline is shown for that metric.
  6. The user absorbs the trend at a glance and proceeds to log a new entry or leaves the screen satisfied.

## MVP Boundaries

- **In-Scope**:
  - A sparkline rendered below each metric's current value within the existing "last measurements" summary card.
  - Data window of up to the last 10 logged entries per metric.
  - Sparkline is displayed only when at least 2 data points exist for a metric.
  - The sparkline is read-only and non-interactive.

- **Out-of-Scope**:
  - Directional arrow or icon alternatives to the sparkline.
  - Sparklines in the log table rows.
  - Color-coded trend thresholds (e.g., red/green based on goal direction).
  - Any interactive behavior such as tap-to-expand or tooltip values.
  - Configurable date ranges for the sparkline window.
  - Sparklines on the analysis screen or any screen other than the log screen summary card.

## Open Questions

None.

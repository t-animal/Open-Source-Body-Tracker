# 💡 Feature Concept: Automatic Release Screenshot Pipeline

## Executive Summary
Create an automated CI pipeline that generates a consistent set of Android app screenshots for public-facing use on every push to `main`. The pipeline will use deterministic demo-mode data to capture four showcase screens in English, on one phone size, in both light and dark mode, then publish them as CI artifacts and to a stable location that the README can reference.

## The "Why"
- **User Problem**: Maintaining Play Store and README screenshots manually is repetitive, error-prone, and quickly drifts out of date as the UI evolves.
- **Business Value**: The app always has fresh, consistent visual assets for store presence and project presentation, with less manual release work and lower risk of outdated screenshots.
- **Success Metric**: Every push to `main` produces 8 current screenshots successfully, stores them as CI artifacts, and updates a stable published set so the README always shows the latest images.

## User Journey
- **Persona**: The app maintainer preparing and presenting the project through the Play Store listing and GitHub README.
- **The "Happy Path"**: A change lands on `main`, CI runs automatically, deterministic demo data is loaded, the app captures Measurement List, Analysis, Photo, and Photo Compare on one phone profile in light and dark mode, the 8 screenshots are uploaded as build artifacts, and the latest published copies become visible in the README.

## MVP Boundaries
- **In-Scope**: Automatic run on every push to `main`; deterministic demo-mode dataset; one device size; English only; light and dark mode variants; fixed screen set of Measurement List, Analysis, Photo, and Photo Compare; CI artifact upload; stable published location for the latest screenshots; README display of all 8 screenshots from that stable location.
- **Out-of-Scope**: Additional languages; tablet or multi-device layouts; onboarding, settings, or other screens; marketing overlays or captions; automatic Play Store publishing; visual regression approval workflows; screenshot generation for pull requests; manual content editing beyond referencing the generated latest images.

**Feature discovery complete.** The concept is now ready for technical breakdown. Click the **'Design Requirements'** button below to hand this over to the Requirements Engineer.
# Feature Concept: Import Backup

## Executive Summary
Allow users to restore a previously exported encrypted ZIP backup during the onboarding flow, so that switching devices or reinstalling the app does not mean losing historical body tracking data. An "Import Backup" button is added to the WelcomeScreen as a third option alongside "Try with Demo Data" and "Set Up My Profile."

## The "Why"
- **User Problem**: Users who switch phones, reinstall the app, or set up a new device have no way to restore their data. The app already exports encrypted ZIP backups, but there is no corresponding import path — the backup is effectively a dead end.
- **Business Value**: Data portability is a baseline expectation for an open-source health tracker. Without import, the export feature loses credibility, and users are discouraged from committing long-term data to the app.
- **Success Metric**: Successful backup restoration rate — the percentage of users who tap "Import Backup" and land on the dashboard with their data fully restored, without errors.

## Current Export Format
Exports are **AES-256 encrypted ZIP archives** (via Zip4j library) containing:
- `measurements.csv` — all body measurements (weight, body fat, circumferences, skinfolds, notes, photos paths)
- `profile.json` — user profile (sex, dateOfBirth, heightCm)
- `metadata.json` — export metadata (schemaVersion, exportedAt, measurementCount, imageCount)
- `measurement_photos/` — all photos associated with measurements

The archive is password-protected. Users set their export password in the Export Settings screen. The password is stored locally using Google Tink (AES256-GCM) with Android Keystore.

## User Journey
- **Persona**: An existing user who has previously tracked body metrics and exported an encrypted ZIP backup. They are now setting up the app on a new device or after a reinstall.
- **The "Happy Path"**:
  1. User opens the app for the first time (clean database) and sees the WelcomeScreen.
  2. User taps "Import Backup" (third button, alongside "Try with Demo Data" and "Set Up My Profile").
  3. The system opens the Android file picker, filtered to ZIP files.
  4. User selects their previously exported `.zip` backup file.
  5. The app prompts the user for the backup password.
  6. The app decrypts and extracts the ZIP, then parses `profile.json`, `measurements.csv`, `metadata.json`, and restores `measurement_photos/` into the app's storage.
  7. All data is written to the Room database — profile, measurements, and photo references.
  8. Profile setup is skipped entirely — the imported profile is adopted wholesale.
  9. User lands on the main dashboard with all historical data restored.

## Key Decisions Made
- **Full ZIP import**: The import reads the complete encrypted ZIP archive, restoring all contents (profile, measurements, photos, metadata).
- **Password input required**: Since exports are AES-256 encrypted, the user must enter their backup password before the archive can be read.
- **Onboarding only**: The import button appears exclusively on the WelcomeScreen when the database is clean. There is no import option from within the main app (settings, etc.) in this version.
- **Full profile restore**: The imported user profile replaces the need for profile setup. The user does not see the profile configuration screen after a successful import.
- **Minimal error handling**: If the import fails (wrong password, corrupt file, invalid format), surface whatever error information is trivially available. No investment in detailed diagnostics, retry logic, or partial-import recovery.
- **Relative photo paths in export**: The export will be changed to use relative paths (e.g., `measurement_photos/img_123.jpg`) instead of absolute device paths. On import, photos are extracted to the app's internal files directory and paths resolve naturally.
- **New IDs on import**: Imported measurements get new Room-assigned IDs rather than preserving original IDs from the CSV.
- **Inline password UX**: Password input is shown inline on the import screen (not a dialog), with action buttons below and a progress bar during import.

## MVP Boundaries

### In-Scope
- "Import Backup" button on WelcomeScreen (third option)
- Android file picker integration, filtered to ZIP files
- Password input dialog/screen for decrypting the archive
- Decrypting the ZIP archive using Zip4j (same library used for export)
- Parsing all archive contents: `profile.json`, `measurements.csv`, `metadata.json`, `measurement_photos/`
- Restoring photos to the app's internal storage
- Writing imported profile and measurements to the Room database
- Skipping profile setup after successful import
- Navigating to the main dashboard upon completion
- Basic error feedback (wrong password, invalid file, etc.) using trivially available information

### Out-of-Scope
- Import from within app settings (post-onboarding)
- Merge or conflict resolution with existing data
- Backup format migration or version negotiation beyond the current schema version 1
- Cloud-based backup and restore
- Partial import recovery
- Detailed error diagnostics or logging

## Open Questions
None. The scope is sufficiently defined for technical breakdown.

# Feature Specification: Import Backup

**Platform Context**: Jetpack Compose / Min SDK 26 / Target SDK 35

## Purpose
Allow new or returning users to restore all their body tracking data (measurements, photos, and profile) from a previously exported AES-256 encrypted ZIP archive during onboarding, so they can migrate between devices or recover from a fresh install without losing history.

## Functional Requirements

- **FR-1 (Import Screen Navigation)**: The user taps the new "Import Backup" button on the WelcomeScreen. This navigates directly to a new ImportBackupScreen (route: `import_backup`). The "Import Backup" button does not yet exist on WelcomeScreen and must be added.

- **FR-2 (File Selection)**: On the ImportBackupScreen, a "Select File" button launches the Android system file picker (ACTION_OPEN_DOCUMENT) filtered to ZIP files. The selected file name is displayed on screen after selection.

- **FR-3 (Password Entry & Import Gate)**: The password field is shown inline on the ImportBackupScreen (not a dialog). The field uses password masking by default with a visibility toggle. The "Import" button is disabled when either no file has been selected or the password field is empty. Both conditions must be satisfied to enable the button.

- **FR-4 (ZIP Decryption and Streaming Extraction)**: On tapping "Import", the app opens the selected file via its content URI and uses Zip4j to decrypt and stream entries directly from the archive. Instead of copying the entire ZIP to cache, each entry is read via Zip4j's streaming API and written directly to its target destination (database for JSON/CSV data, filesystem for photos). If Zip4j's streaming API does not support direct content URI input, the file may be copied to a temporary location first, but extraction should still stream entries individually rather than extracting all to a temp directory.

- **FR-5 (Profile Restoration)**: The app reads `profile.json` from the archive, parses the `sex` (Sex enum), `dateOfBirth` (LocalDate, YYYY-MM-DD), and `heightCm` (Float) fields, and inserts a UserProfile record into the database. This replaces the need for the ProfileSetupScreen.

- **FR-6 (Measurement Restoration)**: The app reads `measurements.csv` from the archive, parses each row into a MeasurementEntity. The CSV columns are: `id,dateEpochMillis,photoFilePath,weightKg,bodyFatPercent,neckCircumferenceCm,chestCircumferenceCm,waistCircumferenceCm,abdomenCircumferenceCm,hipCircumferenceCm,chestSkinfoldMm,abdomenSkinfoldMm,thighSkinfoldMm,tricepsSkinfoldMm,suprailiacSkinfoldMm,note`. The original `id` column is ignored; new IDs are auto-generated on insert. Nullable numeric fields may be empty strings. String fields (`photoFilePath`, `note`) may be quoted if they contain commas, quotes, or newlines; internal quotes are escaped as `""`. Photo paths in the CSV are treated as relative paths (see FR-9) and are resolved to the app's internal files directory.

- **FR-7 (Photo Restoration)**: The app copies the `measurement_photos/` directory from the extracted archive to the app's internal files directory (`context.filesDir/measurement_photos/`). Each measurement's photoPath is stored as the absolute path to the corresponding file in this directory.

- **FR-8 (Onboarding Completion & Navigation)**: On successful import, the app sets `onboardingCompleted = true` in `SettingsState` (via `PreferencesSettingsRepository`) and then navigates directly to the HomeScreen, clearing the back stack (popUpTo WelcomeScreen inclusive). The user skips ProfileSetupScreen entirely since the profile was restored.

- **FR-9 (Export Modification -- Relative Photo Paths)**: The existing ExportViewModel must be modified so that the `photoPath` column in `measurements.csv` writes relative paths (e.g., `measurement_photos/img_123.jpg`) instead of absolute device paths. The photo files are already correctly copied into the `measurement_photos/` folder in the ZIP; only the CSV path string needs to change.

- **FR-10 (Progress Indicator)**: During import, a linear progress bar is displayed on the ImportBackupScreen. The "Import" and "Cancel" buttons are disabled while import is in progress. No granular progress percentage is required; an indeterminate progress bar is acceptable.

- **FR-11 (Metadata Validation)**: The app reads `metadata.json` from the archive first, before processing any other entries. If `metadata.json` is missing or contains an unexpected/unsupported `schemaVersion`, the import aborts immediately with an inline error: "Unsupported backup format." The metadata contains: `schemaVersion` (Int), `archiveFileName` (String), `exportedAtEpochMillis` (Long), `exportedAtUtc` (String), `measurementCount` (Int), `imageCount` (Int), `missingImageCount` (Int). Currently only `schemaVersion: 1` is supported.

## Non-Functional Requirements

- **NFR-1 (Performance)**: Import must run on a background coroutine (Dispatchers.IO) and must not block the main thread. The ViewModel uses viewModelScope.launch to manage the coroutine.

- **NFR-2 (Storage Cleanup)**: If a temporary ZIP copy was needed (fallback for content URI), it must be deleted after import completes, whether successful or failed. The streaming approach minimizes temporary file usage.

- **NFR-3 (Data Integrity — Two-Phase Verification)**: The import proceeds in two phases with verification:
  1. **Database phase**: Profile and measurement insertion is wrapped in a Room transaction. During CSV parsing, for each measurement row that references a `photoFilePath`, the import verifies that the corresponding entry exists in the ZIP archive before inserting the row. If the photo entry is missing from the ZIP, the measurement's `photoFilePath` is set to null and a message appears on the screen informing the user that not all images are present in the zip-file.
  2. **File phase**: After the database transaction commits, photos are extracted from the ZIP to the filesystem. After all photos are written, a second verification pass checks that every non-null `photoFilePath` in the database resolves to an existing file on disk.
  3. **Post-import consistency**: If the verification in step 2 finds any missing files, or if any error occurs during the file phase, the import is considered failed (see NFR-5).

- **NFR-5 (Catastrophic Failure Recovery)**: If any unrecoverable error occurs after the database transaction has committed (e.g., photo files fail to write, post-import verification fails, or any unexpected exception), the app must: (1) show a long-duration Toast with a descriptive error message and (2) clear all app data (same method as the reset app button in demo mode on the measurement list screen). This ensures the app never enters a partially-imported, inconsistent state. On next launch, the user returns to a clean onboarding flow and can retry.

- **NFR-4 (Onboarding-Only)**: The import feature is only accessible from the WelcomeScreen. It is not available from the SettingsScreen or any other location. The database is assumed to be empty at import time.

## UI/UX Requirements

- **UX-1 (WelcomeScreen)**: A new "Import Backup" OutlinedButton is added to WelcomeScreen. Tapping it navigates directly to ImportBackupScreen.

- **UX-2 (ImportBackupScreen Layout)**: Top to bottom: screen title ("Import Backup"), "Select File" button (shows selected file name after selection), inline password TextField with visibility toggle (same as on the export screen), error message area (if applicable), horizontal row of "Cancel" and "Import" buttons (Import disabled until both file and password are provided), indeterminate LinearProgressIndicator (visible only during import).

- **UX-3 (State Preservation)**: The password field value and selected file URI survive configuration changes (rotation) via ViewModel state.

- **UX-4 (Success Feedback)**: On success, the user is navigated to HomeScreen immediately. No separate success screen or toast is required.

- **UX-5 (Cancel Behavior)**: The "Cancel" button navigates back to WelcomeScreen. If import is in progress, the buttons are disabled and the user must wait for it to complete or fail.

## Data Requirements

- **DR-1 (Archive Contents)**: The import expects a ZIP archive containing at minimum `measurements.csv` and `profile.json`. Optionally: `metadata.json` and `measurement_photos/` directory.

- **DR-2 (CSV Schema)**: The CSV must have the header: `id,dateEpochMillis,photoFilePath,weightKg,bodyFatPercent,neckCircumferenceCm,chestCircumferenceCm,waistCircumferenceCm,abdomenCircumferenceCm,hipCircumferenceCm,chestSkinfoldMm,abdomenSkinfoldMm,thighSkinfoldMm,tricepsSkinfoldMm,suprailiacSkinfoldMm,note`. The `id` column is present but ignored on import. Nullable numeric fields may be empty. String fields use CSV quoting with `""` escape for embedded quotes.

- **DR-3 (Profile JSON Schema)**: Must contain fields: `sex` (String, Sex enum value), `dateOfBirth` (String, YYYY-MM-DD), `heightCm` (Float).

- **DR-4 (Photo Path Resolution)**: The CSV `photoPath` values are relative (e.g., `measurement_photos/img_123.jpg`). On import, these are resolved to absolute paths under `context.filesDir` (e.g., `/data/data/com.example.bodytracker/files/measurement_photos/img_123.jpg`).

## Error Scenarios and Handling

- **ERR-1 (Wrong Password)**: Zip4j throws ZipException when decryption fails. Display inline error text: "Incorrect password or corrupted file."

- **ERR-2 (Invalid/Corrupted Archive)**: If the selected file is not a valid ZIP or extraction fails for any reason, display inline error text: "Could not read backup file."

- **ERR-3 (Missing/Invalid Metadata)**: If `metadata.json` is missing or has an unexpected version, the import aborts immediately with inline error: "Unsupported backup format."

- **ERR-3b (Missing Required Files)**: If `measurements.csv` or `profile.json` is not found in the archive, display inline error text: "Backup file is incomplete."

- **ERR-4 (CSV Parse Failure)**: If a row in the CSV cannot be parsed (malformed data), skip that row and continue. After import, no special notification is given for skipped rows.

- **ERR-5 (Insufficient Storage)**: If a write fails due to storage, display inline error text: "Not enough storage space."

- **ERR-6 (General Failure)**: Any other exception during import displays: "Import failed: [exception message]".

- **ERR-7 (Pre-Commit Error Recovery)**: For errors that occur before the database transaction commits (ERR-1 through ERR-6), the password field remains populated, the file selection is retained, and the user can retry by tapping "Import" again. The user can also tap "Cancel" to return to WelcomeScreen.

- **ERR-8 (Post-Commit Catastrophic Failure)**: If an error occurs after the database transaction has committed (photo write failures, post-import file verification failure, or any unexpected exception), the app shows a long Toast with the error details, clears all app data (database, files, preferences), and terminates. See NFR-5.

## Android Technical Constraints

- **Lifecycle**: The ImportBackupScreen ViewModel holds all state (selected file URI, password, import progress, error messages). Configuration changes do not interrupt an in-progress import. The coroutine is scoped to viewModelScope.
- **Permissions**: No additional manifest permissions are required. File access uses the Storage Access Framework (ACTION_OPEN_DOCUMENT) which provides temporary URI access grants. Internal files directory requires no permissions.
- **Storage**: Imported data is stored locally in Room (measurements, profile) and the app's internal files directory (photos). No remote/network operations.

## Acceptance Criteria / Test Scenarios

| ID | Type | Scenario | Preconditions | Action | Expected Result |
|----|------|----------|---------------|--------|-----------------|
| T-1 | Unit | Successful full import | Valid encrypted ZIP with profile.json, measurements.csv (3 rows), 2 photos, and metadata.json in cache | Call import use case with correct password | Profile inserted in DB, 3 measurements inserted with new IDs, 2 photo files exist in filesDir/measurement_photos/, temp files cleaned up |
| T-2 | Unit | Wrong password | Valid encrypted ZIP in cache | Call import use case with wrong password | ZipException caught, UI state contains error "Incorrect password or corrupted file", database remains empty |
| T-3 | Unit | Missing profile.json | Encrypted ZIP containing only measurements.csv | Call import use case with correct password | UI state contains error "Backup file is incomplete", database remains empty |
| T-4 | Unit | Malformed CSV row | ZIP with profile.json and measurements.csv where row 2 of 3 has non-numeric weight value | Call import use case with correct password | Profile inserted, 2 valid measurements inserted (malformed row skipped), no error displayed |
| T-5 | Instrumented | End-to-end import from WelcomeScreen | App freshly installed, valid backup file accessible via file picker | Tap "Import Backup", select file, enter password, tap "Import" | App navigates to HomeScreen, profile data visible in Settings, measurements visible in History |
| T-6 | Unit | Export writes relative photo paths | 2 measurements in DB, one with photoPath pointing to existing file | Call export | CSV photoPath column contains relative path (measurement_photos/filename.jpg), not absolute path |

## Relevant Files

- `app/src/main/java/com/example/bodytracker/ui/screens/welcome/WelcomeScreen.kt` -- entry point, "Import Backup" button
- `app/src/main/java/com/example/bodytracker/ui/navigation/NavGraph.kt` -- needs new route for ImportBackupScreen
- `app/src/main/java/com/example/bodytracker/ui/navigation/Screen.kt` -- needs new Screen.ImportBackup entry
- `app/src/main/java/com/example/bodytracker/ui/screens/settings/ExportViewModel.kt` -- needs FR-9 modification for relative paths
- `app/src/main/java/com/example/bodytracker/data/model/Measurement.kt` -- Measurement entity schema
- `app/src/main/java/com/example/bodytracker/data/model/UserProfile.kt` -- UserProfile entity schema
- `app/src/main/java/com/example/bodytracker/data/dao/MeasurementDao.kt` -- DAO for measurement insertion
- `app/src/main/java/com/example/bodytracker/data/dao/UserProfileDao.kt` -- DAO for profile insertion
- `app/src/main/java/com/example/bodytracker/data/repository/MeasurementRepository.kt` -- repository layer
- `app/src/main/java/com/example/bodytracker/data/repository/UserProfileRepository.kt` -- repository layer

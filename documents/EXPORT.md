# Export & Import

## Archive Format

Exports produce a **password-protected ZIP** file (AES-256 encryption via Zip4j).

### Archive Contents

```
bodytracker_export_YYYY-MM-dd_HH-mm-ss.zip
├── metadata.json
├── profile.json
├── measurements.csv
└── measurement_photos/
    └── <photo-filename>.jpg  (one per measurement with a photo)
```

### metadata.json

```json
{
  "schemaVersion": 1,
  "archiveFileName": "bodytracker_export_2026-03-09_14-30-00.zip",
  "exportedAtEpochMillis": 1741531800000,
  "exportedAtUtc": "2026-03-09T13:30:00Z",
  "measurementCount": 42,
  "imageCount": 10,
  "missingImageCount": 0
}
```

### profile.json

```json
{
  "sex": "Male",
  "dateOfBirth": "1990-05-15",
  "heightCm": 180.0
}
```

### measurements.csv

CSV headers (in order):

```
id, dateEpochMillis, photoFilePath, weightKg, bodyFatPercent,
neckCircumferenceCm, chestCircumferenceCm, waistCircumferenceCm,
abdomenCircumferenceCm, hipCircumferenceCm, chestSkinfoldMm,
abdomenSkinfoldMm, thighSkinfoldMm, tricepsSkinfoldMm,
suprailiacSkinfoldMm, note
```

- `photoFilePath` is the relative path within the archive (e.g., `measurement_photos/abc123.jpg`), empty string if no photo.
- Nullable numeric fields are empty strings when absent.

### Photos

Photos are stored in the `measurement_photos/` directory within the archive, mirroring the app's internal storage path (`PhotoStorageContract.PERSISTED_PHOTOS_DIRECTORY`).

---

## File Naming

Format: `bodytracker_export_YYYY-MM-dd_HH-mm-ss.zip`

Timestamp uses the device's local timezone.

---

## Retention

Only the **two most recent** export archives are kept in the selected folder. Older archives are deleted on each export.

---

## Password Storage

The export password is encrypted at rest using AES-GCM with a key from Android Keystore. The ciphertext is persisted in DataStore via `ExportPasswordRepository`.

---

## Manual Export

The Export Settings screen (`settings/export`) provides an "Export Now" button. Validation before export:
1. Device storage export must be enabled
2. A folder must be selected
3. A password must be entered

Errors: `EnableDeviceStorage`, `SelectFolder`, `EnterPassword`.

---

## Automatic Export

Scheduled via WorkManager (`AutomaticExportScheduler`). Runs at **3:00 AM** local time daily when all conditions are met:

- `automaticExportEnabled` is true
- `exportToDeviceStorageEnabled` is true
- A folder URI is configured
- Data has changed since the last export (the pending flag is set by `SetAutomaticExportPendingUseCase` on measurement save/edit/delete)

A foreground notification is shown during export.

---

## Import

Import is accessible from the onboarding welcome screen (`onboarding/import`).

### Flow

1. User selects a `.zip` file
2. User enters the archive password
3. App validates the archive (password, schema version, file integrity)
4. App extracts and restores: profile, all measurements, photos
5. On success: marks onboarding as completed, disables demo mode, navigates to `measurements`

### Error Handling

**Recoverable errors** (user can retry):
- Wrong password
- Unsupported schema version
- Corrupted or missing files within the archive

**Catastrophic errors** (app state may be inconsistent):
- Database write failure during restoration

On catastrophic failure, the app offers a reset option.

---

## Key Source Files

- `domain/export/ExportToFilesystemUseCase.kt` — Export orchestration
- `domain/export/ExportDocumentsCreator.kt` — CSV/JSON generation, CSV headers
- `domain/export/AutomaticExportScheduler.kt` — 3 AM scheduling via WorkManager
- `domain/export/SetAutomaticExportPendingUseCase.kt` — Marks data as changed
- `domain/backup/BackupModels.kt` — `BackupMetadata` and `BackupProfile` serialization models
- `domain/importbackup/ImportBackupUseCase.kt` — Import orchestration
- `domain/importbackup/MeasurementCsvParser.kt` — CSV parsing on import
- `data/export/ExportArchiveWriter.kt` — ZIP creation
- `data/export/ExportPasswordCrypto.kt` — AES-GCM password encryption
- `data/export/DocumentTreeExportStorage.kt` — SAF folder access, archive retention

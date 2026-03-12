package de.t_animal.opensourcebodytracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import de.t_animal.opensourcebodytracker.core.export.AutomaticExportWorker
import de.t_animal.opensourcebodytracker.data.export.AndroidExportDocumentTreeStorage
import de.t_animal.opensourcebodytracker.data.export.ExportArchiveWriter
import de.t_animal.opensourcebodytracker.data.export.ExportDocumentTreeStorage
import de.t_animal.opensourcebodytracker.domain.export.ExportPhotoCollector
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordCrypto
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.domain.export.InternalStorageExportPhotoCollector
import de.t_animal.opensourcebodytracker.data.export.KeystoreExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.export.Zip4jExportArchiveWriter
import de.t_animal.opensourcebodytracker.data.measurements.AppDatabase
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.measurements.RoomMeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.profile.PreferencesProfileRepository
import de.t_animal.opensourcebodytracker.core.notifications.ReminderAlarmScheduler
import de.t_animal.opensourcebodytracker.core.notifications.ReminderNotificationPoster
import de.t_animal.opensourcebodytracker.data.settings.PreferencesSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.PreferencesUiSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.UiSettingsRepository
import de.t_animal.opensourcebodytracker.domain.demodata.DemoDataMeasurementSeriesGenerator
import de.t_animal.opensourcebodytracker.domain.demodata.DemoDataPhotoSeeder
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
import de.t_animal.opensourcebodytracker.domain.export.AutomaticExportScheduler
import de.t_animal.opensourcebodytracker.domain.export.ExportToFilesystemUseCase
import de.t_animal.opensourcebodytracker.domain.export.ExportDocumentsCreator
import de.t_animal.opensourcebodytracker.domain.export.SetAutomaticExportPendingUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.MeasurementSaveValidator
import de.t_animal.opensourcebodytracker.domain.measurements.MeasurementSaver
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsCalculator

class AppContainer(appContext: Context) {
    private val applicationContext = appContext.applicationContext

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // TODO: Can this be deferred until the first notification is posted? No need to create channels if user never enables reminders or automatic export.
        // In any case this implementation and the one from ReminderNotificationPoster should be held in the same file to avoid inconsistencies and duplicate channels.
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
            ?: return

        val exportChannel = NotificationChannel(
            AutomaticExportWorker.EXPORT_NOTIFICATION_CHANNEL_ID,
            "Automatic Export",
            NotificationManager.IMPORTANCE_LOW,
        )
        manager.createNotificationChannel(exportChannel)
    }

    val profileRepository: ProfileRepository by lazy {
        PreferencesProfileRepository(applicationContext)
    }

    val settingsRepository: SettingsRepository by lazy {
        PreferencesSettingsRepository(applicationContext)
    }

    val uiSettingsRepository: UiSettingsRepository by lazy {
        PreferencesUiSettingsRepository(applicationContext)
    }

    val exportPasswordRepository: ExportPasswordRepository by lazy {
        KeystoreExportPasswordRepository(
            context = applicationContext,
            crypto = ExportPasswordCrypto(applicationContext),
        )
    }

    val exportDocumentTreeStorage: ExportDocumentTreeStorage by lazy {
        AndroidExportDocumentTreeStorage(applicationContext)
    }

    private val exportArchiveWriter: ExportArchiveWriter by lazy {
        Zip4jExportArchiveWriter()
    }

    private val exportDocumentsCreator: ExportDocumentsCreator by lazy {
        ExportDocumentsCreator()
    }

    private val exportPhotoCollector: ExportPhotoCollector by lazy {
        InternalStorageExportPhotoCollector(internalPhotoStorage)
    }

    val exportToFilesystemUseCase: ExportToFilesystemUseCase by lazy {
        ExportToFilesystemUseCase(
            measurementRepository = measurementRepository,
            profileRepository = profileRepository,
            exportStorage = exportDocumentTreeStorage,
            exportArchiveWriter = exportArchiveWriter,
            exportDocumentsCreator = exportDocumentsCreator,
            exportPhotoCollector = exportPhotoCollector,
        )
    }

    val setAutomaticExportPendingUseCase: SetAutomaticExportPendingUseCase by lazy {
        SetAutomaticExportPendingUseCase(
            settingsRepository = settingsRepository,
        )
    }

    val automaticExportScheduler: AutomaticExportScheduler by lazy {
        AutomaticExportScheduler(
            workManager = WorkManager.getInstance(applicationContext),
            settingsRepository = settingsRepository,
        )
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "body_tracker.db")
            .build()
    }

    val measurementRepository: MeasurementRepository by lazy {
        RoomMeasurementRepository(database.measurementDao())
    }

    val internalPhotoStorage: InternalPhotoStorage by lazy {
        InternalPhotoStorage(applicationContext)
    }

    val reminderNotificationPoster: ReminderNotificationPoster by lazy {
        ReminderNotificationPoster(applicationContext)
    }

    val reminderAlarmScheduler: ReminderAlarmScheduler by lazy {
        ReminderAlarmScheduler(applicationContext)
    }

    private val derivedMetricsCalculator: DerivedMetricsCalculator by lazy {
        DerivedMetricsCalculator()
    }

    val calculateMeasurementDerivedMetricsUseCase: CalculateMeasurementDerivedMetricsUseCase by lazy {
        CalculateMeasurementDerivedMetricsUseCase(derivedMetricsCalculator)
    }

    private val demoDataMeasurementSeriesGenerator: DemoDataMeasurementSeriesGenerator by lazy {
        DemoDataMeasurementSeriesGenerator()
    }

    private val demoDataPhotoSeeder: DemoDataPhotoSeeder by lazy {
        DemoDataPhotoSeeder(
            measurementRepository = measurementRepository,
            photoStorage = internalPhotoStorage,
            assetManager = applicationContext.assets,
        )
    }

    val generateDemoDataUseCase: GenerateDemoDataUseCase by lazy {
        GenerateDemoDataUseCase(
            measurementRepository = measurementRepository,
            demoDataMeasurementSeriesGenerator = demoDataMeasurementSeriesGenerator,
            demoDataPhotoSeeder = demoDataPhotoSeeder,
        )
    }

    private val measurementSaveValidator: MeasurementSaveValidator by lazy {
        MeasurementSaveValidator()
    }

    private val measurementSaver: MeasurementSaver by lazy {
        MeasurementSaver(
            repository = measurementRepository,
            photoStorage = internalPhotoStorage,
        )
    }

    val saveMeasurementUseCase: SaveMeasurementUseCase by lazy {
        SaveMeasurementUseCase(
            validator = measurementSaveValidator,
            saver = measurementSaver,
            setAutomaticExportPendingUseCase = setAutomaticExportPendingUseCase,
        )
    }

    val deleteMeasurementUseCase: DeleteMeasurementUseCase by lazy {
        DeleteMeasurementUseCase(
            repository = measurementRepository,
            photoStorage = internalPhotoStorage,
            setAutomaticExportPendingUseCase = setAutomaticExportPendingUseCase,
        )
    }
}

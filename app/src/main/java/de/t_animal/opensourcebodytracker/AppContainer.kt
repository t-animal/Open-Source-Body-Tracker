package de.t_animal.opensourcebodytracker

import android.content.Context
import androidx.room.Room
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordCrypto
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.export.KeystoreExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.measurements.AppDatabase
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.measurements.RoomMeasurementRepository
import de.t_animal.opensourcebodytracker.data.photos.InternalPhotoStorage
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.profile.PreferencesProfileRepository
import de.t_animal.opensourcebodytracker.core.notifications.ReminderAlarmScheduler
import de.t_animal.opensourcebodytracker.core.notifications.ReminderNotificationPoster
import de.t_animal.opensourcebodytracker.data.settings.PreferencesSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.demodata.DemoDataMeasurementSeriesGenerator
import de.t_animal.opensourcebodytracker.domain.demodata.DemoDataPhotoSeeder
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.DeleteMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.measurements.MeasurementSaveValidator
import de.t_animal.opensourcebodytracker.domain.measurements.MeasurementSaver
import de.t_animal.opensourcebodytracker.domain.measurements.SaveMeasurementUseCase
import de.t_animal.opensourcebodytracker.domain.metrics.CalculateMeasurementDerivedMetricsUseCase
import de.t_animal.opensourcebodytracker.domain.metrics.DerivedMetricsCalculator

class AppContainer(appContext: Context) {
    private val applicationContext = appContext.applicationContext

    val profileRepository: ProfileRepository by lazy {
        PreferencesProfileRepository(applicationContext)
    }

    val settingsRepository: SettingsRepository by lazy {
        PreferencesSettingsRepository(applicationContext)
    }

    val exportPasswordRepository: ExportPasswordRepository by lazy {
        KeystoreExportPasswordRepository(
            context = applicationContext,
            crypto = ExportPasswordCrypto(applicationContext),
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
        )
    }

    val deleteMeasurementUseCase: DeleteMeasurementUseCase by lazy {
        DeleteMeasurementUseCase(
            repository = measurementRepository,
            photoStorage = internalPhotoStorage,
        )
    }
}

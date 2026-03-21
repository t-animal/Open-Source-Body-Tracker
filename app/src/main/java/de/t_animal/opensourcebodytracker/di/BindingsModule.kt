package de.t_animal.opensourcebodytracker.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.t_animal.opensourcebodytracker.data.export.AndroidExportDocumentTreeStorage
import de.t_animal.opensourcebodytracker.data.export.ExportDocumentTreeStorage
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.export.KeystoreExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.export.Zip4jExportArchiveWriter
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.measurements.RoomMeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.PreferencesProfileRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.settings.ExportSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.PreferencesExportSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.PreferencesGeneralSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.PreferencesMeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.PreferencesReminderSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.ReminderSettingsRepository
import de.t_animal.opensourcebodytracker.data.uisettings.PreferencesUiSettingsRepository
import de.t_animal.opensourcebodytracker.data.uisettings.UiSettingsRepository
import de.t_animal.opensourcebodytracker.data.export.ExportArchiveWriter
import de.t_animal.opensourcebodytracker.domain.export.ExportPhotoCollector
import de.t_animal.opensourcebodytracker.domain.export.InternalStorageExportPhotoCollector
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {

    @Binds
    @Singleton
    abstract fun bindMeasurementRepository(impl: RoomMeasurementRepository): MeasurementRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: PreferencesProfileRepository): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindUiSettingsRepository(impl: PreferencesUiSettingsRepository): UiSettingsRepository

    @Binds
    @Singleton
    abstract fun bindMeasurementSettingsRepository(
        impl: PreferencesMeasurementSettingsRepository,
    ): MeasurementSettingsRepository

    @Binds
    @Singleton
    abstract fun bindReminderSettingsRepository(
        impl: PreferencesReminderSettingsRepository,
    ): ReminderSettingsRepository

    @Binds
    @Singleton
    abstract fun bindExportSettingsRepository(
        impl: PreferencesExportSettingsRepository,
    ): ExportSettingsRepository

    @Binds
    @Singleton
    abstract fun bindGeneralSettingsRepository(
        impl: PreferencesGeneralSettingsRepository,
    ): GeneralSettingsRepository

    @Binds
    @Singleton
    abstract fun bindExportPasswordRepository(impl: KeystoreExportPasswordRepository): ExportPasswordRepository

    @Binds
    @Singleton
    abstract fun bindExportDocumentTreeStorage(impl: AndroidExportDocumentTreeStorage): ExportDocumentTreeStorage

    @Binds
    abstract fun bindExportArchiveWriter(impl: Zip4jExportArchiveWriter): ExportArchiveWriter

    @Binds
    abstract fun bindExportPhotoCollector(impl: InternalStorageExportPhotoCollector): ExportPhotoCollector
}

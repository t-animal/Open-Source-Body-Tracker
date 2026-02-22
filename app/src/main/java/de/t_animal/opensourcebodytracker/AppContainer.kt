package de.t_animal.opensourcebodytracker

import android.content.Context
import androidx.room.Room
import de.t_animal.opensourcebodytracker.data.measurements.AppDatabase
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementRepository
import de.t_animal.opensourcebodytracker.data.measurements.RoomMeasurementRepository
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.profile.PreferencesProfileRepository

class AppContainer(appContext: Context) {
    private val applicationContext = appContext.applicationContext

    val profileRepository: ProfileRepository by lazy {
        PreferencesProfileRepository(applicationContext)
    }

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "body_tracker.db").build()
    }

    val measurementRepository: MeasurementRepository by lazy {
        RoomMeasurementRepository(database.measurementDao())
    }
}

package de.t_animal.opensourcebodytracker.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.t_animal.opensourcebodytracker.data.measurements.AppDatabase
import de.t_animal.opensourcebodytracker.data.measurements.MeasurementDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "body_tracker.db")
            .build()

    @Provides
    fun provideMeasurementDao(database: AppDatabase): MeasurementDao =
        database.measurementDao()
}

package de.t_animal.opensourcebodytracker.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.t_animal.opensourcebodytracker.NoOpScreenshotModeOrchestrator
import de.t_animal.opensourcebodytracker.ScreenshotModeOrchestrator

@Module
@InstallIn(SingletonComponent::class)
abstract class ScreenshotModeModule {
    @Binds
    abstract fun bindScreenshotModeOrchestrator(impl: NoOpScreenshotModeOrchestrator): ScreenshotModeOrchestrator
}

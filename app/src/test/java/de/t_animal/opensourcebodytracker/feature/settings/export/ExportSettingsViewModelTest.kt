package de.t_animal.opensourcebodytracker.feature.settings.export

import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportSettingsViewModelTest {
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onSaveClicked_showsErrorAndPersistsNothing_whenExportEnabledAndFolderMissing() = runTest {
        val settingsRepository = FakeSettingsRepository(defaultSettingsState())
        val passwordRepository = FakeExportPasswordRepository()
        val viewModel = ExportSettingsViewModel(settingsRepository, passwordRepository)

        advanceUntilIdle()

        viewModel.onExportToDeviceStorageEnabledChanged(true)
        viewModel.onExportPasswordChanged("super-secret")
        viewModel.onSaveClicked()

        advanceUntilIdle()

        assertEquals("Select an export folder", viewModel.uiState.value.errorMessage)
        assertEquals(0, settingsRepository.saveCalls)
        assertEquals(0, passwordRepository.saveCalls)
    }

    @Test
    fun onSaveClicked_showsErrorAndPersistsNothing_whenExportEnabledAndPasswordMissing() = runTest {
        val settingsRepository = FakeSettingsRepository(defaultSettingsState())
        val passwordRepository = FakeExportPasswordRepository()
        val viewModel = ExportSettingsViewModel(settingsRepository, passwordRepository)

        advanceUntilIdle()

        viewModel.onExportToDeviceStorageEnabledChanged(true)
        viewModel.onExportFolderSelected("content://example/tree/export")
        viewModel.onSaveClicked()

        advanceUntilIdle()

        assertEquals("Enter an export password", viewModel.uiState.value.errorMessage)
        assertEquals(0, settingsRepository.saveCalls)
        assertEquals(0, passwordRepository.saveCalls)
    }

    @Test
    fun onSaveClicked_persistsSettingsAndPasswordAndEmitsSaved_whenInputValid() = runTest {
        val settingsRepository = FakeSettingsRepository(defaultSettingsState())
        val passwordRepository = FakeExportPasswordRepository()
        val viewModel = ExportSettingsViewModel(settingsRepository, passwordRepository)
        val emittedEvents = mutableListOf<ExportSettingsEvent>()
        val collectJob = launch {
            viewModel.events.collect { event ->
                emittedEvents.add(event)
            }
        }

        advanceUntilIdle()

        val selectedFolder = "content://example/tree/export"
        val selectedPassword = "super-secret"
        viewModel.onExportToDeviceStorageEnabledChanged(true)
        viewModel.onExportFolderSelected(selectedFolder)
        viewModel.onExportPasswordChanged(selectedPassword)
        viewModel.onSaveClicked()

        advanceUntilIdle()

        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(1, settingsRepository.saveCalls)
        assertEquals(1, passwordRepository.saveCalls)
        assertTrue(settingsRepository.savedSettings.exportToDeviceStorageEnabled)
        assertEquals(selectedFolder, settingsRepository.savedSettings.exportFolderUri)
        assertEquals(selectedPassword, passwordRepository.savedPassword)
        assertTrue(emittedEvents.contains(ExportSettingsEvent.Saved))

        collectJob.cancel()
    }
}

private class FakeSettingsRepository(
    initial: SettingsState,
) : SettingsRepository {
    private val _settingsFlow = MutableStateFlow(initial)
    override val settingsFlow: Flow<SettingsState> = _settingsFlow

    var saveCalls: Int = 0
    var savedSettings: SettingsState = initial

    override suspend fun saveSettings(settings: SettingsState) {
        saveCalls += 1
        savedSettings = settings
        _settingsFlow.value = settings
    }
}

private class FakeExportPasswordRepository(
    private var initialPassword: String? = null,
) : ExportPasswordRepository {
    var saveCalls: Int = 0
    var savedPassword: String? = initialPassword

    override suspend fun getPassword(): String? = initialPassword

    override suspend fun savePassword(password: String?) {
        saveCalls += 1
        savedPassword = password
    }
}

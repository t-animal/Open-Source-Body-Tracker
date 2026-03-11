package de.t_animal.opensourcebodytracker.feature.settings.export

import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.ExportActionError
import de.t_animal.opensourcebodytracker.domain.export.ExportActionResult
import de.t_animal.opensourcebodytracker.domain.export.ExportExecutionCommand
import de.t_animal.opensourcebodytracker.domain.export.ExportNowUseCase
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
import org.junit.Assert.assertFalse
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
        val exportNowUseCase = FakeExportNowUseCase()
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            exportNowUseCase,
        )

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
        val exportNowUseCase = FakeExportNowUseCase()
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            exportNowUseCase,
        )

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
        val exportNowUseCase = FakeExportNowUseCase()
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            exportNowUseCase,
        )
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

    @Test
    fun onExportNowClicked_showsErrorAndSkipsStorage_whenFolderMissing() = runTest {
        val settingsRepository = FakeSettingsRepository(defaultSettingsState())
        val passwordRepository = FakeExportPasswordRepository()
        val exportNowUseCase = FakeExportNowUseCase(
            nextResult = ExportActionResult.Failure(
                ExportActionError.Validation(de.t_animal.opensourcebodytracker.domain.export.ExportValidationError.SelectFolder),
            ),
        )
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            exportNowUseCase,
        )

        advanceUntilIdle()

        viewModel.onExportToDeviceStorageEnabledChanged(true)
        viewModel.onExportPasswordChanged("super-secret")
        viewModel.onExportNowClicked()

        advanceUntilIdle()

        assertEquals("Select an export folder", viewModel.uiState.value.errorMessage)
        assertEquals(1, exportNowUseCase.calls)
        assertFalse(viewModel.uiState.value.isExporting)
    }

    @Test
    fun onExportNowClicked_usesUnsavedStateAndShowsSuccess_whenWriteSucceeds() = runTest {
        val settingsRepository = FakeSettingsRepository(defaultSettingsState())
        val passwordRepository = FakeExportPasswordRepository()
        val exportNowUseCase = FakeExportNowUseCase(
            nextResult = ExportActionResult.Success("bodytracker_export_2026-03-11_08-30-45_123.zip"),
        )
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            exportNowUseCase,
        )

        advanceUntilIdle()

        val selectedFolder = "content://example/tree/export"
        val selectedPassword = "super-secret"
        viewModel.onExportToDeviceStorageEnabledChanged(true)
        viewModel.onExportFolderSelected(selectedFolder)
        viewModel.onExportPasswordChanged(selectedPassword)
        viewModel.onExportNowClicked()

        advanceUntilIdle()

        assertEquals(1, exportNowUseCase.calls)
        assertEquals(selectedFolder, exportNowUseCase.lastCommand?.exportFolderUri)
        assertEquals(selectedPassword, exportNowUseCase.lastCommand?.exportPassword)
        assertEquals(
            "Export archive created: bodytracker_export_2026-03-11_08-30-45_123.zip",
            viewModel.uiState.value.statusMessage,
        )
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onExportNowClicked_showsError_whenPermissionIsDenied() = runTest {
        val settingsRepository = FakeSettingsRepository(defaultSettingsState())
        val passwordRepository = FakeExportPasswordRepository()
        val exportNowUseCase = FakeExportNowUseCase(
            nextResult = ExportActionResult.Failure(ExportActionError.PermissionDenied),
        )
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            exportNowUseCase,
        )

        advanceUntilIdle()

        viewModel.onExportToDeviceStorageEnabledChanged(true)
        viewModel.onExportFolderSelected("content://example/tree/export")
        viewModel.onExportPasswordChanged("super-secret")
        viewModel.onExportNowClicked()

        advanceUntilIdle()

        assertEquals(
            "Export folder permission was lost. Select folder again",
            viewModel.uiState.value.errorMessage,
        )
        assertNull(viewModel.uiState.value.statusMessage)
        assertFalse(viewModel.uiState.value.isExporting)
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

private class FakeExportNowUseCase(
    private val nextResult: ExportActionResult = ExportActionResult.Success(
        "bodytracker_export_2026-03-11_08-30-45_123.zip",
    ),
) : ExportNowUseCase {
    var calls: Int = 0
    var lastCommand: ExportExecutionCommand? = null

    override suspend fun invoke(command: ExportExecutionCommand): ExportActionResult {
        calls += 1
        lastCommand = command
        return nextResult
    }
}

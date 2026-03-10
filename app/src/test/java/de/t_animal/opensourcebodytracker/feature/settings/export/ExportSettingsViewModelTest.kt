package de.t_animal.opensourcebodytracker.feature.settings.export

import de.t_animal.opensourcebodytracker.core.model.SettingsState
import de.t_animal.opensourcebodytracker.core.model.defaultSettingsState
import de.t_animal.opensourcebodytracker.data.export.ExportDocumentTreeStorage
import de.t_animal.opensourcebodytracker.data.export.ExportStorageError
import de.t_animal.opensourcebodytracker.data.export.ExportStorageResult
import de.t_animal.opensourcebodytracker.data.export.ExportTreeFile
import de.t_animal.opensourcebodytracker.data.export.ExportPasswordRepository
import de.t_animal.opensourcebodytracker.data.settings.SettingsRepository
import de.t_animal.opensourcebodytracker.domain.export.CreateLocalExportTestFileUseCase
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
        val storage = FakeExportDocumentTreeStorage()
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            CreateLocalExportTestFileUseCase(storage),
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
        val storage = FakeExportDocumentTreeStorage()
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            CreateLocalExportTestFileUseCase(storage),
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
        val storage = FakeExportDocumentTreeStorage()
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            CreateLocalExportTestFileUseCase(storage),
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
        val storage = FakeExportDocumentTreeStorage()
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            CreateLocalExportTestFileUseCase(storage),
        )

        advanceUntilIdle()

        viewModel.onExportToDeviceStorageEnabledChanged(true)
        viewModel.onExportPasswordChanged("super-secret")
        viewModel.onExportNowClicked()

        advanceUntilIdle()

        assertEquals("Select an export folder", viewModel.uiState.value.errorMessage)
        assertEquals(0, storage.writeCalls)
        assertFalse(viewModel.uiState.value.isExporting)
    }

    @Test
    fun onExportNowClicked_usesUnsavedStateAndShowsSuccess_whenWriteSucceeds() = runTest {
        val settingsRepository = FakeSettingsRepository(defaultSettingsState())
        val passwordRepository = FakeExportPasswordRepository()
        val storage = FakeExportDocumentTreeStorage()
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            CreateLocalExportTestFileUseCase(storage),
        )

        advanceUntilIdle()

        val selectedFolder = "content://example/tree/export"
        val selectedPassword = "super-secret"
        viewModel.onExportToDeviceStorageEnabledChanged(true)
        viewModel.onExportFolderSelected(selectedFolder)
        viewModel.onExportPasswordChanged(selectedPassword)
        viewModel.onExportNowClicked()

        advanceUntilIdle()

        assertEquals(1, storage.writeCalls)
        assertEquals(selectedFolder, storage.lastWriteTreeUri)
        assertEquals("export_test.txt", storage.lastWriteFileName)
        assertEquals("Export test file created", viewModel.uiState.value.statusMessage)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun onExportNowClicked_showsError_whenStoragePermissionIsDenied() = runTest {
        val settingsRepository = FakeSettingsRepository(defaultSettingsState())
        val passwordRepository = FakeExportPasswordRepository()
        val storage = FakeExportDocumentTreeStorage(
            nextWriteResult = ExportStorageResult.Failure(ExportStorageError.PermissionDenied),
        )
        val viewModel = ExportSettingsViewModel(
            settingsRepository,
            passwordRepository,
            CreateLocalExportTestFileUseCase(storage),
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

private class FakeExportDocumentTreeStorage(
    private val nextWriteResult: ExportStorageResult<ExportTreeFile> = ExportStorageResult.Success(
        ExportTreeFile(
            name = "export_test.txt",
            documentUri = "content://example/document/export_test.txt",
            mimeType = "text/plain",
            lastModifiedEpochMillis = null,
        ),
    ),
) : ExportDocumentTreeStorage {
    var writeCalls: Int = 0
    var lastWriteTreeUri: String? = null
    var lastWriteFileName: String? = null

    override suspend fun writeOrReplaceFile(
        treeUri: String,
        fileName: String,
        mimeType: String,
        content: ByteArray,
    ): ExportStorageResult<ExportTreeFile> {
        writeCalls += 1
        lastWriteTreeUri = treeUri
        lastWriteFileName = fileName
        return nextWriteResult
    }

    override suspend fun listFiles(treeUri: String): ExportStorageResult<List<ExportTreeFile>> {
        return ExportStorageResult.Success(emptyList())
    }

    override suspend fun deleteFile(treeUri: String, fileName: String): ExportStorageResult<Unit> {
        return ExportStorageResult.Success(Unit)
    }
}

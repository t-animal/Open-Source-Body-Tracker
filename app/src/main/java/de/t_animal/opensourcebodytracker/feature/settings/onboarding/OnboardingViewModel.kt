package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.t_animal.opensourcebodytracker.core.model.AnalysisMethod
import de.t_animal.opensourcebodytracker.core.model.MeasuredBodyMetric
import de.t_animal.opensourcebodytracker.core.model.MeasurementSettings
import de.t_animal.opensourcebodytracker.core.model.ReminderSettings
import de.t_animal.opensourcebodytracker.core.model.Sex
import de.t_animal.opensourcebodytracker.core.model.UnitSystem
import de.t_animal.opensourcebodytracker.data.profile.ProfileRepository
import de.t_animal.opensourcebodytracker.data.reminders.ReminderAlarmScheduler
import de.t_animal.opensourcebodytracker.data.settings.GeneralSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.MeasurementSettingsRepository
import de.t_animal.opensourcebodytracker.data.settings.ReminderSettingsRepository
import de.t_animal.opensourcebodytracker.domain.demodata.GenerateDemoDataUseCase
import de.t_animal.opensourcebodytracker.domain.metrics.RequiredMeasurementsResolver
import de.t_animal.opensourcebodytracker.feature.settings.profile.ProfileValidationError
import de.t_animal.opensourcebodytracker.feature.settings.reminders.ReminderValidationError
import java.time.DayOfWeek
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StartStepState(
    val isDemoModeBusy: Boolean = false,
    val demoModeError: Boolean = false,
)

data class ProfileStepState(
    val sex: Sex? = null,
    val dateOfBirthText: String = "",
    val heightCmText: String = "",
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val validationError: ProfileValidationError? = null,
)

data class AnalysisStepState(
    val isLoading: Boolean = false,
    val settings: MeasurementSettings = MeasurementSettings(),
    val requiredMeasurements: Set<MeasuredBodyMetric> = emptySet(),
    val measurementToAnalysisMethods: Map<MeasuredBodyMetric, Set<AnalysisMethod>> = emptyMap(),
)

data class ReminderStepState(
    val enabled: Boolean = false,
    val weekdays: Set<DayOfWeek> = setOf(DayOfWeek.SUNDAY),
    val time: LocalTime = LocalTime.of(9, 0),
    val validationError: ReminderValidationError? = null,
)

data class OnboardingUiState(
    val start: StartStepState = StartStepState(),
    val profile: ProfileStepState = ProfileStepState(),
    val analysis: AnalysisStepState = AnalysisStepState(),
    val reminders: ReminderStepState = ReminderStepState(),
    val isSaving: Boolean = false,
    val saveError: Boolean = false,
)

sealed interface OnboardingEvent {
    data object DemoModeCompleted : OnboardingEvent
    data object ProfileValid : OnboardingEvent
    data object OnboardingCompleted : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val measurementSettingsRepository: MeasurementSettingsRepository,
    private val generalSettingsRepository: GeneralSettingsRepository,
    private val reminderSettingsRepository: ReminderSettingsRepository,
    private val requiredMeasurementsResolver: RequiredMeasurementsResolver,
    generateDemoDataUseCase: GenerateDemoDataUseCase,
    private val reminderAlarmScheduler: ReminderAlarmScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events = _events.asSharedFlow()

    private val analysisHandler: AnalysisStepHandler = AnalysisStepHandler(
        uiState = _uiState,
        requiredMeasurementsResolver = requiredMeasurementsResolver,
        validatedProfile = { profileHandler.validatedProfile },
    )

    private val profileHandler: ProfileStepHandler = ProfileStepHandler(
        uiState = _uiState,
        events = _events,
        coroutineScope = viewModelScope,
        analysisStepHandler = analysisHandler,
    )

    private val startHandler: StartStepHandler = StartStepHandler(
        uiState = _uiState,
        events = _events,
        coroutineScope = viewModelScope,
        profileRepository = profileRepository,
        generalSettingsRepository = generalSettingsRepository,
        generateDemoDataUseCase = generateDemoDataUseCase,
    )

    private val reminderHandler: ReminderStepHandler = ReminderStepHandler(uiState = _uiState)

    // Start step
    fun onTryDemoDataClicked() = startHandler.onTryDemoDataClicked()

    // Profile step
    fun onSexChanged(sex: Sex) = profileHandler.onSexChanged(sex)
    fun onDateOfBirthChanged(text: String) = profileHandler.onDateOfBirthChanged(text)
    fun onHeightCmChanged(text: String) = profileHandler.onHeightCmChanged(text)
    fun onUnitSystemChanged(unitSystem: UnitSystem) = profileHandler.onUnitSystemChanged(unitSystem)
    fun validateProfile() = profileHandler.validateProfile()

    // Analysis step
    fun onBmiEnabledChanged(enabled: Boolean) = analysisHandler.onBmiEnabledChanged(enabled)
    fun onNavyBodyFatEnabledChanged(enabled: Boolean) = analysisHandler.onNavyBodyFatEnabledChanged(enabled)
    fun onSkinfoldBodyFatEnabledChanged(enabled: Boolean) = analysisHandler.onSkinfoldBodyFatEnabledChanged(enabled)
    fun onWaistHipRatioEnabledChanged(enabled: Boolean) = analysisHandler.onWaistHipRatioEnabledChanged(enabled)
    fun onWaistHeightRatioEnabledChanged(enabled: Boolean) = analysisHandler.onWaistHeightRatioEnabledChanged(enabled)
    fun onMeasurementEnabledChanged(type: MeasuredBodyMetric, enabled: Boolean) =
        analysisHandler.onMeasurementEnabledChanged(type, enabled)

    // Reminder step
    fun onReminderEnabledChanged(enabled: Boolean) = reminderHandler.onEnabledChanged(enabled)
    fun onWeekdayToggled(dayOfWeek: DayOfWeek) = reminderHandler.onWeekdayToggled(dayOfWeek)
    fun onTimeChanged(time: LocalTime) = reminderHandler.onTimeChanged(time)
    fun onPermissionDeniedWhileSaving() = reminderHandler.onPermissionDeniedWhileSaving()

    // Finish — orchestrates across all steps
    fun validateAndFinish() {
        val current = _uiState.value
        if (current.isSaving) return
        
        if (!reminderHandler.validate()) return
        val profile = profileHandler.validatedProfile ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = false)
            runCatching {
                profileRepository.saveProfile(profile)

                val effective = requiredMeasurementsResolver.ensureRequired(
                    current.analysis.settings,
                    profile,
                )
                measurementSettingsRepository.saveSettings(effective.settings)

                val reminderSettings = ReminderSettings(
                    reminderEnabled = current.reminders.enabled,
                    reminderWeekdays = current.reminders.weekdays,
                    reminderTime = current.reminders.time,
                )
                reminderSettingsRepository.saveSettings(reminderSettings)
                reminderAlarmScheduler.syncWithSettings(reminderSettings)

                generalSettingsRepository.updateSettings {
                    it.copy(
                        onboardingCompleted = true,
                        isDemoMode = false,
                        unitSystem = current.profile.unitSystem,
                    )
                }

                _events.emit(OnboardingEvent.OnboardingCompleted)
            }.onFailure {
                _uiState.value = _uiState.value.copy(saveError = true)
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }
}

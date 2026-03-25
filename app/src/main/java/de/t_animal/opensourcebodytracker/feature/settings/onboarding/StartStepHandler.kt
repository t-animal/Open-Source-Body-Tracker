package de.t_animal.opensourcebodytracker.feature.settings.onboarding

import de.t_animal.opensourcebodytracker.domain.demodata.StartDemoModeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class StartStepHandler(
    private val uiState: MutableStateFlow<OnboardingUiState>,
    private val events: MutableSharedFlow<OnboardingEvent>,
    private val coroutineScope: CoroutineScope,
    private val startDemoModeUseCase: StartDemoModeUseCase,
) {

    fun onTryDemoDataClicked() {
        if (uiState.value.start.isDemoModeBusy) return

        coroutineScope.launch {
            uiState.value = uiState.value.copy(
                start = StartStepState(isDemoModeBusy = true, demoModeError = false),
            )
            runCatching {
                startDemoModeUseCase()
                events.emit(OnboardingEvent.DemoModeCompleted)
            }.onFailure {
                uiState.value = uiState.value.copy(
                    start = uiState.value.start.copy(demoModeError = true),
                )
            }
            uiState.value = uiState.value.copy(
                start = uiState.value.start.copy(isDemoModeBusy = false),
            )
        }
    }
}

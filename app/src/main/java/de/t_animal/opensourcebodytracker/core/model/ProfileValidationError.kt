package de.t_animal.opensourcebodytracker.core.model

sealed interface ProfileValidationError {
    data object MissingSex : ProfileValidationError
    data object InvalidDateOfBirth : ProfileValidationError
    data object InvalidHeight : ProfileValidationError
}

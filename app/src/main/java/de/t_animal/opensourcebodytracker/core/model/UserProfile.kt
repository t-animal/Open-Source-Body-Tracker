package de.t_animal.opensourcebodytracker.core.model

import de.t_animal.opensourcebodytracker.core.util.parseLocalizedFloatOrNull
import java.time.LocalDate

data class UserProfile(
    val sex: Sex,
    val dateOfBirth: LocalDate,
    val heightCm: Float,
) {
    companion object {
        fun parse(sex: Sex?, dateOfBirthText: String, heightCmText: String): ProfileParseResult {
            if (sex == null) return ProfileParseResult.Error(ProfileValidationError.MissingSex)

            val date = runCatching { LocalDate.parse(dateOfBirthText.trim()) }.getOrNull()
                ?: return ProfileParseResult.Error(ProfileValidationError.InvalidDateOfBirth)

            val height = parseLocalizedFloatOrNull(heightCmText)
            if (height == null || height <= 0f || height > 250f) {
                return ProfileParseResult.Error(ProfileValidationError.InvalidHeight)
            }

            return ProfileParseResult.Success(UserProfile(sex = sex, dateOfBirth = date, heightCm = height))
        }
    }
}

sealed interface ProfileParseResult {
    data class Success(val profile: UserProfile) : ProfileParseResult
    data class Error(val error: ProfileValidationError) : ProfileParseResult
}

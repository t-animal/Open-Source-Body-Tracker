package de.t_animal.opensourcebodytracker.data.profile

import de.t_animal.opensourcebodytracker.core.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

interface ProfileRepository {
    val profileFlow: Flow<UserProfile?>

    val requiredProfileFlow: Flow<UserProfile>
        get() = profileFlow.filterNotNull()

    suspend fun saveProfile(profile: UserProfile)
}

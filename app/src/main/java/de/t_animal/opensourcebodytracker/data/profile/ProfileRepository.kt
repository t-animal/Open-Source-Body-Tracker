package de.t_animal.opensourcebodytracker.data.profile

import de.t_animal.opensourcebodytracker.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profileFlow: Flow<UserProfile?>

    suspend fun saveProfile(profile: UserProfile)
}

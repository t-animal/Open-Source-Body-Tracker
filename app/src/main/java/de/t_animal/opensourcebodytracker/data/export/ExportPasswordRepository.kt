package de.t_animal.opensourcebodytracker.data.export

interface ExportPasswordRepository {
    suspend fun getPassword(): String?

    suspend fun savePassword(password: String?)
}

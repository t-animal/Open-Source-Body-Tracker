package de.t_animal.opensourcebodytracker

import android.app.Application

class BodyTrackerApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}

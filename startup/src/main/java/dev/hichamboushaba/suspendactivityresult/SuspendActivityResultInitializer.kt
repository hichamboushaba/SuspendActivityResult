package dev.hichamboushaba.suspendactivityresult

import android.app.Application
import android.content.Context
import androidx.startup.AppInitializer
import androidx.startup.Initializer


/**
 * Initializes [ActivityResultManager] using [androidx.startup]
 */
class SuspendActivityResultInitializer : Initializer<ActivityResultManager> {
    override fun create(context: Context): ActivityResultManager {
        val appInitializer = AppInitializer.getInstance(context)
        check(appInitializer.isEagerlyInitialized(javaClass)) {
            """SuspendActivityResultInitializer cannot be initialized lazily. 
                Please ensure that you have: 
                <meta-data
                    android:name='dev.hichamboushaba.suspendactivityresult.SuspendActivityResultInitializer' 
                    android:value='androidx.startup' /> 
                under InitializationProvider in your AndroidManifest.xml"""
        }
        val application = context as Application
        ActivityResultManager.init(application)
        return ActivityResultManager.getInstance()
    }

    override fun dependencies() = emptyList<Class<Initializer<*>>>()

}
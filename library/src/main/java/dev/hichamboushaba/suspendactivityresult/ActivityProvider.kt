package dev.hichamboushaba.suspendactivityresult

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.flow.*
import java.lang.ref.WeakReference

internal object ActivityProvider : Application.ActivityLifecycleCallbacks {
    private lateinit var _applicationContext: Context
    val applicationContext: Context
        get() = _applicationContext

    private val _activityFlow = MutableStateFlow(WeakReference<ComponentActivity>(null))
    val activityFlow: Flow<ComponentActivity> =
        _activityFlow
            .asStateFlow()
            .distinctUntilChanged { old, new -> old.get() === new.get() }
            .map { it.get() }
            .filterNotNull()
            .filter { it.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED) }

    val currentActivity: ComponentActivity?
        get() = _activityFlow.value
            .get()?.takeIf { it.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED) }

    fun init(application: Application) {
        _applicationContext = application
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        (activity as? ComponentActivity)?.let {
            _activityFlow.value = WeakReference(it)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        (activity as? ComponentActivity)?.let {
            _activityFlow.value = WeakReference(it)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        (activity as? ComponentActivity)?.let {
            _activityFlow.value = WeakReference(it)
        }
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
package com.hicham.activityresult

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.*
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityProvider @Inject constructor() :
    Application.ActivityLifecycleCallbacks {
    private val _activityFlow = MutableStateFlow(WeakReference<ComponentActivity>(null))
    val activityFlow = _activityFlow.asStateFlow()
        .distinctUntilChanged { old, new -> old.get() === new.get() }
        .filter { it.get() != null }
        .map { it.get()!! }

    val currentActivity
        get() = _activityFlow.value.get()

    fun init(application: Application) = application.registerActivityLifecycleCallbacks(this)

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {}

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
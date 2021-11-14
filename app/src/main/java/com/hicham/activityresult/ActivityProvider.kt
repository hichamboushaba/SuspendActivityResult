package com.hicham.activityresult

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityProvider @Inject constructor(private val application: Application) :
    Application.ActivityLifecycleCallbacks {
    private val _activityFlow = MutableStateFlow(WeakReference<ComponentActivity>(null))
    val activityFlow = _activityFlow.asStateFlow()
        .filter { it.get() != null }
        .map { it.get()!! }

    val currentActivity = _activityFlow.value.get()

    init {
        application.registerActivityLifecycleCallbacks(this)
    }

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
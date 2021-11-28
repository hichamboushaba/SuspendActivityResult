package com.hicham.activityresult

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.hichamboushaba.suspendactivityresult.ActivityResultManager
import dev.hichamboushaba.suspendactivityresult.init
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun onCreate() {
        super.onCreate()
        activityProvider.init(this)
        ActivityResultManager.init(this)
    }
}
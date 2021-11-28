package dev.hichamboushaba.suspendactivityresult

import android.app.Application
import androidx.activity.result.contract.ActivityResultContract

interface ActivityResultManager {
    companion object {
        fun getInstance(): ActivityResultManager = ActivityResultManagerImpl
        fun init(application: Application) {
            ActivityProvider.init(application)
        }
    }

    suspend fun <I, O, C : ActivityResultContract<I, O>> requestResult(contract: C, input: I): O?
}
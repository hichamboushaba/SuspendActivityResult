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

    /**
     * Requests an Activity Result using the current visible Activity from the app.
     *
     * To allow handling process-death scenarios, the function checks if there is a pending result before
     * re-requesting a new one. So to handle this case, you just need to remember that there is a pending operation
     * in your [androidx.lifecycle.ViewModel] using a [androidx.lifecycle.SavedStateHandle],
     * then call the function another time when the app recovers to continue from where it left.
     *
     * @param contract the [androidx.activity.result.contract.ActivityResultContract] to use
     * @param input the input to pass when requesting the result, it needs to match the used [contract]
     *
     * @return the activity result
     */
    suspend fun <I, O, C : ActivityResultContract<I, O>> requestResult(contract: C, input: I): O?
}
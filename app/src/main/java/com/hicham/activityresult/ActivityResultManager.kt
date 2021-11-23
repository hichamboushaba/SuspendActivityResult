package com.hicham.activityresult

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.bundleOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class ActivityResultManager@Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityProvider: ActivityProvider
) {
    companion object {
        private const val SAVED_STATE_REGISTRY_KEY = "permissions_saved_state"
        private const val PENDING_INPUT_KEY = "pending_input"
        private const val LAST_INCREMENT_KEY = "key_increment"
    }

    private val keyIncrement = AtomicInteger(0)
    private var pendingInput: String? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <I, O, C: ActivityResultContract<I, O>> requestResult(contract: C, input: I): O? {
        var isLaunched = false
        val key = activityProvider.currentActivity?.let { activity ->
            val savedBundle =
                activity.savedStateRegistry.consumeRestoredStateForKey(SAVED_STATE_REGISTRY_KEY)
            // We assume that the `toString()` is enough for comparing the input
            if (savedBundle?.getString(PENDING_INPUT_KEY) == input.toString()) {
                isLaunched = true
                generateKey(savedBundle.getInt(LAST_INCREMENT_KEY))
            } else {
                generateKey(keyIncrement.getAndIncrement())
            }
        } ?: return null

        pendingInput = input.toString()
        return activityProvider.activityFlow
            .mapLatest { currentActivity ->
                if (!isLaunched) {
                    prepareSavedData(currentActivity)
                }

                var launcher: ActivityResultLauncher<I>? = null
                try {
                    suspendCancellableCoroutine<O> { continuation ->
                        launcher = currentActivity.activityResultRegistry.register(
                            key,
                            contract
                        ) { result ->
                            pendingInput = null
                            clearSavedStateData(currentActivity)
                            continuation.resume(result)
                        }

                        if (!isLaunched) {
                            launcher!!.launch(input)
                            isLaunched = true
                        }
                    }
                } finally {
                    launcher?.unregister()
                }
            }
            .first()
    }

    private fun prepareSavedData(currentActivity: ComponentActivity) {
        currentActivity.savedStateRegistry.registerSavedStateProvider(
            SAVED_STATE_REGISTRY_KEY
        ) {
            bundleOf(
                PENDING_INPUT_KEY to pendingInput,
                LAST_INCREMENT_KEY to keyIncrement.get() - 1
            )
        }
    }

    private fun clearSavedStateData(currentActivity: ComponentActivity) {
        currentActivity.savedStateRegistry.unregisterSavedStateProvider(
            SAVED_STATE_REGISTRY_KEY
        )
        // Delete the data by consuming it
        currentActivity.savedStateRegistry.consumeRestoredStateForKey(
            SAVED_STATE_REGISTRY_KEY
        )
    }

    private fun generateKey(increment: Int) = "permission_$increment"
}
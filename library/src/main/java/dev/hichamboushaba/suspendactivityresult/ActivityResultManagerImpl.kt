package dev.hichamboushaba.suspendactivityresult

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.bundleOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

internal object ActivityResultManagerImpl : ActivityResultManager {
    private const val SAVED_STATE_REGISTRY_KEY = "permissions_saved_state"
    private const val PENDING_RESULT_KEY = "pending"
    private const val LAST_INCREMENT_KEY = "key_increment"

    private val keyIncrement = AtomicInteger(0)
    private var pendingResult: Boolean = false

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun <I, O, C : ActivityResultContract<I, O>> requestResult(
        contract: C,
        input: I
    ): O? {
        var isLaunched = false
        val key = ActivityProvider.currentActivity?.let { activity ->
            val savedBundle =
                activity.savedStateRegistry.consumeRestoredStateForKey(SAVED_STATE_REGISTRY_KEY)
            if (savedBundle?.getBoolean(PENDING_RESULT_KEY) == true) {
                isLaunched = true
                generateKey(savedBundle.getInt(LAST_INCREMENT_KEY))
            } else {
                generateKey(keyIncrement.getAndIncrement())
            }
        } ?: return null

        pendingResult = true
        return ActivityProvider.activityFlow
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
                            pendingResult = false
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
                PENDING_RESULT_KEY to pendingResult,
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
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
    private var pendingResult: String? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun <I, O, C : ActivityResultContract<I, O>> requestResult(
        contract: C,
        input: I
    ): O? {
        var (isLaunched, key) = ActivityProvider.currentActivity?.calculateKey(contract)
            ?: return null

        pendingResult = contract.javaClass.simpleName
        return ActivityProvider.activityFlow
            .mapLatest { currentActivity ->
                if (!isLaunched) {
                    currentActivity.prepareSavedData()
                }

                var launcher: ActivityResultLauncher<I>? = null
                try {
                    suspendCancellableCoroutine<O> { continuation ->
                        launcher = currentActivity.activityResultRegistry.register(
                            key,
                            contract
                        ) { result ->
                            pendingResult = null
                            currentActivity.clearSavedStateData()
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

    private fun <C : ActivityResultContract<*, *>> ComponentActivity.calculateKey(contract: C): Pair<Boolean, String> {
        fun generateKey(increment: Int) = "result_$increment"

        val savedBundle = savedStateRegistry.consumeRestoredStateForKey(SAVED_STATE_REGISTRY_KEY)

        return if (contract.javaClass.simpleName == savedBundle?.getString(PENDING_RESULT_KEY)) {
            Pair(true, generateKey(savedBundle!!.getInt(LAST_INCREMENT_KEY)))
        } else {
            Pair(false, generateKey(keyIncrement.getAndIncrement()))
        }
    }

    private fun ComponentActivity.prepareSavedData() {
        savedStateRegistry.registerSavedStateProvider(
            SAVED_STATE_REGISTRY_KEY
        ) {
            bundleOf(
                PENDING_RESULT_KEY to pendingResult,
                LAST_INCREMENT_KEY to keyIncrement.get() - 1
            )
        }
    }

    private fun ComponentActivity.clearSavedStateData() {
        savedStateRegistry.unregisterSavedStateProvider(
            SAVED_STATE_REGISTRY_KEY
        )
        // Delete the data by consuming it
        savedStateRegistry.consumeRestoredStateForKey(
            SAVED_STATE_REGISTRY_KEY
        )
    }
}
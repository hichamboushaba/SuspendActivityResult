package com.hicham.activityresult.permission

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import com.hicham.activityresult.ActivityProvider
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
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityProvider: ActivityProvider
) {
    companion object {
        private const val SAVED_STATE_REGISTRY_KEY = "permissions_saved_state"
    }

    private val keyIncrement = AtomicInteger(0)

    private var pendingPermission: String? = null

    fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PERMISSION_GRANTED
    }

    suspend fun requestPermission(permission: String): PermissionStatus {
        return requestPermissions(permission)[permission] ?: error("permission result is empty")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun requestPermissions(vararg permissions: String): Map<String, PermissionStatus> {
        var isLaunched = false
        val key = activityProvider.currentActivity?.let { activity ->
            val savedBundle =
                activity.savedStateRegistry.consumeRestoredStateForKey(SAVED_STATE_REGISTRY_KEY)
            if (savedBundle?.getString("pending_permission") == permissions.joinToString(",")) {
                isLaunched = true
                generateKey(savedBundle.getInt("key_increment"))
            } else {
                generateKey(keyIncrement.getAndIncrement())
            }
        } ?: return permissions.associateWith {
            if (hasPermission(it)) PermissionGranted else PermissionDenied(shouldShowRationale = false)
        }

        pendingPermission = permissions.joinToString(",")
        return activityProvider.activityFlow
            .mapLatest { currentActivity ->
                if (!isLaunched) {
                    prepareSavedData(currentActivity)
                }

                var launcher: ActivityResultLauncher<Array<out String>>? = null
                try {
                    suspendCancellableCoroutine<Map<String, PermissionStatus>> { continuation ->
                        launcher = currentActivity.activityResultRegistry.register(
                            key,
                            ActivityResultContracts.RequestMultiplePermissions()
                        ) { result ->
                            pendingPermission = null
                            clearSavedStateData(currentActivity)
                            continuation.resume(permissions.associateWith {
                                if (result[it] == true) {
                                    PermissionGranted
                                } else {
                                    val shouldShowRationale = currentActivity.shouldShowRequestPermissionRationale(it)
                                    PermissionDenied(shouldShowRationale)
                                }
                            })
                        }

                        if (!isLaunched) {
                            launcher!!.launch(permissions)
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
                "pending_permission" to pendingPermission,
                "key_increment" to keyIncrement.get() - 1
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

sealed class PermissionStatus
object PermissionGranted : PermissionStatus()
data class PermissionDenied(val shouldShowRationale: Boolean) : PermissionStatus()
package com.hicham.activityresult.permission

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.activity.result.contract.ActivityResultContracts
import com.hicham.activityresult.ActivityProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private val keyIncrement = AtomicInteger(0)

    fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PERMISSION_GRANTED
    }

    suspend fun requestPermission(permission: String): PermissionStatus {
        return requestPermissions(permission)[permission] ?: error("permission result is empty")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun requestPermissions(vararg permissions: String): Map<String, PermissionStatus> {
        val currentActivity = activityProvider.currentActivity ?: return permissions.associateWith {
            PermissionDenied(false)
        }

        return suspendCancellableCoroutine { continuation ->
            val launcher = currentActivity.activityResultRegistry.register(
                "permission_${keyIncrement.getAndIncrement()}",
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                continuation.resume(permissions.associateWith {
                    if (result[it] == true) {
                        PermissionGranted
                    } else {
                        val shouldShowRationale =
                            currentActivity.shouldShowRequestPermissionRationale(it)
                        PermissionDenied(shouldShowRationale)
                    }
                })
            }
            launcher.launch(permissions)

            continuation.invokeOnCancellation {
                launcher.unregister()
            }
        }
    }
}

sealed class PermissionStatus
object PermissionGranted : PermissionStatus()
data class PermissionDenied(val shouldShowRationale: Boolean) : PermissionStatus()
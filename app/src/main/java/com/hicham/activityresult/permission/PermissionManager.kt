package com.hicham.activityresult.permission

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import com.hicham.activityresult.ActivityProvider
import com.hicham.activityresult.ActivityResultManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityResultManager: ActivityResultManager,
    private val activityProvider: ActivityProvider
) {
    fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PERMISSION_GRANTED
    }

    suspend fun requestPermission(permission: String): PermissionStatus {
        return requestPermissions(permission)[permission] ?: error("permission result is empty")
    }

    suspend fun requestPermissions(vararg permissions: String): Map<String, PermissionStatus> {
        return activityResultManager.requestResult(ActivityResultContracts.RequestMultiplePermissions(), permissions)?.let { result ->
            permissions.associateWith {
                if (result[it] == true) {
                    PermissionGranted
                } else {
                    val shouldShowRationale = activityProvider.currentActivity?.shouldShowRequestPermissionRationale(it)
                    PermissionDenied(shouldShowRationale ?: false)
                }
            }
        } ?: return permissions.associateWith {
            if (hasPermission(it)) PermissionGranted else PermissionDenied(shouldShowRationale = false)
        }
    }
}

sealed class PermissionStatus
object PermissionGranted : PermissionStatus()
data class PermissionDenied(val shouldShowRationale: Boolean) : PermissionStatus()
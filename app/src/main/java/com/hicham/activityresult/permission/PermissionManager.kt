package com.hicham.activityresult.permission

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.activity.result.contract.ActivityResultContracts
import com.hicham.activityresult.ActivityProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.hichamboushaba.suspendactivityresult.ActivityResultManager
import dev.hichamboushaba.suspendactivityresult.requestPermissions
import javax.inject.Inject

class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityProvider: ActivityProvider,
    private val activityResultManager: ActivityResultManager
) {
    fun hasPermission(permission: String): Boolean {
        return context.checkSelfPermission(permission) == PERMISSION_GRANTED
    }

    suspend fun requestPermission(permission: String): PermissionStatus {
        return requestPermissions(permission)[permission] ?: error("permission result is empty")
    }

    suspend fun requestPermissions(vararg permissions: String): Map<String, PermissionStatus> {
        return activityResultManager.requestPermissions(*permissions)?.let { result ->
            permissions.associateWith {
                if (result[it] == true) {
                    PermissionGranted
                } else {
                    val shouldShowRationale =
                        activityProvider.currentActivity?.shouldShowRequestPermissionRationale(it)
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
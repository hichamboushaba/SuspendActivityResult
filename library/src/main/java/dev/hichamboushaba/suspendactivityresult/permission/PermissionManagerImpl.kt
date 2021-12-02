package dev.hichamboushaba.suspendactivityresult.permission

import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.hichamboushaba.suspendactivityresult.ActivityProvider
import dev.hichamboushaba.suspendactivityresult.ActivityResultManager
import dev.hichamboushaba.suspendactivityresult.requestPermission
import dev.hichamboushaba.suspendactivityresult.requestPermissions

internal object PermissionManagerImpl : PermissionManager {
    override fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            ActivityProvider.applicationContext,
            permission
        ) == PERMISSION_GRANTED
    }

    override suspend fun requestPermission(permission: String): PermissionStatus {
        val isGranted = ActivityResultManager.getInstance().requestPermission(permission)
        return if (isGranted) {
            PermissionGranted
        } else {
            val shouldShowRationale = ActivityProvider.currentActivity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
            }
            PermissionDenied(shouldShowRationale ?: false)
        }
    }

    override suspend fun requestPermissions(vararg permissions: String): Map<String, PermissionStatus> {
        return ActivityResultManager.getInstance().requestPermissions(*permissions)?.let { result ->
            permissions.associateWith { permission ->
                if (result[permission] == true) {
                    PermissionGranted
                } else {
                    val shouldShowRationale = ActivityProvider.currentActivity?.let {
                        ActivityCompat.shouldShowRequestPermissionRationale(it, permission)
                    }
                    PermissionDenied(shouldShowRationale ?: false)
                }
            }
        } ?: return permissions.associateWith {
            if (hasPermission(it)) PermissionGranted else PermissionDenied(shouldShowRationale = false)
        }
    }
}
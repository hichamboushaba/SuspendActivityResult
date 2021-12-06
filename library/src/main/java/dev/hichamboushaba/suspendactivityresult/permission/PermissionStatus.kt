package dev.hichamboushaba.suspendactivityresult.permission

import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.app.Activity

sealed class PermissionStatus

/**
 * @see [PERMISSION_GRANTED]
 */
object PermissionGranted : PermissionStatus()

/**
 * @see [PERMISSION_DENIED]
 * @see [Activity.shouldShowRequestPermissionRationale]
 */
data class PermissionDenied(val shouldShowRationale: Boolean) : PermissionStatus()
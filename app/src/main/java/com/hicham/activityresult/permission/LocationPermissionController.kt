package com.hicham.activityresult.permission

import android.Manifest
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hicham.activityresult.ActivityProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationPermissionController @Inject constructor(
    private val permissionManager: PermissionManager,
    private val activityProvider: ActivityProvider
) {
    suspend fun requestLocationPermission(): Boolean {
        if (permissionManager.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return true
        val permissionStatus =
            permissionManager.requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        return when (permissionStatus) {
            is PermissionDenied -> {
                if (permissionStatus.shouldShowRationale) {
                    if (showRationale()) requestLocationPermission() else false
                } else {
                    showDenialSnackBar()
                    false
                }
            }
            PermissionGranted -> true
        }
    }

    private suspend fun showRationale(): Boolean {
        val activity = activityProvider.currentActivity ?: return false
        return try {
            withContext(activity.lifecycleScope.coroutineContext) {
                suspendCancellableCoroutine { continuation ->
                    val dialog = MaterialAlertDialogBuilder(activity)
                        .setTitle("The app needs the location permission")
                        .setPositiveButton("Grant") { _, _ ->
                            continuation.resume(true)
                        }
                        .setNegativeButton("Cancel", null)
                        .setOnDismissListener {
                            if (continuation.isActive) {
                                continuation.resume(false)
                            }
                        }
                        .setCancelable(false)
                        .show()

                    continuation.invokeOnCancellation {
                        dialog.dismiss()
                    }
                }
            }
        } catch (e: CancellationException) {
            // The activity was destroyed
            return false
        }
    }

    private fun showDenialSnackBar() {
        activityProvider.currentActivity?.let {
            Snackbar.make(
                it.findViewById(android.R.id.content),
                "Location permission was denied",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}

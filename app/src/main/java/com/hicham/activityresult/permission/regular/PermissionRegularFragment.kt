package com.hicham.activityresult.permission.regular

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hicham.activityresult.BaseFragment
import com.hicham.activityresult.R
import com.hicham.activityresult.databinding.FragmentPermissionRegularBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class PermissionRegularFragment :
    BaseFragment<PermissionRegularViewModel>(R.layout.fragment_permission_regular) {
    companion object {
        private const val IS_WAITING_FOR_PERMISSION_KEY = "is_waiting_for_permission"
    }

    override val viewModel: PermissionRegularViewModel by viewModels()

    private var isWaitingForPermission = false

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.onPermissionGranted(
                granted = it,
                shouldShowRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            )
            isWaitingForPermission = false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            isWaitingForPermission = it.getBoolean(IS_WAITING_FOR_PERMISSION_KEY, false)
        }

        val binding = FragmentPermissionRegularBinding.bind(view)
        binding.toggleButton.setOnClickListener {
            viewModel.toggleState()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            observeEvents(binding)
            observeViewState(binding)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(IS_WAITING_FOR_PERMISSION_KEY, isWaitingForPermission)
        super.onSaveInstanceState(outState)
    }

    private fun requestLocationPermission() {
        if (!isWaitingForPermission) {
            locationPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            isWaitingForPermission = true
        }
    }

    private fun CoroutineScope.observeViewState(binding: FragmentPermissionRegularBinding) {
        viewModel.viewState
            .onEach { viewState ->
                binding.toggleButton.text = if (viewState.isObservingLocation) "Stop" else "Start"
                if (viewState.isObservingLocation) {
                    viewState.currentLocation?.let {
                        binding.currentLocation.text =
                            "Current Location ${it.latitude},${it.longitude}"
                    } ?: run {
                        binding.currentLocation.text = "Getting your location!"
                    }
                } else {
                    binding.currentLocation.text = ""
                }
            }
            .launchIn(this)
    }

    private fun CoroutineScope.observeEvents(binding: FragmentPermissionRegularBinding) {
        viewModel.events
            .onEach {
                when (it) {
                    PermissionRegularViewModel.Event.RequestLocationPermission -> {
                        requestLocationPermission()
                    }
                    PermissionRegularViewModel.Event.ShowPermissionRationale -> {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("The app needs the location permission")
                            .setPositiveButton("Grant") { _, _ ->
                                requestLocationPermission()
                            }
                            .setCancelable(false)
                            .show()
                    }
                    PermissionRegularViewModel.Event.ShowPermissionSnackBar -> {
                        Snackbar.make(
                            binding.root,
                            "Location permission was denied",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .launchIn(this)
    }
}
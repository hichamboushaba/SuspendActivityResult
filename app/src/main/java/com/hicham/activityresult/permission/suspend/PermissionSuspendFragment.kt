package com.hicham.activityresult.permission.suspend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.hicham.activityresult.BaseFragment
import com.hicham.activityresult.R
import com.hicham.activityresult.databinding.FragmentPermissionBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class PermissionSuspendFragment :
    BaseFragment<PermissionSuspendViewModel>(R.layout.fragment_permission) {

    override val viewModel: PermissionSuspendViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentPermissionBinding.bind(view)
        binding.toggleButton.setOnClickListener {
            viewModel.toggleState()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            observeEvents(binding)
            observeViewState(binding)
        }
    }

    private fun CoroutineScope.observeViewState(binding: FragmentPermissionBinding) {
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

    private fun CoroutineScope.observeEvents(binding: FragmentPermissionBinding) {
        viewModel.events
            .onEach {
                when (it) {
                    PermissionSuspendViewModel.Event.ShowPermissionRationale -> {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle("The app needs the location permission")
                            .setPositiveButton("Grant") { _, _ ->
                                viewModel.toggleState()
                            }
                            .setCancelable(false)
                            .show()
                    }
                    PermissionSuspendViewModel.Event.ShowPermissionSnackBar -> {
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
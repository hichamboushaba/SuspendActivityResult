package com.hicham.activityresult.permission.suspend

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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
}
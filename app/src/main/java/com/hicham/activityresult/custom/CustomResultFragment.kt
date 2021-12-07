package com.hicham.activityresult.custom

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hicham.activityresult.BaseFragment
import com.hicham.activityresult.R
import com.hicham.activityresult.databinding.FragmentInternalResultBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class CustomResultFragment :
    BaseFragment<CustomResultViewModel>(R.layout.fragment_internal_result) {
    override val viewModel: CustomResultViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentInternalResultBinding.bind(view)
        binding.getResultButton.setOnClickListener {
            viewModel.getResult()
        }

        viewModel.result
            .filterNotNull()
            .onEach {
                binding.result.text = it.toString()
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
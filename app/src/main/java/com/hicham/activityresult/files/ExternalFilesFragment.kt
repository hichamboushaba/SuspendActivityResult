package com.hicham.activityresult.files

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hicham.activityresult.BaseFragment
import com.hicham.activityresult.R
import com.hicham.activityresult.databinding.FragmentExternalFilesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@AndroidEntryPoint
class ExternalFilesFragment :
    BaseFragment<ExternalFilesViewModel>(R.layout.fragment_external_files) {
    override val viewModel: ExternalFilesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentExternalFilesBinding.bind(view)
        binding.pickFileButton.setOnClickListener {
            viewModel.pickFile()
        }

        viewModel.image
            .onEach {
                binding.previewImage.setImageURI(it)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }
}
package com.hicham.activityresult.files

import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewModelScope
import com.hicham.activityresult.ActivityResultManager
import com.hicham.activityresult.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExternalFilesViewModel @Inject constructor(
    private val activityResultManager: ActivityResultManager
) : BaseViewModel() {
    fun pickFile() {
        viewModelScope.launch {
            val file = activityResultManager.requestResult(
                ActivityResultContracts.OpenDocument(),
                arrayOf("image/*")
            )

            println("picked URI: $file")
        }
    }
}

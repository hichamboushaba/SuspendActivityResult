package com.hicham.activityresult.files

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hicham.activityresult.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hichamboushaba.suspendactivityresult.ActivityResultManager
import dev.hichamboushaba.suspendactivityresult.getContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExternalFilesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activityResultManager: ActivityResultManager
) : BaseViewModel() {
    companion object {
        private const val IMAGE_URI_KEY = "image"
        private const val IS_WAITING_FOR_FILE_KEY = "is_waiting_for_key"
    }

    private val _image = MutableStateFlow<Uri?>(savedStateHandle.get(IMAGE_URI_KEY))
    val image = _image.asStateFlow()

    init {
        if (savedStateHandle.get<Boolean>(IS_WAITING_FOR_FILE_KEY) == true) {
            pickFile()
        }
    }

    fun pickFile() {
        viewModelScope.launch {
            savedStateHandle.set(IS_WAITING_FOR_FILE_KEY, true)
            val uri = activityResultManager.getContent("image/*")
            savedStateHandle.set(IMAGE_URI_KEY, uri)
            savedStateHandle.set(IS_WAITING_FOR_FILE_KEY, false)

            _image.value = uri
        }
    }
}

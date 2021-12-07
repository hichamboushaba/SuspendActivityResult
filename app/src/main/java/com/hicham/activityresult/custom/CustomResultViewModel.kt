package com.hicham.activityresult.custom

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hicham.activityresult.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.hichamboushaba.suspendactivityresult.ActivityResultManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomResultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val activityResultManager: ActivityResultManager
) : BaseViewModel() {
    companion object {
        private const val RESULT_KEY = "result"
        private const val IS_WAITING_FOR_RESULT = "is_waiting_for_result"
    }

    private val _result = MutableStateFlow<Int?>(savedStateHandle.get(RESULT_KEY))
    val result = _result.asStateFlow()

    init {
        if (savedStateHandle.get<Boolean>(IS_WAITING_FOR_RESULT) == true) {
            getResult()
        }
    }

    fun getResult() {
        viewModelScope.launch {
            savedStateHandle.set(IS_WAITING_FOR_RESULT, true)
            val result = activityResultManager.requestResult(
                contract = CustomContract(),
                input = null
            )
            savedStateHandle.set(RESULT_KEY, result)
            savedStateHandle.set(IS_WAITING_FOR_RESULT, false)

            _result.value = result
        }
    }

    private class CustomContract : ActivityResultContract<Nothing?, Int?>() {
        override fun createIntent(context: Context, input: Nothing?): Intent {
            return Intent(context, ResultActivity::class.java)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Int? {
            return intent?.let {
                it.getIntExtra(ResultActivity.RESULT_EXTRA_KEY, -1)
                    .takeIf { result -> result != -1 }
            }
        }
    }
}

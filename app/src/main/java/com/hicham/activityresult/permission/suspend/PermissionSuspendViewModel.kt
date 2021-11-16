package com.hicham.activityresult.permission.suspend

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hicham.activityresult.BaseViewModel
import com.hicham.activityresult.LocationObserver
import com.hicham.activityresult.permission.LocationPermissionController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PermissionSuspendViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val locationObserver: LocationObserver,
    private val locationPermissionController: LocationPermissionController
) : BaseViewModel() {
    companion object {
        private const val IS_STARTED_KEY = "is_started"
    }

    private val _isStarted = MutableStateFlow(false)

    @SuppressLint("MissingPermission")
    val viewState = _isStarted
        .onEach { savedStateHandle.set(IS_STARTED_KEY, it) }
        .flatMapLatest { started ->
            if (!started) {
                flowOf(ViewState(isObservingLocation = false, currentLocation = null))
            } else {
                val granted = locationPermissionController.requestLocationPermission()
                if (granted) {
                    startObservingLocation()
                        .map { ViewState(isObservingLocation = true, currentLocation = it) }
                        .onStart { emit(ViewState(isObservingLocation = true)) }
                } else {
                    _isStarted.value = false
                    flowOf(ViewState(isObservingLocation = false, currentLocation = null))
                }
            }
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ViewState()
        )

    init {
        _isStarted.value = savedStateHandle.get<Boolean>(IS_STARTED_KEY) ?: false
    }

    @SuppressLint("MissingPermission")
    private fun startObservingLocation(): Flow<Location> {
        return locationObserver.observeLocationUpdates()
            .whenAtLeast(Lifecycle.State.STARTED)
    }

    fun toggleState() {
        _isStarted.update { !it }
    }

    data class ViewState(
        val isObservingLocation: Boolean = false,
        val currentLocation: Location? = null
    )
}
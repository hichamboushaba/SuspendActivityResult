package com.hicham.activityresult.permission.regular

import android.annotation.SuppressLint
import android.location.Location
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hicham.activityresult.BaseViewModel
import com.hicham.activityresult.LocationObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class PermissionRegularViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val locationObserver: LocationObserver
) : BaseViewModel() {
    companion object {
        private const val IS_STARTED_KEY = "is_started"
    }

    private val _events = Channel<Event>(capacity = Channel.BUFFERED)
    val events = _events.receiveAsFlow()
        .whenAtLeast(Lifecycle.State.STARTED)

    private val _locationPermissionGranted = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    private val _isStarted = MutableStateFlow(false)

    @SuppressLint("MissingPermission")
    val viewState = _isStarted
        .onEach { savedStateHandle.set(IS_STARTED_KEY, it) }
        .flatMapLatest { started ->
            if (!started) {
                flowOf(ViewState(isObservingLocation = false, currentLocation = null))
            } else {
                _events.trySend(Event.RequestLocationPermission)
                _locationPermissionGranted.flatMapLatest { granted ->
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
        }
        .stateIn(
            viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = ViewState()
        )

    @SuppressLint("MissingPermission")
    private fun startObservingLocation(): Flow<Location> {
        return locationObserver.observeLocationUpdates()
            .whenAtLeast(Lifecycle.State.STARTED)
    }

    init {
        _isStarted.value = savedStateHandle.get<Boolean>(IS_STARTED_KEY) ?: false
    }

    fun onPermissionGranted(granted: Boolean, shouldShowRationale: Boolean) {
        if (!granted) {
            if (shouldShowRationale) {
                _events.trySend(Event.ShowPermissionRationale)
            } else {
                _events.trySend(Event.ShowPermissionSnackBar)
            }
        }
        if (granted || !shouldShowRationale) {
            _locationPermissionGranted.tryEmit(granted)
        }
    }

    fun toggleState() {
        _isStarted.update { !it }
    }

    data class ViewState(
        val isObservingLocation: Boolean = false,
        val currentLocation: Location? = null
    )

    sealed class Event {
        object RequestLocationPermission : Event()
        object ShowPermissionRationale : Event()
        object ShowPermissionSnackBar : Event()
    }
}
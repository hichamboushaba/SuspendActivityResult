package com.hicham.activityresult

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

open class BaseViewModel : ViewModel() {
    private val lifeCycleState = MutableSharedFlow<Lifecycle.State>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val lifecycleObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            lifeCycleState.tryEmit(event.targetState)
            if (event.targetState == Lifecycle.State.DESTROYED) {
                source.lifecycle.removeObserver(this)
            }
        }
    }

    fun startObservingLifecycle(lifecycle: Lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
    }

    protected fun <T> Flow<T>.whenAtLeast(requiredState: Lifecycle.State): Flow<T> {
        return lifeCycleState.map { state -> state.isAtLeast(requiredState) }
            .distinctUntilChanged()
            .flatMapLatest {
                if (it) this else emptyFlow()
            }
    }
}
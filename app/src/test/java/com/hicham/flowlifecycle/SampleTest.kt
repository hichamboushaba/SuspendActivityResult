package com.hicham.activityresult

import android.app.Application
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito.mock
import org.mockito.kotlin.mock

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SampleTest {
    val viewModel = TestViewModel(mock())

    @Test
    fun `don't emit when not started`() {
        val lifecycle = LifecycleRegistry.createUnsafe(mock())
        viewModel.startObservingLifecycle(lifecycle)

        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        runBlocking {
            val items = mutableListOf<Int>()
            withTimeoutOrNull(1000) {
                viewModel.testFlow.collect {
                    items.add(it)
                }
            }
            assert(items.isEmpty())
        }
    }

    @Test
    fun `emit when started`() {
        val lifecycle = LifecycleRegistry.createUnsafe(mock())
        viewModel.startObservingLifecycle(lifecycle)

        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)

        runBlocking {
            val items = mutableListOf<Int>()
            withTimeoutOrNull(1000) {
                viewModel.testFlow.collect {
                    items.add(it)
                }
            }
            assert(items.isNotEmpty())
        }
    }
}

class TestViewModel(application: Application) : BaseViewModel(application) {
    val testFlow = flow {
        emit(1)
        emit(2)
    }.whenAtLeast(Lifecycle.State.STARTED)
}
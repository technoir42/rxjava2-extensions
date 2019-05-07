package com.sch.rxjava2.extensions

import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DisposableAutoConnectObservableTest {
    @Test
    fun `Connects at the required number of observers`() {
        val source = PublishSubject.create<Int>()
        val observable = source.publish().autoConnectDisposable(2)

        assertFalse(source.hasObservers())
        observable.test()
        assertFalse(source.hasObservers())
        observable.test()
        assertTrue(source.hasObservers())
    }

    @Test
    fun `Connects immediately if numberOfObservers is non-positive`() {
        val source = PublishSubject.create<Int>()
        source.publish().autoConnectDisposable(0)

        assertTrue(source.hasObservers())
    }

    @Test
    fun `Dispose terminates connection to source`() {
        val source = PublishSubject.create<Int>()
        val observable = source.publish().autoConnectDisposable(1)

        observable.test().cancel()
        assertTrue(source.hasObservers())
        assertFalse(observable.isDisposed)

        observable.dispose()
        assertFalse(source.hasObservers())
        assertTrue(observable.isDisposed)
    }
}

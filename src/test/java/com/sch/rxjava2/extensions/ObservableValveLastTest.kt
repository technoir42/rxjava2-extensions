package com.sch.rxjava2.extensions

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class ObservableValveLastTest {
    @Test
    @DisplayName("Stops relaying values when the other Observable signals false")
    fun closeValve() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val ts = ObservableValveLast.create(source, other, true).test()

        source.onNext(1)
        other.onNext(false)
        source.onNext(2)

        ts.assertValues(1)
    }

    @Test
    @DisplayName("Resumes relaying values when the other Observable signals true")
    fun openValve() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val ts = ObservableValveLast.create(source, other, false).test()

        source.onNext(1)
        source.onNext(2)

        ts.assertNoValues()

        other.onNext(true)
        source.onNext(3)

        ts.assertValues(2, 3)
    }

    @Test
    @DisplayName("Disposes source and other observers on downstream disposal")
    fun cancellation() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val ts = ObservableValveLast.create(source, other, true).test()

        assertTrue(other.hasObservers())
        assertTrue(source.hasObservers())

        ts.dispose()

        assertFalse(other.hasObservers())
        assertFalse(source.hasObservers())
    }

    @Test
    @DisplayName("Disposes source and other observers on completion")
    fun disposeOnComplete() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        ObservableValveLast.create(source, other, true).test()

        source.onComplete()

        assertFalse(other.hasObservers())
        assertFalse(source.hasObservers())
    }

    @Test
    @DisplayName("Disposes source and other observers on error")
    fun disposeOnError() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        ObservableValveLast.create(source, other, true).test()

        source.onError(RuntimeException())

        assertFalse(other.hasObservers())
        assertFalse(source.hasObservers())
    }

    @Test
    @DisplayName("Error from source is propagated downstream when valve is opened")
    fun sourceError() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val ts = ObservableValveLast.create(source, other, false).test()

        val error = RuntimeException()
        source.onError(error)

        ts.assertNotTerminated()

        other.onNext(true)

        ts.assertError(error)
    }

    @Test
    @DisplayName("Source completion is propagated downstream when valve is opened")
    fun sourceComplete() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val ts = ObservableValveLast.create(source, other, false).test()

        source.onComplete()

        ts.assertNotTerminated()

        other.onNext(true)

        ts.assertComplete()
    }

    @Test
    @DisplayName("Error from valve source is propagated downstream")
    fun valveSourceError() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val ts = ObservableValveLast.create(source, other, true).test()

        val error = RuntimeException()
        other.onError(error)

        ts.assertError(error)
    }

    @Test
    @DisplayName("If valve source completes the downstream terminates with IllegalStateException")
    fun valveSourceComplete() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val ts = ObservableValveLast.create(source, other, true).test()

        other.onComplete()

        ts.assertError { error ->
            error.javaClass == IllegalStateException::class.java && error.message == "The valve source completed unexpectedly."
        }
    }
}

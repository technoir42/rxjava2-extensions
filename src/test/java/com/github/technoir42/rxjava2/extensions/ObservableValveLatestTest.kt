package com.github.technoir42.rxjava2.extensions

import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ObservableValveLatestTest {
    @Test
    fun `Stops relaying values when the other Observable signals false`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val observer = ObservableValveLatest.create(source, other, true).test()

        source.onNext(1)
        other.onNext(false)
        source.onNext(2)

        observer.assertValues(1)
    }

    @Test
    fun `Resumes relaying values when the other Observable signals true`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val observer = ObservableValveLatest.create(source, other, false).test()

        source.onNext(1)
        source.onNext(2)

        observer.assertNoValues()

        other.onNext(true)
        source.onNext(3)

        observer.assertValues(2, 3)
    }

    @Test
    fun `Disposes source and other observers on downstream disposal`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val observer = ObservableValveLatest.create(source, other, true).test()

        assertTrue(other.hasObservers())
        assertTrue(source.hasObservers())

        observer.dispose()

        assertFalse(other.hasObservers())
        assertFalse(source.hasObservers())
    }

    @Test
    fun `Disposes source and other observers on completion`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        ObservableValveLatest.create(source, other, true).test()

        source.onComplete()

        assertFalse(other.hasObservers())
        assertFalse(source.hasObservers())
    }

    @Test
    fun `Disposes source and other observers on error`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        ObservableValveLatest.create(source, other, true).test()

        source.onError(RuntimeException())

        assertFalse(other.hasObservers())
        assertFalse(source.hasObservers())
    }

    @Test
    fun `Error from source is propagated downstream when valve is opened`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val observer = ObservableValveLatest.create(source, other, false).test()

        val error = RuntimeException()
        source.onError(error)

        observer.assertNotTerminated()

        other.onNext(true)

        observer.assertError(error)
    }

    @Test
    fun `Source completion is propagated downstream when valve is opened`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val observer = ObservableValveLatest.create(source, other, false).test()

        source.onComplete()

        observer.assertNotTerminated()

        other.onNext(true)

        observer.assertComplete()
    }

    @Test
    fun `Error from valve source is propagated downstream`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val observer = ObservableValveLatest.create(source, other, true).test()

        val error = RuntimeException()
        other.onError(error)

        observer.assertError(error)
    }

    @Test
    fun `If valve source completes the downstream terminates with IllegalStateException`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val observer = ObservableValveLatest.create(source, other, true).test()

        other.onComplete()

        observer.assertError { error ->
            error.javaClass == IllegalStateException::class.java && error.message == "The valve source completed unexpectedly."
        }
    }

    @Test
    fun `Doesn't emit anything if source didn't emit while valve was closed`() {
        val source = PublishSubject.create<Int>()
        val other = BehaviorSubject.create<Boolean>()

        val observer = ObservableValveLatest.create(source, other, true).test()

        source.onNext(1)
        other.onNext(false)
        other.onNext(true)

        observer.assertValues(1)
    }
}

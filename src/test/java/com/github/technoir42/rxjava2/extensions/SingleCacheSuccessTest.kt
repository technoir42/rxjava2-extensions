package com.github.technoir42.rxjava2.extensions

import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class SingleCacheSuccessTest {
    @Test
    fun `Subscribes to source only once`() {
        val count = AtomicInteger()
        val cache = SingleCacheSuccess.create(Single.fromCallable {
            count.getAndIncrement()
            1
        })

        cache.test().assertResult(1)
        cache.test().assertResult(1)
        assertEquals(1, count.get())
    }

    @Test
    fun `Resubscribes to source after error`() {
        val count = AtomicInteger()
        val cache = SingleCacheSuccess.create(Single.fromCallable {
            if (count.getAndIncrement() == 0) {
                throw IOException()
            }
            1
        })

        cache.test().assertError(IOException::class.java)
        cache.test().assertResult(1)
        assertEquals(2, count.get())
    }

    @Test
    fun normal() {
        val cache = SingleCacheSuccess.create(Single.just(1))

        cache.test().assertResult(1)
        cache.test().assertResult(1)
    }

    @Test
    fun error() {
        val cache = SingleCacheSuccess.create(Single.error<Int>(IOException()))

        cache.test().assertFailure(IOException::class.java)
        cache.test().assertFailure(IOException::class.java)
    }

    @Test
    fun delayed() {
        val ps = PublishSubject.create<Int>()
        val cache = SingleCacheSuccess.create(ps.single(-99))

        val observer1 = cache.test()
        val observer2 = cache.test()

        ps.onNext(1)
        ps.onComplete()

        observer1.assertResult(1)
        observer2.assertResult(1)
    }

    @Test
    fun delayedDisposed() {
        val ps = PublishSubject.create<Int>()
        val cache = SingleCacheSuccess.create(ps.single(-99))

        val observer1 = cache.test()
        val observer2 = cache.test()

        observer1.cancel()

        ps.onNext(1)
        ps.onComplete()

        observer1.assertNoValues().assertNoErrors().assertNotComplete()
        observer2.assertResult(1)
    }

    @Test
    fun crossCancel() {
        val ps = PublishSubject.create<Int>()
        val cache = SingleCacheSuccess.create(ps.single(-99))

        val observer1 = TestSubscriber<Int>()
        val observer2 = object : TestSubscriber<Int>() {
            override fun onNext(t: Int) {
                super.onNext(t)
                observer1.cancel()
            }
        }

        cache.toFlowable().subscribe(observer2)
        cache.toFlowable().subscribe(observer1)

        ps.onNext(1)
        ps.onComplete()

        observer1.assertNoValues().assertNoErrors().assertNotComplete()
        observer2.assertResult(1)
    }

    @Test
    fun crossCancelOnError() {
        val ps = PublishSubject.create<Int>()
        val cache = SingleCacheSuccess.create(ps.single(-99))

        val observer1 = TestSubscriber<Int>()
        val observer2 = object : TestSubscriber<Int>() {
            override fun onError(t: Throwable) {
                super.onError(t)
                observer1.cancel()
            }
        }

        cache.toFlowable().subscribe(observer2)
        cache.toFlowable().subscribe(observer1)

        ps.onError(IOException())

        observer1.assertNoValues().assertNoErrors().assertNotComplete()
        observer2.assertFailure(IOException::class.java)
    }
}

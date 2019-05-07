package com.sch.rxjava2.extensions

import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class SingleMapErrorTest {
    @Test
    fun `Signals onError with error returned by mapper`() {
        val upstreamError = Exception()
        val mappedError = Exception(upstreamError)
        Single.error<Any>(upstreamError)
                .mapError { mappedError }
                .test()
                .assertError(mappedError)
    }

    @Test
    fun `Signals onError with CompositeException if mapper throws an exception`() {
        val upstreamError = Exception()
        val mapperError = Exception()
        val observer = Single.error<Any>(upstreamError)
                .mapError { throw mapperError }
                .test()
                .assertError(CompositeException::class.java)

        val error = observer.errors().first() as CompositeException
        assertEquals(2, error.exceptions.size)
        assertSame(upstreamError, error.exceptions[0])
        assertSame(mapperError, error.exceptions[1])
    }

    @Test
    fun `Passes-through the success value`() {
        Single.just(1)
                .mapError { RuntimeException() }
                .test()
                .assertResult(1)
    }
}

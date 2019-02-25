package com.sch.rxjava2.extensions

import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class SingleMapErrorTest {
    @Test
    @DisplayName("Signals onError with error returned by mapper")
    fun map() {
        val upstreamError = Exception()
        val mappedError = Exception(upstreamError)
        Single.error<Any>(upstreamError)
                .mapError { mappedError }
                .test()
                .assertError(mappedError)
    }

    @Test
    @DisplayName("Signals onError with CompositeException if mapper throws an exception")
    fun mapperThrows() {
        val upstreamError = Exception()
        val mapperError = Exception()
        val ts = Single.error<Any>(upstreamError)
                .mapError { throw mapperError }
                .test()
                .assertError(CompositeException::class.java)

        val error = ts.errors().first() as CompositeException
        assertEquals(2, error.exceptions.size)
        assertSame(upstreamError, error.exceptions[0])
        assertSame(mapperError, error.exceptions[1])
    }

    @Test
    @DisplayName("Passes-through the success value")
    fun success() {
        Single.just(1)
                .mapError { RuntimeException() }
                .test()
                .assertResult(1)
    }
}

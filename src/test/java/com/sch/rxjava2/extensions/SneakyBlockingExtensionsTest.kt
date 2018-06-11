package com.sch.rxjava2.extensions

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException

class SneakyBlockingExtensionsTest {
    @Test
    fun singleSuccess() {
        val result = Single.just(1).sneakyGet()
        assertEquals(1, result)
    }

    @Test
    fun singleError() {
        assertThrows<IOException> {
            Single.error<Any>(IOException()).sneakyGet()
        }
    }

    @Test
    fun maybeSuccess() {
        val result = Maybe.just(1).sneakyGet()
        assertEquals(1, result)
    }

    @Test
    fun maybeComplete() {
        val maybe = Maybe.empty<Any>()
        assertEquals(null, maybe.sneakyGet())
    }

    @Test
    fun maybeError() {
        assertThrows<IOException> {
            Maybe.error<Any>(IOException()).sneakyGet()
        }
    }

    @Test
    fun completableComplete() {
        Completable.complete().sneakyAwait()
    }

    @Test
    fun completableError() {
        assertThrows<IOException> {
            Completable.error(IOException()).sneakyAwait()
        }
    }
}

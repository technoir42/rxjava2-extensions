package com.sch.rxjava2.extensions

import io.reactivex.exceptions.OnErrorNotImplementedException
import io.reactivex.plugins.RxJavaPlugins
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException

class FailFastErrorHandlerTest {
    private val uncaughtExceptions = mutableListOf<Throwable>()
    private var previousUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    @BeforeEach
    fun setUp() {
        RxJavaPlugins.setErrorHandler(FailFastErrorHandler())

        previousUncaughtExceptionHandler = Thread.currentThread().uncaughtExceptionHandler
        Thread.currentThread().uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, e -> uncaughtExceptions += e }
    }

    @AfterEach
    fun tearDown() {
        RxJavaPlugins.setErrorHandler(null)

        Thread.currentThread().uncaughtExceptionHandler = previousUncaughtExceptionHandler
    }

    @Test
    fun exception() {
        RxJavaPlugins.onError(IOException())

        assertTrue(uncaughtExceptions.isEmpty())
    }

    @Test
    fun fatalError() {
        val error = NoSuchFieldError()
        RxJavaPlugins.onError(error)

        assertEquals(listOf(error), uncaughtExceptions)
    }

    @Test
    fun programmingError() {
        val error = OnErrorNotImplementedException(IOException())
        RxJavaPlugins.onError(error)

        assertEquals(listOf(error), uncaughtExceptions)
    }
}

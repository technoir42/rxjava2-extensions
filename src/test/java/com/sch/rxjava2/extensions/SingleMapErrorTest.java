package com.sch.rxjava2.extensions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.reactivex.Single;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.observers.TestObserver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class SingleMapErrorTest {
    @Test
    @DisplayName("Signals onError with error returned by mapper")
    void map() {
        Exception upstreamError = new Exception();
        Exception mappedError = new Exception(upstreamError);
        new SingleMapError<>(Single.error(upstreamError), cause -> mappedError)
                .test()
                .assertError(mappedError);
    }

    @Test
    @DisplayName("Signals onError with CompositeException if mapper throws an exception")
    void mapperThrows() {
        Exception upstreamError = new Exception();
        Exception mapperError = new Exception();
        TestObserver<Object> ts = new SingleMapError<>(Single.error(upstreamError), cause -> {
            throw mapperError;
        })
                .test()
                .assertError(CompositeException.class);

        CompositeException error = (CompositeException) ts.errors().get(0);
        assertEquals(2, error.getExceptions().size());
        assertSame(upstreamError, error.getExceptions().get(0));
        assertSame(mapperError, error.getExceptions().get(1));
    }

    @Test
    @DisplayName("Passes-through the success value")
    void success() {
        new SingleMapError<>(Single.just(1), cause -> new RuntimeException())
                .test()
                .assertResult(1);
    }
}

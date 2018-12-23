package com.sch.rxjava2.extensions;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

final class SingleMapError<T> extends Single<T> {
    private final Single<T> source;
    private final Function<Throwable, Throwable> errorMapper;

    SingleMapError(Single<T> source, Function<Throwable, Throwable> errorMapper) {
        this.source = source;
        this.errorMapper = errorMapper;
    }

    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        source.subscribe(new MapErrorObserver<>(observer, errorMapper));
    }

    private static class MapErrorObserver<T> implements SingleObserver<T> {
        private final SingleObserver<? super T> actual;
        private final Function<Throwable, Throwable> errorMapper;

        MapErrorObserver(SingleObserver<? super T> actual, Function<Throwable, Throwable> errorMapper) {
            this.actual = actual;
            this.errorMapper = errorMapper;
        }

        @Override
        public void onSubscribe(Disposable d) {
            actual.onSubscribe(d);
        }

        @Override
        public void onSuccess(T value) {
            actual.onSuccess(value);
        }

        @Override
        public void onError(Throwable e) {
            Throwable error;
            try {
                error = errorMapper.apply(e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                actual.onError(new CompositeException(e, ex));
                return;
            }

            actual.onError(error);
        }
    }
}

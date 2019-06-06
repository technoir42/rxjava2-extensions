package com.github.technoir42.rxjava2.extensions;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.plugins.RxJavaPlugins;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.technoir42.rxjava2.extensions.Util.requireNonNull;

/**
 * Stores the success value from the source Single and replays it to observers.
 */
public final class SingleCacheSuccess<T> extends Single<T> implements SingleObserver<T> {
    @SuppressWarnings("rawtypes")
    private static final CacheDisposable[] EMPTY = new CacheDisposable[0];
    @SuppressWarnings("rawtypes")
    private static final CacheDisposable[] TERMINATED = new CacheDisposable[0];

    private final Single<T> source;
    private final AtomicBoolean wip = new AtomicBoolean();
    private final AtomicReference<CacheDisposable<T>[]> observers;
    private T value;

    @NonNull
    public static <T> Single<T> create(@NonNull Single<T> source) {
        requireNonNull(source, "source is null");
        return RxJavaPlugins.onAssembly(new SingleCacheSuccess<>(source));
    }

    @SuppressWarnings("unchecked")
    private SingleCacheSuccess(Single<T> source) {
        this.source = source;
        observers = new AtomicReference<CacheDisposable<T>[]>(EMPTY);
    }

    @Override
    protected void subscribeActual(SingleObserver<? super T> observer) {
        final CacheDisposable<T> d = new CacheDisposable<>(observer, this);
        observer.onSubscribe(d);

        if (add(d)) {
            if (d.isDisposed()) {
                remove(d);
            }
        } else {
            observer.onSuccess(value);
            return;
        }

        if (!wip.getAndSet(true)) {
            source.subscribe(this);
        }
    }

    @Override
    public void onSubscribe(Disposable d) {
        // not supported by this operator
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onSuccess(T value) {
        this.value = value;

        for (CacheDisposable<T> observer : observers.getAndSet(TERMINATED)) {
            if (!observer.isDisposed()) {
                observer.actual.onSuccess(value);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onError(Throwable e) {
        wip.set(false);

        for (CacheDisposable<T> observer : observers.getAndSet(EMPTY)) {
            if (!observer.isDisposed()) {
                observer.actual.onError(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean add(CacheDisposable<T> observer) {
        for (; ; ) {
            final CacheDisposable<T>[] a = observers.get();
            if (a == TERMINATED) {
                return false;
            }
            final int n = a.length;
            final CacheDisposable<T>[] b = new CacheDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = observer;
            if (observers.compareAndSet(a, b)) {
                return true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void remove(CacheDisposable<T> observer) {
        for (; ; ) {
            final CacheDisposable<T>[] a = observers.get();
            final int n = a.length;
            if (n == 0) {
                return;
            }

            int j = -1;
            for (int i = 0; i < n; i++) {
                if (a[i] == observer) {
                    j = i;
                    break;
                }
            }

            if (j < 0) {
                return;
            }

            final CacheDisposable<T>[] b;
            if (n == 1) {
                b = EMPTY;
            } else {
                b = new CacheDisposable[n - 1];
                System.arraycopy(a, 0, b, 0, j);
                System.arraycopy(a, j + 1, b, j, n - j - 1);
            }
            if (observers.compareAndSet(a, b)) {
                return;
            }
        }
    }

    private static final class CacheDisposable<T> extends AtomicBoolean implements Disposable {
        private static final long serialVersionUID = 4746876330948546833L;

        final SingleObserver<? super T> actual;
        private final SingleCacheSuccess<T> parent;

        CacheDisposable(SingleObserver<? super T> actual, SingleCacheSuccess<T> parent) {
            this.actual = actual;
            this.parent = parent;
        }

        @Override
        public boolean isDisposed() {
            return get();
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                parent.remove(this);
            }
        }
    }
}

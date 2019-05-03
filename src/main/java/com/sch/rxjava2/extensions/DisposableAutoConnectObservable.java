package com.sch.rxjava2.extensions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.observables.ConnectableObservable;

import static com.sch.rxjava2.extensions.Util.requireNonNull;

/**
 * Wraps a {@link ConnectableObservable} and calls its connect() method once
 * the specified number of Observers have subscribed.
 * <p>
 * The internal connection can be terminated by calling {@link #dispose()}.
 *
 * @param <T> the type of the items emitted by the Observable.
 */
public final class DisposableAutoConnectObservable<T> extends DisposableObservable<T> {
    private final ConnectableObservable<? extends T> source;
    private final int numberOfObservers;
    private final AtomicInteger clients = new AtomicInteger();
    private final AtomicReference<Disposable> connection = new AtomicReference<>();
    private final Consumer<Disposable> connectionCallback = disposable -> DisposableHelper.setOnce(connection, disposable);

    /**
     * Returns a {@link DisposableObservable} that automatically connects (at most once) to source {@link ConnectableObservable}
     * when the specified number of Observers subscribe to it.
     *
     * @param source            the upstream.
     * @param numberOfObservers the number of observers to await before calling connect
     *                          on the {@link ConnectableObservable}. A non-positive value indicates
     *                          an immediate connection.
     * @param <T>               the type of the items emitted by the Observable.
     * @return the new Observable.
     */
    @NonNull
    public static <T> DisposableObservable<T> create(@NonNull ConnectableObservable<? extends T> source, int numberOfObservers) {
        requireNonNull(source, "source is null");
        return new DisposableAutoConnectObservable<>(source, numberOfObservers);
    }

    private DisposableAutoConnectObservable(ConnectableObservable<? extends T> source, int numberOfObservers) {
        this.source = source;
        this.numberOfObservers = numberOfObservers;

        if (numberOfObservers <= 0) {
            source.connect(connectionCallback);
        }
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(observer);
        if (clients.incrementAndGet() == numberOfObservers) {
            source.connect(connectionCallback);
        }
    }

    @Override
    public boolean isDisposed() {
        return DisposableHelper.isDisposed(connection.get());
    }

    @Override
    public void dispose() {
        DisposableHelper.dispose(connection);
    }
}

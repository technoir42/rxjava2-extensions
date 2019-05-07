package com.sch.rxjava2.extensions;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.util.AtomicThrowable;
import io.reactivex.plugins.RxJavaPlugins;

import java.util.concurrent.atomic.AtomicReference;

import static com.sch.rxjava2.extensions.Util.requireNonNull;

/**
 * Relays values from source until the other Observable signals false and resumes if the other
 * Observable signals true again. Drops all values except the latest while valve is closed.
 */
public final class ObservableValveLatest<T> extends Observable<T> {
    private final Observable<T> source;
    private final Observable<Boolean> other;
    private final boolean defaultOpen;

    @NonNull
    public static <T> Observable<T> create(@NonNull Observable<T> source, @NonNull Observable<Boolean> other, boolean defaultOpen) {
        requireNonNull(source, "source is null");
        requireNonNull(other, "other is null");
        return RxJavaPlugins.onAssembly(new ObservableValveLatest<>(source, other, defaultOpen));
    }

    private ObservableValveLatest(Observable<T> source, Observable<Boolean> other, boolean defaultOpen) {
        this.source = source;
        this.other = other;
        this.defaultOpen = defaultOpen;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        source.subscribe(new SourceObserver<>(observer, other, defaultOpen));
    }

    private static final class SourceObserver<T> implements Observer<T>, Disposable {
        private final Observer<? super T> actual;
        private final OtherObserver otherObserver;
        private final AtomicReference<Disposable> source = new AtomicReference<>();
        private final AtomicReference<T> last = new AtomicReference<>();
        private final AtomicThrowable error = new AtomicThrowable();
        private volatile boolean done;
        private volatile boolean gate;
        private volatile boolean disposed;

        SourceObserver(Observer<? super T> actual, Observable<Boolean> other, boolean defaultOpen) {
            this.actual = actual;
            gate = defaultOpen;
            otherObserver = new OtherObserver(this);
            other.subscribe(otherObserver);
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public void dispose() {
            disposed = true;
            DisposableHelper.dispose(source);
            DisposableHelper.dispose(otherObserver);
        }

        @Override
        public void onSubscribe(Disposable d) {
            actual.onSubscribe(this);
            DisposableHelper.setOnce(source, d);
        }

        @Override
        public void onNext(T t) {
            last.set(t);
            drain();
        }

        @Override
        public void onError(Throwable e) {
            if (error.addThrowable(e)) {
                drain();
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        @Override
        public void onComplete() {
            done = true;
            drain();
        }

        void change(boolean gate) {
            this.gate = gate;
            drain();
        }

        private void drain() {
            if (!gate) {
                return;
            }

            if (error.get() != null) {
                Throwable ex = error.terminate();
                DisposableHelper.dispose(source);
                DisposableHelper.dispose(otherObserver);
                actual.onError(ex);
            } else {
                T last = this.last.getAndSet(null);
                if (last != null) {
                    actual.onNext(last);
                }
                if (done) {
                    DisposableHelper.dispose(otherObserver);
                    actual.onComplete();
                }
            }
        }
    }

    private static final class OtherObserver extends AtomicReference<Disposable> implements Observer<Boolean> {
        private final SourceObserver<?> sourceObserver;

        OtherObserver(SourceObserver<?> sourceObserver) {
            this.sourceObserver = sourceObserver;
        }

        @Override
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this, d);
        }

        @Override
        public void onNext(Boolean value) {
            sourceObserver.change(value);
        }

        @Override
        public void onError(Throwable e) {
            sourceObserver.onError(e);
        }

        @Override
        public void onComplete() {
            sourceObserver.onError(new IllegalStateException("The valve source completed unexpectedly."));
        }
    }
}

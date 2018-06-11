package com.sch.rxjava2.extensions;

import java.util.concurrent.CountDownLatch;

import io.reactivex.CompletableObserver;
import io.reactivex.MaybeObserver;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.BlockingHelper;

public final class SneakyBlockingObserver<T> extends CountDownLatch implements SingleObserver<T>, MaybeObserver<T>, CompletableObserver {
    private T value;
    private Throwable error;
    private Disposable d;
    private volatile boolean cancelled;

    public SneakyBlockingObserver() {
        super(1);
    }

    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
        if (cancelled) {
            d.dispose();
        }
    }

    @Override
    public void onSuccess(T t) {
        value = t;
        countDown();
    }

    @Override
    public void onComplete() {
        countDown();
    }

    @Override
    public void onError(Throwable e) {
        error = e;
        countDown();
    }

    public T sneakyGet() {
        if (getCount() != 0) {
            try {
                BlockingHelper.verifyNonBlocking();
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw Util.sneakyThrow(ex);
            }
        }
        Throwable ex = error;
        if (ex != null) {
            throw Util.sneakyThrow(ex);
        }
        return value;
    }

    private void dispose() {
        cancelled = true;
        Disposable d = this.d;
        if (d != null) {
            d.dispose();
        }
    }
}

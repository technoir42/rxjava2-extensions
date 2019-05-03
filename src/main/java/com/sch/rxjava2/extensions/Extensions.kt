@file:Suppress("NOTHING_TO_INLINE")

package com.sch.rxjava2.extensions

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.observables.ConnectableObservable
import java.util.concurrent.atomic.AtomicReference

fun <T : Any> Single<T>.sneakyGet(): T {
    return subscribeWith(SneakyBlockingObserver<T>()).sneakyGet()
}

fun <T : Any> Maybe<T>.sneakyGet(): T? {
    return subscribeWith(SneakyBlockingObserver<T>()).sneakyGet()
}

fun Completable.sneakyAwait() {
    subscribeWith(SneakyBlockingObserver<Any>()).sneakyGet()
}

inline fun <T : Any> ConnectableObservable<T>.autoConnectDisposable(numberOfObservers: Int = 1): DisposableObservable<T> {
    return DisposableAutoConnectObservable.create(this, numberOfObservers)
}

inline fun <T> Single<T>.mapError(noinline mapper: (Throwable) -> Throwable): Single<T> {
    return SingleMapError.create(this, mapper)
}

fun <T> Observable<T>.pairwiseWithPrevious(): Observable<Pair<T, T?>> {
    val previous = AtomicReference<T>()
    return map { item -> Pair(item, previous.getAndSet(item)) }
}

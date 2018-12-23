@file:Suppress("NOTHING_TO_INLINE")

package com.sch.rxjava2.extensions

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observables.ConnectableObservable
import io.reactivex.plugins.RxJavaPlugins
import java.util.concurrent.atomic.AtomicReference

inline fun <reified T : Any> Observable<*>.ofType(): Observable<T> {
    return ofType(T::class.java)
}

inline operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}

fun <T : Any> Single<T>.sneakyGet(): T {
    return subscribeWith(SneakyBlockingObserver<T>()).sneakyGet()
}

fun <T : Any> Maybe<T>.sneakyGet(): T? {
    return subscribeWith(SneakyBlockingObserver<T>()).sneakyGet()
}

fun Completable.sneakyAwait() {
    subscribeWith(SneakyBlockingObserver<Any>()).sneakyGet()
}

fun <T : Any> ConnectableObservable<T>.autoConnectDisposable(numberOfObservers: Int = 1): DisposableObservable<T> {
    return DisposableAutoConnectObservable.create(this, numberOfObservers)
}

fun <T> Single<T>.mapError(mapper: (Throwable) -> Throwable): Single<T> {
    return RxJavaPlugins.onAssembly(SingleMapError(this, mapper))
}

fun <T> Observable<T>.pairwiseWithPrevious(): Observable<Pair<T, T?>> {
    val previous = AtomicReference<T>()
    return map { item -> Pair(item, previous.getAndSet(item)) }
}

@file:Suppress("NOTHING_TO_INLINE")

package com.sch.rxjava2.extensions

import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observables.ConnectableObservable

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

@file:Suppress("NOTHING_TO_INLINE")

package com.sch.rxjava2.extensions

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

inline fun <reified T : Any> Observable<*>.ofType(): Observable<T> {
    return ofType(T::class.java)
}

inline operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}

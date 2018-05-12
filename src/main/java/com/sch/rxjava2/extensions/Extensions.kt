package com.sch.rxjava2.extensions

import io.reactivex.Observable

inline fun <reified T : Any> Observable<*>.ofType(): Observable<T> {
    return ofType(T::class.java)
}

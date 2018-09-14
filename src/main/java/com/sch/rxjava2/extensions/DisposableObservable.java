package com.sch.rxjava2.extensions;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * An {@link Observable} that also offers a means to dispose it.
 *
 * @param <T> the type of the items emitted by the Observable.
 */
public abstract class DisposableObservable<T> extends Observable<T> implements Disposable {
}

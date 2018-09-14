package com.sch.rxjava2.extensions;

import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.PublishSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisposableAutoConnectObservableTest {
    @Test
    @DisplayName("Connects at the required number of observers")
    void testConnect() {
        PublishSubject<Integer> source = PublishSubject.create();
        ConnectableObservable<Integer> connectableSource = source.publish();

        DisposableObservable<Integer> observable = DisposableAutoConnectObservable.create(connectableSource, 2);

        assertFalse(source.hasObservers());
        observable.test();
        assertFalse(source.hasObservers());
        observable.test();
        assertTrue(source.hasObservers());
    }

    @Test
    @DisplayName("Connects immediately if numberOfObservers is non-positive")
    void testConnectImmediately() {
        PublishSubject<Integer> source = PublishSubject.create();
        ConnectableObservable<Integer> connectableSource = source.publish();

        DisposableAutoConnectObservable.create(connectableSource, 0);

        assertTrue(source.hasObservers());
    }

    @Test
    @DisplayName("Dispose terminates connection to source")
    void testDispose() {
        PublishSubject<Integer> source = PublishSubject.create();
        ConnectableObservable<Integer> connectableSource = source.publish();

        DisposableObservable<Integer> observable = DisposableAutoConnectObservable.create(connectableSource, 1);

        observable.test().cancel();
        assertTrue(source.hasObservers());
        assertFalse(observable.isDisposed());

        observable.dispose();
        assertFalse(source.hasObservers());
        assertTrue(observable.isDisposed());
    }
}

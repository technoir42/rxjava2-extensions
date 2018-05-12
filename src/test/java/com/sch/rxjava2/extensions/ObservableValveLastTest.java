package com.sch.rxjava2.extensions;

import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObservableValveLastTest {
    @Test
    @DisplayName("Stops relaying values when the other Observable signals false")
    void closeValve() {
        Subject<Integer> source = PublishSubject.create();
        Subject<Boolean> other = BehaviorSubject.create();

        TestObserver<Integer> ts = source.compose(Transformers.valveLast(other, true)).test();

        source.onNext(1);
        other.onNext(false);
        source.onNext(2);

        ts.assertValues(1);
    }

    @Test
    @DisplayName("Resumes relaying values when the other Observable signals true")
    void openValve() {
        Subject<Integer> source = PublishSubject.create();
        Subject<Boolean> other = BehaviorSubject.create();

        TestObserver<Integer> ts = source.compose(Transformers.valveLast(other, false)).test();

        source.onNext(1);
        source.onNext(2);

        ts.assertNoValues();

        other.onNext(true);
        source.onNext(3);

        ts.assertValues(2, 3);
    }

    @Test
    @DisplayName("Disposes source and other observers on downstream disposal")
    void cancellation() {
        Subject<Integer> source = PublishSubject.create();
        Subject<Boolean> other = BehaviorSubject.create();

        TestObserver<Integer> ts = source.compose(Transformers.valveLast(other, true)).test();

        assertTrue(other.hasObservers());
        assertTrue(source.hasObservers());

        ts.dispose();

        assertFalse(other.hasObservers());
        assertFalse(source.hasObservers());
    }

    @Test
    @DisplayName("Disposes source and other observers on completion")
    void disposeOnComplete() {
        Subject<Integer> source = PublishSubject.create();
        Subject<Boolean> other = BehaviorSubject.create();

        source.compose(Transformers.valveLast(other, true)).test();

        source.onComplete();

        assertFalse(other.hasObservers());
        assertFalse(source.hasObservers());
    }

    @Test
    @DisplayName("Disposes source and other observers on error")
    void disposeOnError() {
        Subject<Integer> source = PublishSubject.create();
        Subject<Boolean> other = BehaviorSubject.create();

        source.compose(Transformers.valveLast(other, true)).test();

        source.onError(new RuntimeException());

        assertFalse(other.hasObservers());
        assertFalse(source.hasObservers());
    }

    @Test
    @DisplayName("Error from source is propagated downstream when valve is opened")
    void sourceError() {
        Subject<Integer> source = PublishSubject.create();
        Subject<Boolean> other = BehaviorSubject.create();

        TestObserver<Integer> ts = source.compose(Transformers.valveLast(other, false)).test();

        Throwable error = new RuntimeException();
        source.onError(error);

        ts.assertNotTerminated();

        other.onNext(true);

        ts.assertError(error);
    }

    @Test
    @DisplayName("Source completion is propagated downstream when valve is opened")
    void sourceComplete() {
        Subject<Integer> source = PublishSubject.create();
        Subject<Boolean> other = BehaviorSubject.create();

        TestObserver<Integer> ts = source.compose(Transformers.valveLast(other, false)).test();

        source.onComplete();

        ts.assertNotTerminated();

        other.onNext(true);

        ts.assertComplete();
    }

    @Test
    @DisplayName("Error from valve source is propagated downstream")
    void valveSourceError() {
        Subject<Integer> source = PublishSubject.create();
        Subject<Boolean> other = BehaviorSubject.create();

        TestObserver<Integer> ts = source.compose(Transformers.valveLast(other, true)).test();

        Throwable error = new RuntimeException();
        other.onError(error);

        ts.assertError(error);
    }

    @Test
    @DisplayName("If valve source completes the downstream terminates with IllegalStateException")
    void valveSourceComplete() {
        Subject<Integer> source = PublishSubject.create();
        Subject<Boolean> other = BehaviorSubject.create();

        TestObserver<Integer> ts = source.compose(Transformers.valveLast(other, true)).test();

        other.onComplete();

        ts.assertError(error -> error.getClass() == IllegalStateException.class &&
                "The valve source completed unexpectedly.".equals(error.getMessage()));
    }
}

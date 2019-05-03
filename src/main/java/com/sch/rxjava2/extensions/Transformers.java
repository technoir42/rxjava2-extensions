package com.sch.rxjava2.extensions;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.SchedulerSupport;

public final class Transformers {
    /**
     * Relays values until the other Observable signals false and resumes if the other
     * Observable signals true again. Drops all values except the last while valve is closed.
     *
     * @param other the other source.
     * @param <T>   the value type of the main source.
     * @return the new ObservableTransformer instance.
     * @throws NullPointerException if {@code other} is null.
     */
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T> ObservableTransformer<T, T> valveLast(@NonNull Observable<Boolean> other) {
        return valveLast(other, true);
    }

    /**
     * Relays values until the other Observable signals false and resumes if the other
     * Observable signals true again. Drops all values except the last while valve is closed.
     *
     * @param other       the other source.
     * @param defaultOpen whether the valve should start open.
     * @param <T>         the value type of the main source.
     * @return the new ObservableTransformer instance.
     * @throws NullPointerException if {@code other} is null.
     */
    @NonNull
    @SchedulerSupport(SchedulerSupport.NONE)
    public static <T> ObservableTransformer<T, T> valveLast(@NonNull Observable<Boolean> other, boolean defaultOpen) {
        return upstream -> ObservableValveLast.create(upstream, other, defaultOpen);
    }

    private Transformers() {
    }
}

package com.sch.rxjava2.extensions;

import io.reactivex.annotations.NonNull;

final class Util {
    @NonNull
    static <T> T requireNonNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }
}

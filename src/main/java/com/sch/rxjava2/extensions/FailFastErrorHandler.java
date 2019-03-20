package com.sch.rxjava2.extensions;

import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;

/**
 * RxJava error handler that fails fast on programming errors but ignores normal {@link UndeliverableException}s
 * which typically occur after flow has been cancelled/disposed.
 *
 * @see RxJavaPlugins#setErrorHandler(Consumer)
 */
public final class FailFastErrorHandler implements Consumer<Throwable> {
    @Override
    public void accept(Throwable error) {
        if (!(error instanceof UndeliverableException)) {
            uncaughtException(error);
        } else if (isFatal(error.getCause())) {
            uncaughtException(error.getCause());
        }
    }

    private void uncaughtException(Throwable error) {
        Thread thread = Thread.currentThread();
        thread.getUncaughtExceptionHandler().uncaughtException(thread, error);
    }

    private boolean isFatal(Throwable error) {
        // See Exceptions.throwIfFatal
        return error instanceof VirtualMachineError ||
                error instanceof ThreadDeath ||
                error instanceof LinkageError;
    }
}

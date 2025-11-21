package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

/**
 * <p>
 * Description: CRunnable
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CRunnable extends Runnable {

    @Override
    @SneakyThrows
    default void run() {
        runThrowable();
    }

    void runThrowable() throws Throwable;

    CRunnable EMPTY = () -> {};

    static void run(CRunnable runnable) {
        runnable.run();
    }

    static Runnable convert(CRunnable runnable) {
        return () -> run(runnable);
    }

}

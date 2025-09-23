package com.c332030.ctool4j.core.function;

import lombok.Lombok;

/**
 * <p>
 * Description: CRunnable
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CRunnable {

    void run() throws Exception;

    static void run(CRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    static Runnable convert(CRunnable runnable) {
        return () -> run(runnable);
    }

}

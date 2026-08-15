package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

/**
 * <p>
 * Description: CRunnable
 * </p>
 * <p>
 * 注意：run 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
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

    static void run(Runnable runnable) {
        if(null == runnable) {
            return;
        }
        runnable.run();
    }

    static Runnable convert(CRunnable runnable) {
        return () -> run(runnable);
    }

}

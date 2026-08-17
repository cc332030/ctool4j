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

    /**
     * 执行任务（受检异常由内部包装处理）
     */
    @Override
    @SneakyThrows
    default void run() {
        runThrowable();
    }

    /**
     * 执行任务，可抛出受检异常
     * @throws Throwable 执行过程中可能抛出的异常
     */
    void runThrowable() throws Throwable;

    /**
     * 空实现常量
     */
    CRunnable EMPTY = () -> {};

    /**
     * 执行任务（runnable 为空时不做处理）
     * @param runnable 任务
     */
    static void run(Runnable runnable) {
        if(null == runnable) {
            return;
        }
        runnable.run();
    }

    /**
     * 转换为 JDK Runnable
     * @param runnable 自定义任务
     * @return JDK Runnable
     */
    static Runnable convert(CRunnable runnable) {
        return () -> run(runnable);
    }

}

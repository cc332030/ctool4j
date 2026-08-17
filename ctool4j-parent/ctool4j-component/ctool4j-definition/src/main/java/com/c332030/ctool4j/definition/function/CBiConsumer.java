package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.BiConsumer;

/**
 * <p>
 * Description: CBiConsumer
 * </p>
 * <p>
 * 注意：accept 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * @since 2025/2/21
 */
@FunctionalInterface
public interface CBiConsumer<T, U> extends BiConsumer<T, U> {

    /**
     * accept 方法名常量
     */
    String ACCEPT = "accept";

    /**
     * 消费两个参数（受检异常由内部包装处理）
     * @param t 第一个参数
     * @param u 第二个参数
     */
    @Override
    @SneakyThrows
    default void accept(T t, U u) {
        acceptThrowable(t, u);
    }

    /**
     * 消费两个参数，可抛出受检异常
     * @param t 第一个参数
     * @param u 第二个参数
     * @throws Throwable 处理过程中可能抛出的异常
     */
    void acceptThrowable(T t, U u) throws Throwable;

    /**
     * 空实现常量
     */
    CBiConsumer<Object, Object> EMPTY = (t, u) -> {};

    /**
     * 空实现
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return 空实现
     */
    @SuppressWarnings("unchecked")
    static <T, U> CBiConsumer<T, U> empty() {
        return (CBiConsumer<T, U>)EMPTY;
    }

    /**
     * 消费两个参数（consumer 为空时不做处理）
     * @param consumer 消费者
     * @param t 第一个参数
     * @param u 第二个参数
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     */
    static <T, U> void accept(BiConsumer<T, U> consumer, T t, U u) {
        if(null == consumer) {
            return;
        }
        consumer.accept(t, u);
    }

    /**
     * 转换为 JDK BiConsumer
     * @param consumer 自定义消费者
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @return JDK BiConsumer
     */
    static <T, U> BiConsumer<T, U> convert(CBiConsumer<T, U> consumer) {
        return (t, u) -> accept(consumer, t, u);
    }

}

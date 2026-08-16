package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.Consumer;

/**
 * <p>
 * Description: CConsumer
 * </p>
 * <p>
 * 注意：accept 方法内部使用 @SneakyThrows 包装受检异常，调用方无法从签名感知，需自行处理实际异常（设计取舍）
 * </p>
 *
 * @since 2025/9/28
 */
@FunctionalInterface
public interface CConsumer<T> extends Consumer<T> {

    /**
     * 消费参数（受检异常由内部包装处理）
     * @param t 参数
     */
    @Override
    @SneakyThrows
    default void accept(T t) {
        acceptThrowable(t);
    }

    /**
     * 消费参数，可抛出受检异常
     * @param t 参数
     * @throws Throwable 处理过程中可能抛出的异常
     */
    void acceptThrowable(T t) throws Throwable;

    /**
     * 空实现常量
     */
    CConsumer<Object> EMPTY = (t) -> {};

    /**
     * 空实现
     * @param <T> 参数类型
     * @return 空实现
     */
    @SuppressWarnings("unchecked")
    static <T> CConsumer<T> empty() {
        return (CConsumer<T>)EMPTY;
    }

    /**
     * 消费参数（consumer 为空时不做处理）
     * @param consumer 消费者
     * @param t 参数
     */
    static <T> void accept(Consumer<T> consumer, T t) {
        if(null == consumer) {
            return;
        }
        consumer.accept(t);
    }

    /**
     * 转换为 JDK Consumer
     * @param consumer 自定义消费者
     * @param <T> 参数类型
     * @return JDK Consumer
     */
    static <T> Consumer<T> convert(CConsumer<T> consumer) {
        return t -> accept(consumer, t);
    }

}

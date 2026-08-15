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

    @Override
    @SneakyThrows
    default void accept(T t) {
        acceptThrowable(t);
    }

    void acceptThrowable(T t) throws Throwable;

    CConsumer<Object> EMPTY = (t) -> {};

    @SuppressWarnings("unchecked")
    static <T> CConsumer<T> empty() {
        return (CConsumer<T>)EMPTY;
    }

    static <T> void accept(Consumer<T> consumer, T t) {
        if(null == consumer) {
            return;
        }
        consumer.accept(t);
    }

    static <T> Consumer<T> convert(CConsumer<T> consumer) {
        return t -> accept(consumer, t);
    }

}

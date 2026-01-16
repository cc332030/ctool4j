package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.BiConsumer;

/**
 * <p>
 * Description: CBiConsumer
 * </p>
 *
 * @since 2025/2/21
 */
@FunctionalInterface
public interface CBiConsumer<T, U> extends BiConsumer<T, U> {

    String ACCEPT = "accept";

    @Override
    @SneakyThrows
    default void accept(T t, U u) {
        acceptThrowable(t, u);
    }

    void acceptThrowable(T t, U u) throws Throwable;

    CBiConsumer<Object, Object> EMPTY = (t, u) -> {};

    @SuppressWarnings("unchecked")
    static <T, U> CBiConsumer<T, U> empty() {
        return (CBiConsumer<T, U>)EMPTY;
    }

    static <T, U> void accept(BiConsumer<T, U> consumer, T t, U u) {
        if(null == consumer) {
            return;
        }
        consumer.accept(t, u);
    }

    static <T, U> BiConsumer<T, U> convert(CBiConsumer<T, U> consumer) {
        return (t, u) -> accept(consumer, t, u);
    }

}

package com.c332030.ctool4j.core.function;

import lombok.Lombok;

import java.util.function.BiConsumer;

/**
 * <p>
 * Description: CBiConsumer
 * </p>
 *
 * @since 2025/2/21
 */
@FunctionalInterface
public interface CBiConsumer<T, U> {

    void accept(T t, U u) throws Throwable;

    CBiConsumer<?, ?> EMPTY = (t, u) -> {};

    @SuppressWarnings("unchecked")
    static <T, U> CBiConsumer<T, U> empty() {
        return (CBiConsumer<T, U>)EMPTY;
    }

    static <T, U> void accept(CBiConsumer<T, U> consumer, T t, U u) {
        try {
            consumer.accept(t, u);
        } catch (Throwable e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    static <T, U> BiConsumer<T, U> convert(CBiConsumer<T, U> consumer) {
        return (t, u) -> accept(consumer, t, u);
    }

}

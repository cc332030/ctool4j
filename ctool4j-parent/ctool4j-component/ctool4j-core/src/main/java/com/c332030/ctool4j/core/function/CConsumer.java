package com.c332030.ctool4j.core.function;

import lombok.Lombok;
import lombok.SneakyThrows;

import java.util.function.Consumer;

/**
 * <p>
 * Description: CConsumer
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

    static <T> void accept(CConsumer<T> consumer, T t) {
        try {
            consumer.accept(t);
        } catch (Throwable e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    static <T> Consumer<T> convert(CConsumer<T> consumer) {
        return t -> accept(consumer, t);
    }

}

package com.c332030.ctool4j.core.function;

import lombok.Lombok;

import java.util.function.Consumer;

/**
 * <p>
 * Description: CConsumer
 * </p>
 *
 * @since 2025/9/28
 */
@FunctionalInterface
public interface CConsumer<T> {

    void accept(T t) throws Throwable;

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

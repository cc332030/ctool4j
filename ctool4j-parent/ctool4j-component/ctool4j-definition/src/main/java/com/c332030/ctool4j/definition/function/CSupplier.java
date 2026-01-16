package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CSupplier
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CSupplier<T> extends Supplier<T> {

    @Override
    @SneakyThrows
    default T get() {
        return getThrowable();
    }

    T getThrowable() throws Throwable;

    CSupplier<Object> NULL = () -> null;

    @SuppressWarnings("unchecked")
    static <T> CSupplier<T> alwaysNull() {
        return (CSupplier<T>)NULL;
    }

    static <T> T get(CSupplier<T> supplier) {
        if(supplier == null) {
            return null;
        }
        return supplier.get();
    }

    static <T> Supplier<T> convert(CSupplier<T> supplier) {
        return () -> get(supplier);
    }

}

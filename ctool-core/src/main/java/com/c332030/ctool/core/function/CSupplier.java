package com.c332030.ctool.core.function;

import lombok.Lombok;

import java.util.function.Supplier;

/**
 * <p>
 * Description: CSupplier
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CSupplier<T> {

    T get() throws Exception;

    static <T> T get(CSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    static <T> Supplier<T> convert(CSupplier<T> supplier) {
        return () -> get(supplier);
    }

}

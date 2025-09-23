package com.c332030.ctool4j.core.function;

import lombok.Lombok;

import java.util.function.Function;

/**
 * <p>
 * Description: CFunction
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CFunction<O, R> {

    R apply(O o) throws Exception;

    static <O, R> R apply(CFunction<O, R> function, O o) {
        try {
            return function.apply(o);
        } catch (Throwable e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    static <T, U> Function<T, U> convert(CFunction<T, U> function) {
        return t -> apply(function, t);
    }

}

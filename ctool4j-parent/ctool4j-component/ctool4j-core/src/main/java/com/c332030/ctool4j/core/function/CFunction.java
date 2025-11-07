package com.c332030.ctool4j.core.function;

import lombok.SneakyThrows;

import java.util.function.Function;

/**
 * <p>
 * Description: CFunction
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CFunction<O, R> extends Function<O, R> {

    @Override
    @SneakyThrows
    default R apply(O o) {
        return applyThrowable(o);
    }

    R applyThrowable(O o) throws Throwable;

    CFunction<Object, Object> SELF = o -> o;

    @SuppressWarnings("unchecked")
    static <O, R> CFunction<O, R> self() {
        return (CFunction<O, R>)SELF;
    }

    static <O, R> R apply(CFunction<O, R> function, O o) {
        return function.apply(o);
    }

    static <T, U> Function<T, U> convert(CFunction<T, U> function) {
        return t -> apply(function, t);
    }


}

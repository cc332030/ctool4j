package com.c332030.ctool4j.definition.function;

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

    String APPLY = "apply";

    @Override
    @SneakyThrows
    default R apply(O o) {
        return applyThrowable(o);
    }

    R applyThrowable(O o) throws Throwable;

    CFunction<Object, Object> SELF = o -> o;

    @SuppressWarnings("unchecked")
    static <O> CFunction<O, O> self() {
        return (CFunction<O, O>)SELF;
    }

    CFunction<Object, Object> EMPTY = o -> null;

    @SuppressWarnings("unchecked")
    static <O, R> CFunction<O, R> empty() {
        return (CFunction<O, R>)EMPTY;
    }

    static <O, R> R apply(Function<O, R> function, O o) {
        if(null == function) {
            return null;
        }
        return function.apply(o);
    }

    static <O, R> Function<O, R> convert(CFunction<O, R> function) {
        return t -> apply(function, t);
    }

}

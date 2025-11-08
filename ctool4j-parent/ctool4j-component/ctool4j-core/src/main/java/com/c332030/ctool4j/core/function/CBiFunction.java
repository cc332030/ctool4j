package com.c332030.ctool4j.core.function;

import lombok.SneakyThrows;

import java.util.function.BiFunction;

/**
 * <p>
 * Description: CBiFunction
 * </p>
 *
 * @since 2025/5/12
 */
@FunctionalInterface
public interface CBiFunction<O1, O2, R> extends BiFunction<O1, O2, R> {

    @Override
    @SneakyThrows
    default R apply(O1 o1, O2 o2) {
        return applyThrowable(o1, o2);
    }

    R applyThrowable(O1 o1, O2 o2) throws Throwable;

    static <O1, O2, R> R apply(CBiFunction<O1, O2, R> function, O1 o1, O2 o2) {
        return function.apply(o1, o2);
    }

    static <O1, O2, U> BiFunction<O1, O2, U> convert(CBiFunction<O1, O2, U> function) {
        return (o1, o2) -> apply(function, o1, o2);
    }

    CBiFunction<Object, Object, Object> FIRST = (o1, o2) -> o1;

    @SuppressWarnings("unchecked")
    static <O1, O2> BiFunction<O1, O2, O1> first() {
        return (BiFunction<O1, O2, O1>)FIRST;
    }

    CBiFunction<Object, Object, Object> SECOND = (o1, o2) -> o2;

    @SuppressWarnings("unchecked")
    static <O1, O2> BiFunction<O1, O2, O2> second() {
        return (BiFunction<O1, O2, O2>)SECOND;
    }

}

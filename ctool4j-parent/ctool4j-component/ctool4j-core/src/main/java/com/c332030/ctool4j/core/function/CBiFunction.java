package com.c332030.ctool4j.core.function;

import lombok.Lombok;

import java.util.function.BiFunction;

/**
 * <p>
 * Description: CBiFunction
 * </p>
 *
 * @since 2025/5/12
 */
@FunctionalInterface
public interface CBiFunction<O1, O2, R> {

    R apply(O1 o1, O2 o2) throws Throwable;

    static <O1, O2, R> R apply(CBiFunction<O1, O2, R> function, O1 o1, O2 o2) {
        try {
            return function.apply(o1, o2);
        } catch (Throwable e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    static <O1, O2, R> BiFunction<O1, O2, R> convert(CBiFunction<O1, O2, R> function) {
        return (o1, o2) -> apply(function, o1, o2);
    }

    static <O1, O2> O1 first(O1 o1, O2 o2) {
        return o1;
    }

    static <O1, O2> BiFunction<O1, O2, O1> first() {
        return CBiFunction::first;
    }

    static <O1, O2> O2 second(O1 o1, O2 o2) {
        return o2;
    }

    static <O1, O2> BiFunction<O1, O2, O2> second() {
        return CBiFunction::second;
    }

}

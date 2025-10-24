package com.c332030.ctool4j.core.function;

import lombok.Lombok;

/**
 * <p>
 * Description: CTriFunction
 * </p>
 *
 * @since 2025/10/24
 */
@FunctionalInterface
public interface CTriFunction<O1, O2, O3, R> {

    R apply(O1 o1, O2 o2, O3 o3) throws Throwable;

    static <O1, O2, O3, R> R apply(CTriFunction<O1, O2, O3, R> function, O1 o1, O2 o2, O3 o3) {
        try {
            return function.apply(o1, o2, o3);
        } catch (Throwable e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    static <O1, O2, O3, R> CTriFunction<O1, O2, O3, R> convert(CTriFunction<O1, O2, O3, R> function) {
        return (o1, o2, o3) -> apply(function, o1, o2, o3);
    }

    static <O1, O2, O3> O1 first(O1 o1, O2 o2, O3 o3) {
        return o1;
    }
    static <O1, O2, O3> CTriFunction<O1, O2, O3, O1> first() {
        return CTriFunction::first;
    }

    static <O1, O2, O3> O2 second(O1 o1, O2 o2, O3 o3) {
        return o2;
    }
    static <O1, O2, O3> CTriFunction<O1, O2, O3, O2> second() {
        return CTriFunction::second;
    }

    static <O1, O2, O3> O3 third(O1 o1, O2 o2, O3 o3) {
        return o3;
    }
    static <O1, O2, O3> CTriFunction<O1, O2, O3, O3> third() {
        return CTriFunction::third;
    }

}

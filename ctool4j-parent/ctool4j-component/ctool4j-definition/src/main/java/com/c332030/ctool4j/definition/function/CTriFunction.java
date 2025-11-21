package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

/**
 * <p>
 * Description: CTriFunction
 * </p>
 *
 * @since 2025/10/24
 */
@FunctionalInterface
public interface CTriFunction<O1, O2, O3, R> {

    @SneakyThrows
    default R apply(O1 o1, O2 o2, O3 o3) {
        return applyThrowable(o1, o2, o3);
    }

    R applyThrowable(O1 o1, O2 o2, O3 o3) throws Throwable;

    static <O1, O2, O3, R> R apply(CTriFunction<O1, O2, O3, R> function, O1 o1, O2 o2, O3 o3) {
        return function.apply(o1, o2, o3);
    }

    static <O1, O2, O3, R> CTriFunction<O1, O2, O3, R> convert(CTriFunction<O1, O2, O3, R> function) {
        return (o1, o2, o3) -> apply(function, o1, o2, o3);
    }

    CTriFunction<Object, Object, Object, Object> FIRST = (o1, o2, o3) -> o1;

    @SuppressWarnings("unchecked")
    static <O1, O2, O3> CTriFunction<O1, O2, O3, O1> first() {
        return (CTriFunction<O1, O2, O3, O1>)FIRST;
    }

    CTriFunction<Object, Object, Object, Object> SECOND = (o1, o2, o3) -> o2;

    @SuppressWarnings("unchecked")
    static <O1, O2, O3> CTriFunction<O1, O2, O3, O2> second() {
        return (CTriFunction<O1, O2, O3, O2>)SECOND;
    }

    CTriFunction<Object, Object, Object, Object> THIRD = (o1, o2, o3) -> o3;

    @SuppressWarnings("unchecked")
    static <O1, O2, O3> CTriFunction<O1, O2, O3, O3> third() {
        return (CTriFunction<O1, O2, O3, O3>)THIRD;
    }

}

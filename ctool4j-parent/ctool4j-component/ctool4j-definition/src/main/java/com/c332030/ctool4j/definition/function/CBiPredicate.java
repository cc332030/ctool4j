package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.BiPredicate;

/**
 * <p>
 * Description: CBiPredicate
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CBiPredicate<T, U> extends BiPredicate<T, U> {

    @Override
    @SneakyThrows
    default boolean test(T t, U u) {
        return testThrowable(t, u);
    }

    boolean testThrowable(T t, U u) throws Throwable;

    CBiPredicate<Object, Object> TRUE = (t, u) -> true;

    @SuppressWarnings("unchecked")
    static <T, U> CBiPredicate<T, U> alwaysTrue() {
        return (CBiPredicate<T, U>)TRUE;
    }

    CBiPredicate<Object, Object> FALSE = (t, u) -> false;

    @SuppressWarnings("unchecked")
    static <T, U> CBiPredicate<T, U> alwaysFalse() {
        return (CBiPredicate<T, U>)FALSE;
    }

    static <T, U> boolean test(CBiPredicate<T, U> predicate, T t, U u) {
        return predicate.test(t, u);
    }

    static <T, U> BiPredicate<T, U> convert(CBiPredicate<T, U> predicate) {
        return (t, u) -> test(predicate, t, u);
    }


}

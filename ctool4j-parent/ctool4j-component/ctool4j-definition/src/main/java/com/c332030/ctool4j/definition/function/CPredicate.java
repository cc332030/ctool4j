package com.c332030.ctool4j.definition.function;

import lombok.SneakyThrows;

import java.util.function.Predicate;

/**
 * <p>
 * Description: CPredicate
 * </p>
 *
 * @since 2025/1/15
 */
@FunctionalInterface
public interface CPredicate<T> extends Predicate<T> {

    @Override
    @SneakyThrows
    default boolean test(T t) {
        return testThrowable(t);
    }

    boolean testThrowable(T t) throws Throwable;

    CPredicate<Object> TRUE = t -> true;

    @SuppressWarnings("unchecked")
    static <T> CPredicate<T> alwaysTrue() {
        return (CPredicate<T>)TRUE;
    }

    CPredicate<Object> FALSE = t -> false;

    @SuppressWarnings("unchecked")
    static <T> CPredicate<T> alwaysFalse() {
        return (CPredicate<T>)FALSE;
    }

    static <T> boolean test(Predicate<T> predicate, T t) {
        if(null == predicate) {
            return false;
        }
        return predicate.test(t);
    }

    static <T> Predicate<T> convert(CPredicate<T> predicate) {
        return t -> test(predicate, t);
    }


}

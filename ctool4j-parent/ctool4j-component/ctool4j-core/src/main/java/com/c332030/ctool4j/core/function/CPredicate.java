package com.c332030.ctool4j.core.function;

import lombok.Lombok;
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

    static <T> boolean test(CPredicate<T> predicate, T t) {
        try {
            return predicate.test(t);
        } catch (Throwable e) {
            throw Lombok.sneakyThrow(e);
        }
    }

    static <T> Predicate<T> convert(CPredicate<T> predicate) {
        return t -> test(predicate, t);
    }

}

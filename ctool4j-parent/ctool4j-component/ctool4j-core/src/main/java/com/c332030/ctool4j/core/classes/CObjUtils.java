package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * <p>
 * Description: CObjUtils
 * </p>
 *
 * @since 2024/3/19
 */
@UtilityClass
public class CObjUtils {

    public static final Object OBJECT = new Object();

    @SuppressWarnings("unchecked")
    public <T> T emptyObject() {
        return (T) OBJECT;
    }

    @SuppressWarnings("unchecked")
    public <T> T anyType(Object object) {
        return (T) object;
    }

    public <T> Supplier<T> toSupplier(Runnable runnable) {
        return () -> {
            runnable.run();
            return emptyObject();
        };
    }

    public <T> T defaultIfNull(final T object, Supplier<T> defaultValueSupplier) {
        return object != null ? object : defaultValueSupplier.get();
    }

    public <T> T ifThenGet(boolean bool, Supplier<T> supplier) {
        if(bool) {
            return supplier.get();
        }
        return null;
    }

    public <T> T equalsThenGet(Object v1, Object v2, Supplier<T> supplier) {
        return ifThenGet(Objects.equals(v1, v2), supplier);
    }

    public <T> T notNullThenGet(Object value, Supplier<T> supplier) {
        return ifThenGet(Objects.nonNull(value), supplier);
    }
    public <K, T> T notNullThenGet(K value, Function<K, T> function) {
        return ifThenGet(Objects.nonNull(value), () -> function.apply(value));
    }

    @SuppressWarnings("unchecked")
    public <T> T to(Object o, Class<T> tClass) {

        if(null == o) {
            return null;
        }

        if(tClass.isInstance(o)) {
            return (T) o;
        }

        throw new IllegalStateException("Convert failed, value: " + o + ", targetClass: " + tClass);
    }

    @SuppressWarnings("unchecked")
    public <From, To> To convert(From from, Class<To> toClass) {

        if(null == from) {
            return null;
        }

        if(toClass.isInstance(from)) {
            return (To) from;
        }

        val converter = (CFunction<From, To>) CClassUtils.getConverter(from.getClass(), toClass);
        if (null == converter) {
            return null;
        }
        return converter.apply(from);
    }

    public <O, R> R convert(O o, Function<O, R> function) {
        return convert(o, function, null);
    }

    public <O, R> R convert(O o, Function<O, R> function, R defaultValue) {

        if(Objects.isNull(o)) {
            return defaultValue;
        }

        val value = function.apply(o);
        if(Objects.nonNull(value)) {
            return value;
        }

        return defaultValue;
    }

    public <O1, O2, R> R convert(O1 o1, Function<O1, R> function1, O2 o2, Function<O2, R> function2) {

        if(Objects.nonNull(o1)) {

            val value = function1.apply(o1);
            if(Objects.nonNull(value)) {
                return value;
            }
        }

        if(Objects.nonNull(o2)) {

            val value = function2.apply(o2);
            if(Objects.nonNull(value)) {
                return value;
            }
        }

        return null;
    }

    public <O1, O2> boolean equals(O1 o1, O2 o2, Function<O2, O1> function) {
        return Objects.equals(o1, convert(o2, function));
    }

    public <T> T merge(T v1, T v2, BiFunction<T, T, T> merge) {
        return merge(v1, v2, Objects::nonNull, merge);
    }

    public <T> T merge(T v1, T v2, Predicate<T> availablePredicate, BiFunction<T, T, T> merge) {

        if(!availablePredicate.test(v1)) {
            return v2;
        }

        if(!availablePredicate.test(v2)) {
            return v1;
        }

        if(null == merge) {
            throw new IllegalStateException("Conflict key, v1: " + v1 + ", v2: " + v2);
        }

        return merge.apply(v1, v2);
    }

}

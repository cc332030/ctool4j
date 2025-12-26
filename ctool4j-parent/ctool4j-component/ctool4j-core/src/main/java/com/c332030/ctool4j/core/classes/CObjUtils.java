package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.definition.function.*;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.Objects;

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

    public <T> T emptyObject() {
        return anyType(OBJECT);
    }

    @SuppressWarnings("unchecked")
    public <T> T anyType(Object object) {
        return (T) object;
    }

    public <T> T anyType(CSupplier<T> supplier) {
        return anyType(supplier.get());
    }

    public <T> CSupplier<T> toSupplier(CRunnable runnable) {
        return () -> {
            runnable.run();
            return emptyObject();
        };
    }

    public <T> T defaultIfNull(final T object, CSupplier<T> defaultValueSupplier) {
        return object != null ? object : defaultValueSupplier.get();
    }

    public <T> T ifThenGet(boolean bool, CSupplier<T> supplier) {
        if(bool) {
            return supplier.get();
        }
        return null;
    }

    public <T> T equalsThenGet(Object v1, Object v2, CSupplier<T> supplier) {
        return ifThenGet(Objects.equals(v1, v2), supplier);
    }

    public <T> T notNullThenGet(Object value, CSupplier<T> supplier) {
        return ifThenGet(Objects.nonNull(value), supplier);
    }
    public <K, T> T notNullThenGet(K value, CFunction<K, T> function) {
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

        val converter = (CFunction<From, To>) CConvertUtils.getConverter(from.getClass(), toClass);
        if (null == converter) {
            return null;
        }
        return converter.apply(from);
    }

    public <O, R> R convert(O o, CFunction<O, R> function) {
        return convert(o, function, null);
    }

    public <O, R> R convert(O o, CFunction<O, R> function, R defaultValue) {

        if(Objects.isNull(o)) {
            return defaultValue;
        }

        val value = function.apply(o);
        if(Objects.nonNull(value)) {
            return value;
        }

        return defaultValue;
    }

    public <O1, O2, R> R convert(O1 o1, CFunction<O1, R> function1, O2 o2, CFunction<O2, R> function2) {

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

    public <O1, O2> boolean equals(O1 o1, O2 o2, CFunction<O2, O1> function) {
        return Objects.equals(o1, convert(o2, function));
    }

    public <T> T merge(T v1, T v2, CBiFunction<T, T, T> merge) {
        return merge(v1, v2, Objects::nonNull, merge);
    }

    public <T> T merge(T v1, T v2, CPredicate<T> availablePredicate, CBiFunction<T, T, T> merge) {

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

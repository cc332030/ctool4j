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

    /**
     * 空对象占位
     */
    public static final Object OBJECT = new Object();

    /**
     * 返回空对象
     *
     * @param <T> 目标类型
     * @return 空对象
     */
    public <T> T emptyObject() {
        return anyType(OBJECT);
    }

    /**
     * 对象强转为任意类型
     *
     * @param object 对象
     * @param <T>    目标类型
     * @return 强转后的对象
     */
    @SuppressWarnings("unchecked")
    public <T> T anyType(Object object) {
        return (T) object;
    }

    /**
     * 获取供应商结果并强转为任意类型
     *
     * @param supplier 供应商
     * @param <T>      目标类型
     * @return 供应商结果
     */
    public <T> T anyType(CSupplier<T> supplier) {
        return anyType(supplier.get());
    }

    /**
     * Runnable 转 Supplier
     *
     * @param runnable Runnable
     * @param <T>      目标类型
     * @return Supplier
     */
    public <T> CSupplier<T> toSupplier(CRunnable runnable) {
        return () -> {
            runnable.run();
            return emptyObject();
        };
    }

    /**
     * 对象为 null 时返回默认值
     *
     * @param object               对象
     * @param defaultValueSupplier 默认值供应商
     * @param <T>                  类型
     * @return 对象或默认值
     */
    public <T> T defaultIfNull(final T object, CSupplier<T> defaultValueSupplier) {
        return object != null ? object : defaultValueSupplier.get();
    }

    /**
     * 条件成立时获取结果
     *
     * @param bool     条件
     * @param supplier 结果供应商
     * @param <T>      类型
     * @return 条件成立时的结果，否则为 null
     */
    public <T> T ifThenGet(boolean bool, CSupplier<T> supplier) {
        if(bool) {
            return supplier.get();
        }
        return null;
    }

    /**
     * 两个对象相等时获取结果
     *
     * @param v1       第一个对象
     * @param v2       第二个对象
     * @param supplier 结果供应商
     * @param <T>      类型
     * @return 相等时的结果，否则为 null
     */
    public <T> T equalsThenGet(Object v1, Object v2, CSupplier<T> supplier) {
        return ifThenGet(Objects.equals(v1, v2), supplier);
    }

    /**
     * 对象非 null 时获取结果
     *
     * @param value    对象
     * @param supplier 结果供应商
     * @param <T>      类型
     * @return 对象非 null 时的结果，否则为 null
     */
    public <T> T notNullThenGet(Object value, CSupplier<T> supplier) {
        return ifThenGet(Objects.nonNull(value), supplier);
    }

    /**
     * 对象非 null 时应用函数获取结果
     *
     * @param value    对象
     * @param function 函数
     * @param <K>      入参类型
     * @param <T>      返回类型
     * @return 对象非 null 时的函数结果，否则为 null
     */
    public <K, T> T notNullThenGet(K value, CFunction<K, T> function) {
        return ifThenGet(Objects.nonNull(value), () -> function.apply(value));
    }

    /**
     * 对象转目标类型（不支持转换时抛异常）
     *
     * @param o      对象
     * @param tClass 目标类型
     * @param <T>    目标类型
     * @return 转换结果，对象为 null 时返回 null
     * @throws IllegalStateException 对象不是目标类型实例时抛出
     */
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

    /**
     * 对象转目标类型（使用已注册转换器）
     *
     * @param from    源对象
     * @param toClass 目标类型
     * @param <From>  源类型
     * @param <To>    目标类型
     * @return 转换结果，无可用转换器时返回 null
     */
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

    /**
     * 对象应用函数转换
     *
     * @param o        对象
     * @param function 转换函数
     * @param <O>      源类型
     * @param <R>      返回类型
     * @return 转换结果
     */
    public <O, R> R convert(O o, CFunction<O, R> function) {
        return convert(o, function, null);
    }

    /**
     * 对象应用函数转换，为 null 时返回默认值
     *
     * @param o           对象
     * @param function    转换函数
     * @param defaultValue 默认值
     * @param <O>         源类型
     * @param <R>         返回类型
     * @return 转换结果或默认值
     */
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

    /**
     * 依次尝试两个对象应用函数转换，返回第一个非 null 结果
     *
     * @param o1        第一个对象
     * @param function1 第一个转换函数
     * @param o2        第二个对象
     * @param function2 第二个转换函数
     * @param <O1>      第一个源类型
     * @param <O2>      第二个源类型
     * @param <R>       返回类型
     * @return 第一个非 null 的转换结果，均为 null 时返回 null
     */
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

    /**
     * 判断两个对象是否相等（通过转换函数比较）
     *
     * @param o1       第一个对象
     * @param o2       第二个对象
     * @param function 转换函数
     * @param <O1>     第一个类型
     * @param <O2>     第二个类型
     * @return 是否相等
     */
    public <O1, O2> boolean equals(O1 o1, O2 o2, CFunction<O2, O1> function) {
        return Objects.equals(o1, convert(o2, function));
    }

    /**
     * 合并两个值
     *
     * @param v1    第一个值
     * @param v2    第二个值
     * @param merge 合并函数
     * @param <T>   类型
     * @return 合并结果
     */
    public <T> T merge(T v1, T v2, CBiFunction<T, T, T> merge) {
        return merge(null, v1, v2, Objects::nonNull, merge);
    }

    /**
     * 合并两个值（带冲突 key）
     *
     * @param key   冲突 key
     * @param v1    第一个值
     * @param v2    第二个值
     * @param merge 合并函数
     * @param <K>   key 类型
     * @param <T>   值类型
     * @return 合并结果
     */
    public <K, T> T merge(K key, T v1, T v2, CBiFunction<T, T, T> merge) {
        return merge(key, v1, v2, Objects::nonNull, merge);
    }

    /**
     * 合并两个值（带可用性判断）
     *
     * @param v1                第一个值
     * @param v2                第二个值
     * @param availablePredicate 可用性判断
     * @param merge             合并函数
     * @param <T>               类型
     * @return 合并结果
     */
    public <T> T merge(T v1, T v2, CPredicate<T> availablePredicate, CBiFunction<T, T, T> merge) {
        return merge(null, v1, v2, availablePredicate, merge);
    }

    /**
     * 合并两个值（带冲突 key 与可用性判断）
     *
     * @param key               冲突 key
     * @param v1                第一个值
     * @param v2                第二个值
     * @param availablePredicate 可用性判断
     * @param merge             合并函数
     * @param <K>               key 类型
     * @param <T>               值类型
     * @return 合并结果
     * @throws IllegalStateException 两个值均可用且无合并函数时抛出
     */
    public <K, T> T merge(K key, T v1, T v2, CPredicate<T> availablePredicate, CBiFunction<T, T, T> merge) {

        if(!availablePredicate.test(v1)) {
            return v2;
        }

        if(!availablePredicate.test(v2)) {
            return v1;
        }

        if(null == merge) {
            throw new IllegalStateException("Conflict key: " + key + ", v1: " + v1 + ", v2: " + v2);
        }

        return merge.apply(v1, v2);
    }

}

package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * <p>
 * Description: CComparatorUtils
 * </p>
 *
 * @since 2025/3/26
 */
@UtilityClass
public class CComparatorUtils {

    /**
     * 按比较器取集合中最值（过滤 null 元素）
     *
     * @param collection 集合
     * @param comparator 比较器
     * @param <T>        元素类型
     * @return 最值，集合为空时返回 null
     */
    public <T> T compareCollection(Collection<T> collection, Comparator<? super T> comparator) {
        return collection.stream()
                .filter(Objects::nonNull)
                .min(comparator)
                .orElse(null);
    }

    /**
     * 取参数中的最小值
     *
     * @param os  待比较元素
     * @param <T> 元素类型
     * @return 最小值，参数为空时返回 null
     */
    @SafeVarargs
    public <T extends Comparable<T>> T min(T... os) {
        return min(Arrays.asList(os));
    }

    /**
     * 取集合中的最小值
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 最小值，集合为空时返回 null
     */
    public <T extends Comparable<T>> T min(Collection<T> collection) {
        return compareCollection(collection, Comparable::compareTo);
    }

    /**
     * 取参数中的最大值
     *
     * @param os  待比较元素
     * @param <T> 元素类型
     * @return 最大值，参数为空时返回 null
     */
    @SafeVarargs
    public <T extends Comparable<T>> T max(T... os) {
        return max(Arrays.asList(os));
    }

    /**
     * 取集合中的最大值
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 最大值，集合为空时返回 null
     */
    public <T extends Comparable<T>> T max(Collection<T> collection) {
        return compareCollection(collection, Comparator.reverseOrder());
    }

    /**
     * 通过字段取值后按比较器取集合中最值
     *
     * @param collection 集合
     * @param function   取值函数
     * @param comparator 比较器
     * @param <T>        元素类型
     * @param <V>        字段值类型
     * @return 最值，集合为空时返回 null
     */
    public <T, V extends Comparable<V>> T compareCollection(
            Collection<T> collection,
            Function<? super T, V> function,
            Comparator<V> comparator
    ) {
        return collection.stream()
                .filter(e -> Objects.nonNull(e) && Objects.nonNull(function.apply(e)))
                .min((e1, e2) -> {
                    val v1 = function.apply(e1);
                    val v2 = function.apply(e2);
                    return comparator.compare(v1, v2);
                })
                .orElse(null);
    }

    /**
     * 通过字段取值后取集合中的最小值
     *
     * @param collection 集合
     * @param function   取值函数
     * @param <T>        元素类型
     * @param <V>        字段值类型
     * @return 最小值，集合为空时返回 null
     */
    public <T, V extends Comparable<V>> T min(Collection<T> collection, Function<T, V> function) {
        return compareCollection(collection, function, Comparable::compareTo);
    }

    /**
     * 通过字段取值后取集合中的最大值
     *
     * @param collection 集合
     * @param function   取值函数
     * @param <T>        元素类型
     * @param <V>        字段值类型
     * @return 最大值，集合为空时返回 null
     */
    public <T, V extends Comparable<V>> T max(Collection<T> collection, Function<T, V> function) {
        return compareCollection(collection, function, Comparator.reverseOrder());
    }

    /**
     * 通过字段取值后取最小值并消费
     *
     * @param collection 集合
     * @param function   取值函数
     * @param consumer   消费函数
     * @param <T>        元素类型
     * @param <V>        字段值类型
     */
    public <T, V extends Comparable<V>> void minConsumer(
            Collection<T> collection,
            Function<T, V> function,
            Consumer<T> consumer
    ) {
        Optional.ofNullable(compareCollection(collection, function, Comparable::compareTo))
                .ifPresent(consumer);
    }

    /**
     * 通过字段取值后取最大值并消费
     *
     * @param collection 集合
     * @param function   取值函数
     * @param consumer   消费函数
     * @param <T>        元素类型
     * @param <V>        字段值类型
     */
    public <T, V extends Comparable<V>> void maxConsumer(
            Collection<T> collection,
            Function<T, V> function,
            Consumer<T> consumer
    ) {
        Optional.ofNullable(compareCollection(collection, function, Comparator.reverseOrder()))
                .ifPresent(consumer);
    }

    /**
     * 比较两个值（null 视为最大）
     *
     * @param v1        第一个值
     * @param v2        第二个值
     * @param comparator 比较器
     * @param <V>       值类型
     * @return 比较结果，v1 大于 v2 返回正数，相等返回 0，否则返回负数
     */
    public <V> int compare(V v1, V v2, Comparator<? super V> comparator) {

        if(null == v1) {
            if (null == v2) {
                return 0;
            }
            return 1;
        }

        if(null == v2) {
            return -1;
        }

        return comparator.compare(v1, v2);
    }

    /**
     * 比较两个值（null 视为最大）
     *
     * @param v1  第一个值
     * @param v2  第二个值
     * @param <V> 值类型
     * @return 比较结果，v1 大于 v2 返回正数，相等返回 0，否则返回负数
     */
    public <V extends Comparable<V>> int compare(V v1, V v2) {
        return compare(v1, v2, Comparable::compareTo);
    }

}

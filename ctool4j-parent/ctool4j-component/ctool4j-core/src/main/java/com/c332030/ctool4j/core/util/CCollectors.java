package com.c332030.ctool4j.core.util;

import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * <p>
 * Description: CCollectors
 * </p>
 *
 * @since 2024/4/18
 * @see "doc/design/core/CCollectors.adoc"
 * @see "doc/design/core/CCollectorsTests.adoc"
 */
@UtilityClass
public class CCollectors {

    /**
     * 收集为不可变 List
     *
     * @param <T> 元素类型
     * @return 收集器
     */
    public <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return Collectors.collectingAndThen(
            Collectors.toList(),
            Collections::unmodifiableList
        );
    }

    /**
     * 收集为不可变 LinkedHashMap（元素自身为值）
     *
     * @param keyMapper 键提取函数
     * @param <T>       元素类型
     * @param <K>       键类型
     * @return 收集器
     */
    public <T, K> Collector<T, ?, Map<K, T>> toUnmodifiableLinkedMap(
        Function<? super T, ? extends K> keyMapper
    ) {
        return toUnmodifiableLinkedMap(keyMapper, Function.identity());
    }

    /**
     * 收集为不可变 LinkedHashMap（元素自身为值，带键冲突合并）
     *
     * @param keyMapper     键提取函数
     * @param mergeFunction 键冲突合并函数
     * @param <T>           元素类型
     * @param <K>           键类型
     * @return 收集器
     */
    public <T, K> Collector<T, ?, Map<K, T>> toUnmodifiableLinkedMap(
        Function<? super T, ? extends K> keyMapper, BinaryOperator<T> mergeFunction
    ) {
        return toUnmodifiableLinkedMap(keyMapper, Function.identity(), mergeFunction);
    }

    /**
     * 收集为不可变 LinkedHashMap（键值分别提取）
     *
     * @param keyMapper   键提取函数
     * @param valueMapper 值提取函数
     * @param <T>         元素类型
     * @param <K>         键类型
     * @param <U>         值类型
     * @return 收集器
     */
    public <T, K, U> Collector<T, ?, Map<K, U>> toUnmodifiableLinkedMap(
        Function<? super T, ? extends K> keyMapper,
        Function<? super T, ? extends U> valueMapper
    ) {
        return toUnmodifiableLinkedMap(keyMapper, valueMapper,
            (v1, v2) -> {
                throw new IllegalStateException("Conflict key, v1: " + v1 + ", v2: " + v2);
            }
        );
    }

    /**
     * 收集为不可变 LinkedHashMap（键值分别提取，带键冲突合并）
     *
     * @param keyMapper     键提取函数
     * @param valueMapper   值提取函数
     * @param mergeFunction 键冲突合并函数
     * @param <T>           元素类型
     * @param <K>           键类型
     * @param <U>           值类型
     * @return 收集器
     */
    public <T, K, U> Collector<T, ?, Map<K, U>> toUnmodifiableLinkedMap(
        Function<? super T, ? extends K> keyMapper,
        Function<? super T, ? extends U> valueMapper,
        BinaryOperator<U> mergeFunction
    ) {
        return Collectors.collectingAndThen(
            Collectors.toMap(
                keyMapper,
                valueMapper,
                mergeFunction,
                LinkedHashMap::new
            ),
            Collections::unmodifiableMap
        );
    }

    /**
     * 收集为 LinkedHashSet
     *
     * @param <T> 元素类型
     * @return 收集器
     */
    public <T> Collector<T, ?, Set<T>> toLinkedSet() {
        return Collectors.toCollection(LinkedHashSet::new);
    }

    /**
     * 收集为不可变 Set
     *
     * @param <T> 元素类型
     * @return 收集器
     */
    public <T> Collector<T, ?, Set<T>> toUnmodifiableSet() {
        return Collectors.collectingAndThen(
            Collectors.toSet(),
            Collections::unmodifiableSet
        );
    }

}

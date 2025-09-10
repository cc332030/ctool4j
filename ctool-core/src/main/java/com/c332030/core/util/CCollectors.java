package com.c332030.core.util;

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
 */
@UtilityClass
public class CCollectors {

    public static <T, K> Collector<T, ?, Map<K, T>> toUnmodifiableLinkedMap(
            Function<? super T, ? extends K> keyMapper
    ) {
        return toUnmodifiableLinkedMap(keyMapper, Function.identity());
    }

    public static <T, K> Collector<T, ?, Map<K, T>> toUnmodifiableLinkedMap(
            Function<? super T, ? extends K> keyMapper, BinaryOperator<T> mergeFunction
    ) {
        return toUnmodifiableLinkedMap(keyMapper, Function.identity(), mergeFunction);
    }

    public static <T, K, U> Collector<T, ?, Map<K,U>> toUnmodifiableLinkedMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper
    ) {
        return toUnmodifiableLinkedMap(keyMapper, valueMapper,
                (v1, v2) -> {throw new IllegalStateException("Conflict key, v1: " + v1 + ", v2: " + v2);}
        );
    }

    public static <T, K, U> Collector<T, ?, Map<K,U>> toUnmodifiableLinkedMap(
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

    public static <T> Collector<T, ?, Set<T>> toLinkedSet() {
        return Collectors.toCollection(LinkedHashSet::new);
    }

}

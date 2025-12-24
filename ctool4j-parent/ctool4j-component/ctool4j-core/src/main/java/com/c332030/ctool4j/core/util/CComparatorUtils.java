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

    public <T> T compareCollection(Collection<T> collection, Comparator<? super T> comparator) {
        return collection.stream()
                .filter(Objects::nonNull)
                .min(comparator)
                .orElse(null);
    }

    @SafeVarargs
    public <T extends Comparable<T>> T min(T... os) {
        return min(Arrays.asList(os));
    }

    public <T extends Comparable<T>> T min(Collection<T> collection) {
        return compareCollection(collection, Comparable::compareTo);
    }

    @SafeVarargs
    public <T extends Comparable<T>> T max(T... os) {
        return max(Arrays.asList(os));
    }

    public <T extends Comparable<T>> T max(Collection<T> collection) {
        return compareCollection(collection, Comparator.reverseOrder());
    }

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

    public <T, V extends Comparable<V>> T min(Collection<T> collection, Function<T, V> function) {
        return compareCollection(collection, function, Comparable::compareTo);
    }

    public <T, V extends Comparable<V>> T max(Collection<T> collection, Function<T, V> function) {
        return compareCollection(collection, function, Comparator.reverseOrder());
    }

    public <T, V extends Comparable<V>> void minConsumer(
            Collection<T> collection,
            Function<T, V> function,
            Consumer<T> consumer
    ) {
        Optional.ofNullable(compareCollection(collection, function, Comparable::compareTo))
                .ifPresent(consumer);
    }

    public <T, V extends Comparable<V>> void maxConsumer(
            Collection<T> collection,
            Function<T, V> function,
            Consumer<T> consumer
    ) {
        Optional.ofNullable(compareCollection(collection, function, Comparator.reverseOrder()))
                .ifPresent(consumer);
    }

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

    public <V extends Comparable<V>> int compare(V v1, V v2) {
        return compare(v1, v2, Comparable::compareTo);
    }

}

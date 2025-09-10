package com.c332030.core.util;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Description: CCollUtils
 * </p>
 *
 * @since 2024/11/21
 */
@UtilityClass
public class CCollUtils {

    public <T> Collection<T> defaultEmpty(Collection<T> collection) {
        return CollUtil.isEmpty(collection) ? CList.of() : collection;
    }

    public <T> List<T> defaultEmpty(List<T> list) {
        return CollUtil.isEmpty(list) ? CList.of() : list;
    }

    public <T> Set<T> defaultEmpty(Set<T> list) {
        return CollUtil.isEmpty(list) ? CSet.of() : list;
    }

    public <K, V> Map<K, List<V>> groupingBy(Collection<V> collection, Function<V, K> function) {
        return groupingBy(collection, function, Objects::nonNull);
    }

    public <K, V> Map<K, List<V>> groupingBy(Collection<V> collection, Function<V, K> function, Predicate<K> predicate) {

        val map = new LinkedHashMap<K, List<V>>();

        collection.forEach(item -> {
            val key = function.apply(item);
            if(!predicate.test(key)) {
                return;
            }
            map.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(item);
        });

        return Collections.unmodifiableMap(map);
    }

    public <P, C extends P> void addAllIgnoreNull(Collection<P> collection1, Collection<C> collection2) {
        if(null != collection2) {
            collection1.addAll(collection2);
        }
    }

    @SafeVarargs
    public <P> List<P> concat(Collection<? extends P>... collections) {

        val collectionsNew = Arrays.stream(collections)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        val size = collectionsNew.stream()
                .mapToInt(Collection::size)
                .sum();

        val list = new ArrayList<P>(size);
        collectionsNew.forEach(list::addAll);
        return list;
    }

    public <O, R> Collection<O> filter(Collection<O> collection, Function<O, R> convert, Predicate<R> predicate) {
        if(CollUtil.isEmpty(collection)) {
            return CList.of();
        }
        return collection.stream()
                .filter(e -> predicate.test(CObjUtils.convert(e, convert)))
                .collect(Collectors.toList());
    }
    public <T> Collection<T> filter(Collection<T> collection, Predicate<T> predicate) {
        return filter(collection, Function.identity(), predicate);
    }
    public <T> Collection<T> filterNull(Collection<T> collection) {
        return filter(collection, Objects::nonNull);
    }
    public Collection<String> filterString(Collection<String> collection) {
        return filter(collection, StringUtils::isNotBlank);
    }
    public <T, K> Collection<T> filterKey(Collection<T> collection, Function<T, K> convert) {
        return filter(collection, convert, Objects::nonNull);
    }
    public <T> Collection<T> filterStringKey(Collection<T> collection, Function<T, String> convert) {
        return filter(collection, convert, StringUtils::isNotBlank);
    }

    public <O, R> List<O> filter(List<O> collection, Function<O, R> convert, Predicate<R> predicate) {
        if(CollUtil.isEmpty(collection)) {
            return CList.of();
        }
        return collection.stream()
                .filter(e -> predicate.test(CObjUtils.convert(e, convert)))
                .collect(Collectors.toList());
    }
    public <T> List<T> filter(List<T> collection, Predicate<T> predicate) {
        return filter(collection, Function.identity(), predicate);
    }
    public <T> List<T> filterNull(List<T> collection) {
        return filter(collection, Objects::nonNull);
    }
    public List<String> filterString(List<String> collection) {
        return filter(collection, StringUtils::isNotBlank);
    }
    public <T, K> List<T> filterKey(List<T> collection, Function<T, K> convert) {
        return filter(collection, convert, Objects::nonNull);
    }
    public <T> List<T> filterStringKey(List<T> collection, Function<T, String> convert) {
        return filter(collection, convert, StringUtils::isNotBlank);
    }

    public <O, R> Set<O> filter(Set<O> collection, Function<O, R> convert, Predicate<R> predicate) {
        if(CollUtil.isEmpty(collection)) {
            return CSet.of();
        }

        return collection.stream()
                .filter(e -> predicate.test(CObjUtils.convert(e, convert)))
                .collect(CCollectors.toLinkedSet());
    }
    public <T> Set<T> filter(Set<T> collection, Predicate<T> predicate) {
        return filter(collection, Function.identity(), predicate);
    }
    public <T> Set<T> filterNull(Set<T> collection) {
        return filter(collection, Objects::nonNull);
    }
    public Set<String> filterString(Set<String> collection) {
        return filter(collection, StringUtils::isNotBlank);
    }
    public <T, K> Set<T> filterKey(Set<T> collection, Function<T, K> convert) {
        return filter(collection, convert, Objects::nonNull);
    }
    public <T> Set<T> filterStringKey(Set<T> collection, Function<T, String> convert) {
        return filter(collection, convert, StringUtils::isNotBlank);
    }

    public <T, K, C extends Collection<K>> C convert(
            Collection<T> collection,
            Function<T, K> convert,
            Predicate<K> predicate,
            Supplier<C> cSupplier
    ) {

        if(CollUtil.isEmpty(collection)) {
            return cSupplier.get();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .map(convert)
                .filter(predicate)
                .collect(Collectors.toCollection(cSupplier))
                ;
    }



    public <T, K> List<K> convert(
            Collection<T> collection,
            Function<T, K> convert,
            Predicate<K> predicate
    ) {
        return convert(collection, convert, predicate, ArrayList::new);
    }

    public <T, K> List<K> convert(Collection<T> collection, Function<T, K> convert) {
        return convert(collection, convert, Objects::nonNull);
    }

    public <T, K> Set<K> convertSet(Collection<T> collection, Function<T, K> convert, Predicate<K> predicate) {
        return convert(collection, convert, predicate, LinkedHashSet::new);
    }

    public <T, K> Set<K> convertSet(Collection<T> collection, Function<T, K> convert) {
        return convertSet(collection, convert, Objects::nonNull);
    }

    public <O, R> List<R> convertToList(O o, Function<O, List<R>> function) {
        return Objects.nonNull(o) ? function.apply(o) : CList.of();
    }

    public static <O, R> Set<R> convertToSet(O o, Function<O, Set<R>> function) {
        return Objects.nonNull(o) ? function.apply(o) : CSet.of();
    }

    public static <O, R> Collection<R> convertToCollection(O o, Function<O, Collection<R>> function) {
        return Objects.nonNull(o) ? function.apply(o) : CList.of();
    }

    public static <T> Collection<String> convertString(Collection<T> collection, Function<T, String> convert) {
        return convert(collection, convert, StringUtils::isNotBlank);
    }

    public static <T, K> Collection<K> convertCollection(Collection<T> collection, Function<Collection<T>, Collection<K>> convert) {

        collection = defaultEmpty(collection);
        collection = collection.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if(CollUtil.isEmpty(collection)) {
            return CList.of();
        }

        return convert.apply(collection);
    }

    public static <T> List<T> newList(int size) {
        if(0 == size) {
            return CList.of();
        }
        return new ArrayList<>(size);
    }

    public static <T> Set<T> newSet(int size) {
        if(0 == size) {
            return CSet.of();
        }
        return new HashSet<>(size);
    }

    public static <T> Set<T> newLinkedSet(int size) {
        if(0 == size) {
            return CSet.of();
        }
        return new LinkedHashSet<>(size);
    }

    public static <K, V> Map<K, V> newMap(int size) {
        if(0 == size) {
            return CMap.of();
        }
        return new HashMap<>(size);
    }

    public static <K, V> Map<K, V> newLinkedMap(int size) {
        if(0 == size) {
            return CMap.of();
        }
        return new LinkedHashMap<>(size);
    }

    public static <T> boolean contains(Collection<T> collection, T element) {
        if(CollUtil.isEmpty(collection)) {
            return false;
        }
        return collection.contains(element);
    }


    public static  <T> T get(List<T> list, int index) {
        if(CollUtil.isEmpty(list) || index < 0 || index >= list.size()) {
            return null;
        }

        return list.get(index);
    }

    public static <T> T first(Collection<T> collection) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        return collection.stream()
                .findFirst()
                .orElse(null);
    }

    public static  <T> T last(Collection<T> collection) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        if(collection instanceof List) {
            return ((List<T>) collection)
                    .get(collection.size() - 1);
        }

        return collection.stream()
                .reduce((first,  second) -> second)
                .orElse(null);
    }

    public static <T, U extends Comparable<? super U>> T min(Collection<T> collection, Function<? super T, ? extends U> convert) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        return collection.stream()
                .filter(e -> Objects.nonNull(convert.apply(e)))
                .min(Comparator.comparing(convert))
                .orElse(null);
    }

    public static <T, U extends Comparable<? super U>> T max(Collection<T> collection, Function<? super T, ? extends U> convert) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        return collection.stream()
                .filter(Objects::nonNull)
                .filter(e -> Objects.nonNull(convert.apply(e)))
                .max(Comparator.comparing(convert))
                .orElse(null);
    }

    public static <T> Stream<T> stream(Collection<T> collection) {
        return defaultEmpty(collection).stream();
    }

    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            Function<T, K> toKey
    ) {
        return toMap(collection, toKey, (BiFunction<T, T, T>)null);
    }

    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            Function<T, K> toKey,
            Predicate<K> predicate
    ) {
        return toMap(collection, toKey, predicate, null);
    }

    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            Function<T, K> toKey,
            BiFunction<T, T, T> mergeFunction
    ) {
        return toMap(collection, toKey, null, mergeFunction);
    }

    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            Function<T, K> toKey,
            Predicate<K> predicate,
            BiFunction<T, T, T> mergeFunction
    ) {
        return toMap(collection, toKey, t -> t, predicate, mergeFunction);
    }

    public static <T, K, V> Map<K, V> toMap(
            Collection<T> collection,
            Function<T, K> toKey,
            Function<T, V> toValue
    ) {
        return toMap(collection, toKey, toValue, null, null);
    }

    public static <T, K, V> Map<K, V> toMap(
            Collection<T> collection,
            Function<T, K> toKey,
            Function<T, V> toValue,
            Predicate<K> predicate,
            BiFunction<V, V, V> mergeFunction
    ) {

        if(null == predicate) {
            predicate = Objects::nonNull;
        }

        collection = filter(collection, toKey, predicate);
        if(CollUtil.isEmpty(collection)) {
            return CMap.of();
        }

        val first = first(collection);
        val firstKey = CObjUtils.convert(first, toKey);
        val keyType = CObjUtils.convert(firstKey, e -> e.getClass());

        val map = CMapUtils.<K, V>newMap(keyType, collection.size());
        collection.forEach(t -> {

            val value = toValue.apply(t);
            if(null == value) {
                return;
            }

            map.compute(toKey.apply(t),
                    (k, v) -> CObjUtils.merge(v, value, mergeFunction));
        });
        return Collections.unmodifiableMap(map);
    }

}

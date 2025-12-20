package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.core.validation.CAssert;
import com.c332030.ctool4j.definition.function.CBiFunction;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.function.CPredicate;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.*;
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

    /**
     * 集合遍历
     * @param collection 集合
     * @param consumer 消费方法
     * @param <T> T
     */
    public <T> void forEach(Collection<T> collection, CConsumer<T> consumer) {
        if(CollUtil.isNotEmpty(collection)) {
            collection.forEach(consumer);
        }
    }

    public <K, V> Map<K, List<V>> groupingBy(Collection<V> collection, CFunction<V, K> function) {
        return groupingBy(collection, function, Objects::nonNull);
    }

    public <K, V> Map<K, List<V>> groupingBy(Collection<V> collection, CFunction<V, K> function, CPredicate<K> predicate) {

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

    public <O, R> Collection<O> filter(Collection<O> collection, CFunction<O, R> convert, CPredicate<R> predicate) {
        if(CollUtil.isEmpty(collection)) {
            return CList.of();
        }
        return collection.stream()
                .filter(e -> predicate.test(CObjUtils.convert(e, convert)))
                .collect(Collectors.toList());
    }
    public <T> Collection<T> filter(Collection<T> collection, CPredicate<T> predicate) {
        return filter(collection, CFunction.self(), predicate);
    }
    public <T> Collection<T> filterNull(Collection<T> collection) {
        return filter(collection, Objects::nonNull);
    }
    public Collection<String> filterString(Collection<String> collection) {
        return filter(collection, StrUtil::isNotBlank);
    }
    public <T, K> Collection<T> filterKey(Collection<T> collection, CFunction<T, K> convert) {
        return filter(collection, convert, Objects::nonNull);
    }
    public <T> Collection<T> filterStringKey(Collection<T> collection, CFunction<T, String> convert) {
        return filter(collection, convert, StrUtil::isNotBlank);
    }

    public <O, R> List<O> filter(List<O> collection, CFunction<O, R> convert, CPredicate<R> predicate) {
        if(CollUtil.isEmpty(collection)) {
            return CList.of();
        }
        return collection.stream()
                .filter(e -> predicate.test(CObjUtils.convert(e, convert)))
                .collect(Collectors.toList());
    }
    public <T> List<T> filter(List<T> collection, CPredicate<T> predicate) {
        return filter(collection, CFunction.self(), predicate);
    }
    public <T> List<T> filterNull(List<T> collection) {
        return filter(collection, Objects::nonNull);
    }
    public List<String> filterString(List<String> collection) {
        return filter(collection, StrUtil::isNotBlank);
    }
    public <T, K> List<T> filterKey(List<T> collection, CFunction<T, K> convert) {
        return filter(collection, convert, Objects::nonNull);
    }
    public <T> List<T> filterStringKey(List<T> collection, CFunction<T, String> convert) {
        return filter(collection, convert, StrUtil::isNotBlank);
    }

    public <O, R> Set<O> filter(Set<O> collection, CFunction<O, R> convert, CPredicate<R> predicate) {
        if(CollUtil.isEmpty(collection)) {
            return CSet.of();
        }

        return collection.stream()
                .filter(e -> predicate.test(CObjUtils.convert(e, convert)))
                .collect(CCollectors.toLinkedSet());
    }
    public <T> Set<T> filter(Set<T> collection, CPredicate<T> predicate) {
        return filter(collection, CFunction.self(), predicate);
    }
    public <T> Set<T> filterNull(Set<T> collection) {
        return filter(collection, Objects::nonNull);
    }
    public Set<String> filterString(Set<String> collection) {
        return filter(collection, StrUtil::isNotBlank);
    }
    public <T, K> Set<T> filterKey(Set<T> collection, CFunction<T, K> convert) {
        return filter(collection, convert, Objects::nonNull);
    }
    public <T> Set<T> filterStringKey(Set<T> collection, CFunction<T, String> convert) {
        return filter(collection, convert, StrUtil::isNotBlank);
    }

    public <T, K, C extends Collection<K>> C convert(
            Collection<T> collection,
            CFunction<T, K> convert,
            CPredicate<K> predicate,
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
            CFunction<T, K> convert,
            CPredicate<K> predicate
    ) {
        return convert(collection, convert, predicate, ArrayList::new);
    }

    public <T, K> List<K> convert(Collection<T> collection, CFunction<T, K> convert) {
        return convert(collection, convert, Objects::nonNull);
    }

    public <T, K> Set<K> convertSet(Collection<T> collection, CFunction<T, K> convert, CPredicate<K> predicate) {
        return convert(collection, convert, predicate, LinkedHashSet::new);
    }

    public <T, K> Set<K> convertSet(Collection<T> collection, CFunction<T, K> convert) {
        return convertSet(collection, convert, Objects::nonNull);
    }

    public <O, R> List<R> convertToList(O o, CFunction<O, List<R>> function) {
        return Objects.nonNull(o) ? function.apply(o) : CList.of();
    }

    public static <O, R> Set<R> convertToSet(O o, CFunction<O, Set<R>> function) {
        return Objects.nonNull(o) ? function.apply(o) : CSet.of();
    }

    public static <O, R> Collection<R> convertToCollection(O o, CFunction<O, Collection<R>> function) {
        return Objects.nonNull(o) ? function.apply(o) : CList.of();
    }

    public static <T> Collection<String> convertString(Collection<T> collection, CFunction<T, String> convert) {
        return convert(collection, convert, StrUtil::isNotBlank);
    }

    public static <T, K> Collection<K> convertCollection(Collection<T> collection, CFunction<Collection<T>, Collection<K>> convert) {

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

    public <T> T onlyOne(Collection<T> collection) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        val size = collection.size();
        CAssert.isTrue(size == 1, () -> "collection more then one value, size: " + size);

        return first(collection);
    }

    public static <T, U extends Comparable<? super U>> T min(Collection<T> collection, CFunction<? super T, ? extends U> convert) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        return collection.stream()
                .filter(e -> Objects.nonNull(convert.apply(e)))
                .min(Comparator.comparing(convert))
                .orElse(null);
    }

    public static <T, U extends Comparable<? super U>> T max(Collection<T> collection, CFunction<? super T, ? extends U> convert) {

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
            CFunction<T, K> toKey
    ) {
        return toMap(collection, toKey, (CBiFunction<T, T, T>)null);
    }

    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CPredicate<K> predicate
    ) {
        return toMap(collection, toKey, predicate, null);
    }

    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CBiFunction<T, T, T> mergeFunction
    ) {
        return toMap(collection, toKey, null, mergeFunction);
    }

    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CPredicate<K> predicate,
            CBiFunction<T, T, T> mergeFunction
    ) {
        return toMap(collection, toKey, t -> t, predicate, mergeFunction);
    }

    public static <T, K, V> Map<K, V> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CFunction<T, V> toValue
    ) {
        return toMap(collection, toKey, toValue, null, null);
    }

    public static <T, K, V> Map<K, V> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CFunction<T, V> toValue,
            CPredicate<K> predicate,
            CBiFunction<V, V, V> mergeFunction
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

    @SafeVarargs
    public <T> boolean containsAny(Collection<T> collection, T... elements) {

        if(ArrayUtil.isEmpty(elements)) {
            return false;
        }

        return CollUtil.containsAny(collection, Arrays.asList(elements));
    }

    /**
     * 获取枚举所有值
     * @param enumeration 枚举
     * @return 枚举所有值
     * @param <T> 泛型
     */
    public <T> List<T> getValues(Enumeration<T> enumeration) {

        if(Objects.isNull(enumeration)) {
            return CList.of();
        }

        val values = new ArrayList<T>();
        while (enumeration.hasMoreElements()) {
            values.add(enumeration.nextElement());
        }
        return values;
    }

    /**
     * 获取集合大小
     * @param collection 集合
     * @return 集合大小
     */
    public int size(Collection<?> collection) {
        if(CollUtil.isEmpty(collection)) {
            return 0;
        }
        return collection.size();
    }

}

package com.c332030.ctool4j.core.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
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
 * @see doc/design/core/CCollUtils.adoc
 * @see doc/design/core/CCollUtilsTests.adoc
 */
@UtilityClass
public class CCollUtils {

    /**
     * 集合为空时返回空集合
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 原集合或空集合
     */
    public <T> Collection<T> defaultEmpty(Collection<T> collection) {
        return CollUtil.isEmpty(collection) ? CList.of() : collection;
    }

    /**
     * 列表为空时返回空列表
     *
     * @param list 列表
     * @param <T>  元素类型
     * @return 原列表或空列表
     */
    public <T> List<T> defaultEmpty(List<T> list) {
        return CollUtil.isEmpty(list) ? CList.of() : list;
    }

    /**
     * 集合为空时返回空集合
     *
     * @param list 集合
     * @param <T>  元素类型
     * @return 原集合或空集合
     */
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

    /**
     * 按 key 分组
     *
     * @param collection 集合
     * @param function   key 提取函数
     * @param <K>        key 类型
     * @param <V>        元素类型
     * @return 分组后的不可变 Map
     */
    public <K, V> Map<K, List<V>> groupingBy(Collection<V> collection, CFunction<V, K> function) {
        return groupingBy(collection, function, Objects::nonNull);
    }

    /**
     * 按 key 分组（key 需满足过滤条件）
     *
     * @param collection 集合
     * @param function   key 提取函数
     * @param predicate  key 过滤条件
     * @param <K>        key 类型
     * @param <V>        元素类型
     * @return 分组后的不可变 Map
     */
    public <K, V> Map<K, List<V>> groupingBy(Collection<V> collection, CFunction<V, K> function, CPredicate<K> predicate) {

        if(CollUtil.isEmpty(collection)) {
            return CMap.of();
        }

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

    /**
     * 忽略 null 值添加元素
     *
     * @param collection 集合
     * @param value      元素
     * @param <P>        元素类型
     */
    public <P> void addIgnoreNull(Collection<P> collection, P value) {
        if(null != value) {
            collection.add(value);
        }
    }

    /**
     * 忽略空字符串添加元素
     *
     * @param collection 集合
     * @param value      元素
     * @param <T>        元素类型
     */
    public <T extends CharSequence> void addIgnoreEmpty(Collection<T> collection, T value) {
        if(StrUtil.isNotEmpty(value)) {
            collection.add(value);
        }
    }

    /**
     * 忽略空白字符串添加元素
     *
     * @param collection 集合
     * @param value      元素
     * @param <T>        元素类型
     */
    public <T extends CharSequence> void addIgnoreBlank(Collection<T> collection, T value) {
        if(StrUtil.isNotBlank(value)) {
            collection.add(value);
        }
    }

    /**
     * 忽略 null 集合添加全部元素
     *
     * @param collection1 目标集合
     * @param collection2 源集合
     * @param <P>         元素类型
     * @param <C>         源集合元素类型
     */
    public <P, C extends P> void addAllIgnoreNull(Collection<P> collection1, Collection<C> collection2) {
        if(null != collection2) {
            collection1.addAll(collection2);
        }
    }

    /**
     * 链接集合和元素，与 addFirst 的区别：此方法返回一个新集合
     * @param p 元素
     * @param collection 集合
     * @return 新 list
     * @param <P> 泛型
     */
    public <P> List<P> concatOne(P p, Collection<? extends P> collection) {

        val size1 = null == p ? 0 : 1;
        val size2 = size(collection);

        val list = new ArrayList<P>(size1 + size2);
        if(null != p) {
            list.add(p);
        }
        if(CollUtil.isNotEmpty(collection)) {
            list.addAll(collection);
        }

        return list;
    }

    /**
     * 链接集合和元素，与 addFirst 的区别：此方法返回一个新集合
     * @param collection 集合
     * @param p 元素
     * @return 新 list
     * @param <P> 泛型
     */
    public <P> List<P> concatOne(Collection<? extends P> collection, P p) {

        val size1 = null == p ? 0 : 1;
        val size2 = size(collection);

        val list = new ArrayList<P>(size1 + size2);
        if(CollUtil.isNotEmpty(collection)) {
            list.addAll(collection);
        }
        if(null != p) {
            list.add(p);
        }

        return list;
    }

    /**
     * 拼接多个集合（跳过 null 集合）
     *
     * @param collections 集合数组
     * @param <P>         元素类型
     * @return 拼接后的新列表
     */
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

    /**
     * 按转换结果过滤集合
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param predicate  转换结果过滤条件
     * @param <O>        元素类型
     * @param <R>        转换结果类型
     * @return 过滤后的集合
     */
    public <O, R> Collection<O> filter(Collection<O> collection, CFunction<O, R> convert, CPredicate<R> predicate) {
        if(CollUtil.isEmpty(collection)) {
            return CList.of();
        }
        return collection.stream()
                .filter(e -> predicate.test(CObjUtils.convert(e, convert)))
                .collect(Collectors.toList());
    }

    /**
     * 按条件过滤集合
     *
     * @param collection 集合
     * @param predicate  过滤条件
     * @param <T>        元素类型
     * @return 过滤后的集合
     */
    public <T> Collection<T> filter(Collection<T> collection, CPredicate<T> predicate) {
        return filter(collection, CFunction.self(), predicate);
    }

    /**
     * 过滤 null 元素
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 过滤后的集合
     */
    public <T> Collection<T> filterNull(Collection<T> collection) {
        return filter(collection, Objects::nonNull);
    }

    /**
     * 过滤空白字符串
     *
     * @param collection 字符串集合
     * @return 过滤后的集合
     */
    public Collection<String> filterString(Collection<String> collection) {
        return filter(collection, StrUtil::isNotBlank);
    }

    /**
     * 过滤转换结果为 null 的元素
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param <T>        元素类型
     * @param <K>        转换结果类型
     * @return 过滤后的集合
     */
    public <T, K> Collection<T> filterKey(Collection<T> collection, CFunction<T, K> convert) {
        return filter(collection, convert, Objects::nonNull);
    }

    /**
     * 过滤转换结果为空白字符串的元素
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param <T>        元素类型
     * @return 过滤后的集合
     */
    public <T> Collection<T> filterStringKey(Collection<T> collection, CFunction<T, String> convert) {
        return filter(collection, convert, StrUtil::isNotBlank);
    }

    /**
     * 按转换结果过滤列表
     *
     * @param collection 列表
     * @param convert    转换函数
     * @param predicate  转换结果过滤条件
     * @param <O>        元素类型
     * @param <R>        转换结果类型
     * @return 过滤后的列表
     */
    public <O, R> List<O> filter(List<O> collection, CFunction<O, R> convert, CPredicate<R> predicate) {
        if(CollUtil.isEmpty(collection)) {
            return CList.of();
        }
        return collection.stream()
                .filter(e -> predicate.test(CObjUtils.convert(e, convert)))
                .collect(Collectors.toList());
    }

    /**
     * 按条件过滤列表
     *
     * @param collection 列表
     * @param predicate  过滤条件
     * @param <T>        元素类型
     * @return 过滤后的列表
     */
    public <T> List<T> filter(List<T> collection, CPredicate<T> predicate) {
        return filter(collection, CFunction.self(), predicate);
    }

    /**
     * 过滤 null 元素
     *
     * @param collection 列表
     * @param <T>        元素类型
     * @return 过滤后的列表
     */
    public <T> List<T> filterNull(List<T> collection) {
        return filter(collection, Objects::nonNull);
    }

    /**
     * 过滤空白字符串
     *
     * @param collection 字符串列表
     * @return 过滤后的列表
     */
    public List<String> filterString(List<String> collection) {
        return filter(collection, StrUtil::isNotBlank);
    }

    /**
     * 过滤转换结果为 null 的元素
     *
     * @param collection 列表
     * @param convert    转换函数
     * @param <T>        元素类型
     * @param <K>        转换结果类型
     * @return 过滤后的列表
     */
    public <T, K> List<T> filterKey(List<T> collection, CFunction<T, K> convert) {
        return filter(collection, convert, Objects::nonNull);
    }

    /**
     * 过滤转换结果为空白字符串的元素
     *
     * @param collection 列表
     * @param convert    转换函数
     * @param <T>        元素类型
     * @return 过滤后的列表
     */
    public <T> List<T> filterStringKey(List<T> collection, CFunction<T, String> convert) {
        return filter(collection, convert, StrUtil::isNotBlank);
    }

    /**
     * 按转换结果过滤集合
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param predicate  转换结果过滤条件
     * @param <O>        元素类型
     * @param <R>        转换结果类型
     * @return 过滤后的集合
     */
    public <O, R> Set<O> filter(Set<O> collection, CFunction<O, R> convert, CPredicate<R> predicate) {
        if(CollUtil.isEmpty(collection)) {
            return CSet.of();
        }

        return collection.stream()
                .filter(e -> predicate.test(CObjUtils.convert(e, convert)))
                .collect(CCollectors.toLinkedSet());
    }

    /**
     * 按条件过滤集合
     *
     * @param collection 集合
     * @param predicate  过滤条件
     * @param <T>        元素类型
     * @return 过滤后的集合
     */
    public <T> Set<T> filter(Set<T> collection, CPredicate<T> predicate) {
        return filter(collection, CFunction.self(), predicate);
    }

    /**
     * 过滤 null 元素
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 过滤后的集合
     */
    public <T> Set<T> filterNull(Set<T> collection) {
        return filter(collection, Objects::nonNull);
    }

    /**
     * 过滤空白字符串
     *
     * @param collection 字符串集合
     * @return 过滤后的集合
     */
    public Set<String> filterString(Set<String> collection) {
        return filter(collection, StrUtil::isNotBlank);
    }

    /**
     * 过滤转换结果为 null 的元素
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param <T>        元素类型
     * @param <K>        转换结果类型
     * @return 过滤后的集合
     */
    public <T, K> Set<T> filterKey(Set<T> collection, CFunction<T, K> convert) {
        return filter(collection, convert, Objects::nonNull);
    }

    /**
     * 过滤转换结果为空白字符串的元素
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param <T>        元素类型
     * @return 过滤后的集合
     */
    public <T> Set<T> filterStringKey(Set<T> collection, CFunction<T, String> convert) {
        return filter(collection, convert, StrUtil::isNotBlank);
    }

    /**
     * 转换集合到指定类型集合
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param predicate  转换结果过滤条件
     * @param cSupplier  目标集合供应商
     * @param <T>        源元素类型
     * @param <K>        目标元素类型
     * @param <C>        目标集合类型
     * @return 转换后的集合
     */
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

    /**
     * 转换集合到列表
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param predicate  转换结果过滤条件
     * @param <T>        源元素类型
     * @param <K>        目标元素类型
     * @return 转换后的列表
     */
    public <T, K> List<K> convert(
            Collection<T> collection,
            CFunction<T, K> convert,
            CPredicate<K> predicate
    ) {
        return convert(collection, convert, predicate, ArrayList::new);
    }

    /**
     * 转换集合到列表（过滤 null 结果）
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param <T>        源元素类型
     * @param <K>        目标元素类型
     * @return 转换后的列表
     */
    public <T, K> List<K> convert(Collection<T> collection, CFunction<T, K> convert) {
        return convert(collection, convert, Objects::nonNull);
    }

    /**
     * 转换集合到集合
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param predicate  转换结果过滤条件
     * @param <T>        源元素类型
     * @param <K>        目标元素类型
     * @return 转换后的集合
     */
    public <T, K> Set<K> convertSet(Collection<T> collection, CFunction<T, K> convert, CPredicate<K> predicate) {
        return convert(collection, convert, predicate, LinkedHashSet::new);
    }

    /**
     * 转换集合到集合（过滤 null 结果）
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param <T>        源元素类型
     * @param <K>        目标元素类型
     * @return 转换后的集合
     */
    public <T, K> Set<K> convertSet(Collection<T> collection, CFunction<T, K> convert) {
        return convertSet(collection, convert, Objects::nonNull);
    }

    /**
     * 对象经函数转列表，对象为 null 时返回空列表
     *
     * @param o        对象
     * @param function 转列表函数
     * @param <O>      对象类型
     * @param <R>      列表元素类型
     * @return 列表
     */
    public <O, R> List<R> convertToList(O o, CFunction<O, List<R>> function) {
        return Objects.nonNull(o) ? function.apply(o) : CList.of();
    }

    /**
     * 对象经函数转集合，对象为 null 时返回空集合
     *
     * @param o        对象
     * @param function 转集合函数
     * @param <O>      对象类型
     * @param <R>      集合元素类型
     * @return 集合
     */
    public static <O, R> Set<R> convertToSet(O o, CFunction<O, Set<R>> function) {
        return Objects.nonNull(o) ? function.apply(o) : CSet.of();
    }

    /**
     * 对象经函数转集合，对象为 null 时返回空集合
     *
     * @param o        对象
     * @param function 转集合函数
     * @param <O>      对象类型
     * @param <R>      集合元素类型
     * @return 集合
     */
    public static <O, R> Collection<R> convertToCollection(O o, CFunction<O, Collection<R>> function) {
        return Objects.nonNull(o) ? function.apply(o) : CList.of();
    }

    /**
     * 集合元素转字符串并过滤空白
     *
     * @param collection 集合
     * @param convert    转字符串函数
     * @param <T>        元素类型
     * @return 字符串集合
     */
    public static <T> Collection<String> convertString(Collection<T> collection, CFunction<T, String> convert) {
        return convert(collection, convert, StrUtil::isNotBlank);
    }

    /**
     * 集合整体转换（先过滤 null 元素）
     *
     * @param collection 集合
     * @param convert    整体转换函数
     * @param <T>        源元素类型
     * @param <K>        目标元素类型
     * @return 转换后的集合
     */
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

    /**
     * 新建指定容量列表
     *
     * @param size 容量
     * @param <T>  元素类型
     * @return 列表
     */
    public static <T> List<T> newList(int size) {
        if(0 == size) {
            return CList.of();
        }
        return new ArrayList<>(size);
    }

    /**
     * 新建指定容量集合
     *
     * @param size 容量
     * @param <T>  元素类型
     * @return 集合
     */
    public static <T> Set<T> newSet(int size) {
        if(0 == size) {
            return CSet.of();
        }
        return new HashSet<>(size);
    }

    /**
     * 新建指定容量有序集合
     *
     * @param size 容量
     * @param <T>  元素类型
     * @return 有序集合
     */
    public static <T> Set<T> newLinkedSet(int size) {
        if(0 == size) {
            return CSet.of();
        }
        return new LinkedHashSet<>(size);
    }

    /**
     * 新建指定容量 Map
     *
     * @param size 容量
     * @param <K>  键类型
     * @param <V>  值类型
     * @return Map
     */
    public static <K, V> Map<K, V> newMap(int size) {
        if(0 == size) {
            return CMap.of();
        }
        return new HashMap<>(size);
    }

    /**
     * 新建指定容量有序 Map
     *
     * @param size 容量
     * @param <K>  键类型
     * @param <V>  值类型
     * @return 有序 Map
     */
    public static <K, V> Map<K, V> newLinkedMap(int size) {
        if(0 == size) {
            return CMap.of();
        }
        return new LinkedHashMap<>(size);
    }

    /**
     * 判断集合是否包含元素
     *
     * @param collection 集合
     * @param element    元素
     * @param <T>        元素类型
     * @return 是否包含
     */
    public static <T> boolean contains(Collection<T> collection, T element) {
        if(CollUtil.isEmpty(collection)) {
            return false;
        }
        return collection.contains(element);
    }


    /**
     * 获取列表指定下标元素（越界返回 null）
     *
     * @param list  列表
     * @param index 下标
     * @param <T>   元素类型
     * @return 元素，越界或为空时返回 null
     */
    public static  <T> T get(List<T> list, int index) {
        if(CollUtil.isEmpty(list) || index < 0 || index >= list.size()) {
            return null;
        }

        return list.get(index);
    }

    /**
     * 获取集合第一个元素
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 第一个元素，为空时返回 null
     */
    public static <T> T first(Collection<T> collection) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        return collection.stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取集合最后一个元素
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 最后一个元素，为空时返回 null
     */
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

    /**
     * 获取集合唯一元素
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 唯一元素，为空时返回 null
     * @throws IllegalArgumentException 集合包含多个元素时抛出
     */
    public <T> T onlyOne(Collection<T> collection) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        val size = collection.size();
        CAssert.isTrue(size == 1, () -> "collection more then one value, size: " + size);

        return first(collection);
    }

    /**
     * 按转换结果取集合最小值
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param <T>        元素类型
     * @param <U>        比较值类型
     * @return 最小值，为空时返回 null
     */
    public static <T, U extends Comparable<? super U>> T min(Collection<T> collection, CFunction<? super T, ? extends U> convert) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        return collection.stream()
                .filter(Objects::nonNull)
                .filter(e -> Objects.nonNull(convert.apply(e)))
                .min(Comparator.comparing(convert))
                .orElse(null);
    }

    /**
     * 按转换结果取集合最大值
     *
     * @param collection 集合
     * @param convert    转换函数
     * @param <T>        元素类型
     * @param <U>        比较值类型
     * @return 最大值，为空时返回 null
     */
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

    /**
     * 集合转 Stream（为空时返回空流）
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return Stream
     */
    public static <T> Stream<T> stream(Collection<T> collection) {
        return defaultEmpty(collection).stream();
    }

    /**
     * 集合转 Map（元素自身为值）
     *
     * @param collection 集合
     * @param toKey      键提取函数
     * @param <T>        元素类型
     * @param <K>        键类型
     * @return 不可变 Map
     */
    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey
    ) {
        return toMap(collection, toKey, (CBiFunction<T, T, T>)null);
    }

    /**
     * 集合转 Map（键需满足过滤条件，元素自身为值）
     *
     * @param collection 集合
     * @param toKey      键提取函数
     * @param predicate  键过滤条件
     * @param <T>        元素类型
     * @param <K>        键类型
     * @return 不可变 Map
     */
    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CPredicate<K> predicate
    ) {
        return toMap(collection, toKey, predicate, null);
    }

    /**
     * 集合转 Map（带键冲突合并，元素自身为值）
     *
     * @param collection   集合
     * @param toKey        键提取函数
     * @param mergeFunction 键冲突合并函数
     * @param <T>          元素类型
     * @param <K>          键类型
     * @return 不可变 Map
     */
    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CBiFunction<T, T, T> mergeFunction
    ) {
        return toMap(collection, toKey, (CPredicate<K>) null, mergeFunction);
    }

    /**
     * 集合转 Map（带键过滤与键冲突合并，元素自身为值）
     *
     * @param collection   集合
     * @param toKey        键提取函数
     * @param predicate    键过滤条件
     * @param mergeFunction 键冲突合并函数
     * @param <T>          元素类型
     * @param <K>          键类型
     * @return 不可变 Map
     */
    public static <T, K> Map<K, T> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CPredicate<K> predicate,
            CBiFunction<T, T, T> mergeFunction
    ) {
        return toMap(collection, toKey, t -> t, predicate, mergeFunction);
    }

    /**
     * 集合转 Map（键值分别提取）
     *
     * @param collection 集合
     * @param toKey      键提取函数
     * @param toValue    值提取函数
     * @param <T>        元素类型
     * @param <K>        键类型
     * @param <V>        值类型
     * @return 不可变 Map
     */
    public static <T, K, V> Map<K, V> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CFunction<T, V> toValue
    ) {
        return toMap(collection, toKey, toValue, null, null);
    }

    /**
     * 集合转 Map（带值冲突合并）
     *
     * @param collection   集合
     * @param toKey        键提取函数
     * @param toValue      值提取函数
     * @param mergeFunction 值冲突合并函数
     * @param <T>          元素类型
     * @param <K>          键类型
     * @param <V>          值类型
     * @return 不可变 Map
     */
    public static <T, K, V> Map<K, V> toMap(
            Collection<T> collection,
            CFunction<T, K> toKey,
            CFunction<T, V> toValue,
            CBiFunction<V, V, V> mergeFunction
    ) {
        return toMap(collection, toKey, toValue, null, mergeFunction);
    }

    /**
     * 集合转 Map（带键过滤与值冲突合并）
     *
     * @param collection   集合
     * @param toKey        键提取函数
     * @param toValue      值提取函数
     * @param predicate    键过滤条件
     * @param mergeFunction 值冲突合并函数
     * @param <T>          元素类型
     * @param <K>          键类型
     * @param <V>          值类型
     * @return 不可变 Map
     */
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
                    (k, v) -> CObjUtils.merge(k, v, value, mergeFunction));
        });
        return Collections.unmodifiableMap(map);
    }

    /**
     * Pair 列表转 Map
     *
     * @param pairs Pair 列表
     * @param <K>   键类型
     * @param <V>   值类型
     * @return 不可变 Map
     */
    public <K, V> Map<K, V> toMap(List<Pair<K, V>> pairs) {
        return toMap(pairs, Pair::getKey, (CFunction<Pair<K,V>, V>) Pair::getValue);
    }

    /**
     * 判断集合是否包含任一元素
     *
     * @param collection 集合
     * @param elements   元素数组
     * @param <T>        元素类型
     * @return 是否包含任一元素
     */
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

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
import lombok.var;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Description: CCollUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CCollUtils} 为集合工具类，提供丰富的集合操作：</p>
 * <ul>
 *   <li>空兜底：{@code defaultEmpty}、{@code size}</li>
 *   <li>遍历/分组：{@code forEach}、{@code groupingBy}</li>
 *   <li>添加/拼接：{@code addIgnoreNull}/{@code addIgnoreEmpty}/{@code addIgnoreBlank}、{@code addAllIgnoreNull}、{@code concat}、{@code concatOne}</li>
 *   <li>过滤：{@code filter}（Collection/List/Set × predicate/convert）/{@code filterNull}/{@code filterString}/{@code filterKey}/{@code filterStringKey}</li>
 *   <li>转换：{@code convert}（多版本）/{@code convertSet}/{@code convertToList}/{@code convertToSet}/{@code convertToCollection}/{@code convertString}/{@code convertCollection}</li>
 *   <li>新建：{@code newList}/{@code newSet}/{@code newLinkedSet}/{@code newMap}/{@code newLinkedMap}</li>
 *   <li>元素：{@code contains}/{@code containsAny}/{@code get}/{@code first}/{@code last}/{@code onlyOne}/{@code getValues}</li>
 *   <li>最值：{@code min}/{@code max}（按字段取值）</li>
 *   <li>其他：{@code stream}、{@code toMap}（多版本）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>defaultEmpty null/空集合</td>
 *     <td>返回空集合（非 null）</td>
 *   </tr>
 *   <tr>
 *     <td>filter/concat 等空/null 入参</td>
 *     <td>返回空结果，不抛异常</td>
 *   </tr>
 *   <tr>
 *     <td>get 越界/负索引/null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>first/last 空/null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>onlyOne 空/单元素/多元素</td>
 *     <td>null / 元素 / 抛业务异常</td>
 *   </tr>
 *   <tr>
 *     <td>min/max 空/null/全 null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>toMap value 为 null / key 冲突无 merge</td>
 *     <td>跳过 / 抛 IllegalStateException</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>集合的过滤、转换、拼接、分组、取元素、最值、转 Map 等通用操作。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>filter/convert 依赖转换函数，转换结果为 null 的元素被丢弃。</li>
 *   <li>toMap key 冲突无 merge 时抛异常，需显式提供 mergeFunction。</li>
 *   <li>toMap 始终过滤 null key 与 null value；自定义谓词仅可进一步筛选非空 key，放行 null key 无效（见第 2.6 节）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>null/空入参统一返回空结果（而非抛异常），简化调用方判空。</li>
 *   <li>过滤/转换丢弃 null 结果，换取结果集无 null 的安全语义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>空值语义</b></p>
 * <ul>
 *   <li>多数方法对 null/空集合返回空结果（空集合/空 Map/空流/null），不抛异常。</li>
 *   <li>{@code defaultEmpty}：null/空集合返回空集合（非 null）；非空返回原引用。</li>
 * </ul>
 * <p><b>过滤语义</b></p>
 * <ul>
 *   <li>{@code filter}：按 predicate 过滤；null 元素参与 predicate 判断。</li>
 *   <li>{@code filterNull}/{@code filterString}：过滤 null 元素 / 过滤空、空白、null 字符串。</li>
 *   <li>{@code filterKey}/{@code filterStringKey}：按 key（字段取值）过滤，key 为 null 的元素被过滤。</li>
 * </ul>
 * <p><b>转换语义</b></p>
 * <ul>
 *   <li>{@code convert}：元素转换；转换结果为 null 的元素被过滤；可选 predicate 过滤。</li>
 *   <li>空集合返回供应商提供的新集合；null 对象返回空集合。</li>
 *   <li>{@code convertString}：转换后为空白/null 的元素被过滤。</li>
 * </ul>
 * <p><b>元素获取</b></p>
 * <ul>
 *   <li>{@code get}：List 索引取元素，越界/负索引/null 返回 null。</li>
 *   <li>{@code first}/{@code last}：空/null 返回 null；list 走索引 O(1)（List 取首/末元素），非 List 走迭代器单遍遍历，不构造 Stream。</li>
 *   <li>{@code onlyOne}：空返回 null、单个返回、多个抛业务异常。</li>
 * </ul>
 * <p><b>最值与转 Map</b></p>
 * <ul>
 *   <li>{@code min}/{@code max}：按字段取值，null 集合/空/全 null 返回 null，过滤 convert 结果 null 元素；单遍遍历，每元素仅执行一次 convert（避免重复调用转换函数）。</li>
 *   <li>{@code toMap}：key-value 映射；value 为 null 的条目跳过；无 merge 冲突抛 IllegalStateException；</li>
 *   <li>Pair 版本；返回不可变 Map。单遍循环实现，toKey 每个元素仅执行一次。</li>
 *   <li>toMap 始终过滤 null key 与 null value：即使自定义谓词放行 null key，null key 也被跳过。</li>
 *   <li>键类型决定了 Map 实现：首键为枚举时用 EnumMap，其余用 LinkedHashMap（key 已保证非空）。</li>
 * </ul>
 *
 * @since 2024/11/21
 * @version 1.0
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
     * <ul>
     *   <li>{@code newList(0)} 等 size=0 返回不可变空集合；size&gt;0 返回可变集合（size 为初始容量）。</li>
     * </ul>
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
    public static <T> T get(List<T> list, int index) {
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

        if(collection instanceof List) {
            return ((List<T>) collection).get(0);
        }

        return collection.iterator().next();
    }

    /**
     * 获取集合最后一个元素
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 最后一个元素，为空时返回 null
     */
    public static <T> T last(Collection<T> collection) {

        if(CollUtil.isEmpty(collection)) {
            return null;
        }

        if(collection instanceof List) {
            return ((List<T>) collection)
                    .get(collection.size() - 1);
        }

        val iterator = collection.iterator();
        var last = iterator.next();
        while(iterator.hasNext()) {
            last = iterator.next();
        }
        return last;
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

        return collection.iterator().next();
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

        T min = null;
        U minKey = null;
        for (val e : collection) {

            if(e == null) {
                continue;
            }
            val key = convert.apply(e);
            if(key == null) {
                continue;
            }
            if(min == null || key.compareTo(minKey) < 0) {
                min = e;
                minKey = key;
            }
        }

        return min;
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

        T max = null;
        U maxKey = null;
        for (val e : collection) {

            if(e == null) {
                continue;
            }
            val key = convert.apply(e);
            if(key == null) {
                continue;
            }
            if(max == null || key.compareTo(maxKey) > 0) {
                max = e;
                maxKey = key;
            }
        }

        return max;
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

        if(CollUtil.isEmpty(collection)) {
            return CMap.of();
        }

        final CPredicate<K> keyPredicate = predicate;

        // 单遍循环：toKey 每个元素仅执行一次，避免原实现 filter/toMap 重复转换与中间集合
        Map<K, V> map = null;
        for(T t : collection) {

            if(null == t) {
                continue;
            }

            val key = toKey.apply(t);
            // toMap 始终过滤 null key：即使自定义谓词放行 null key，也直接跳过
            if(null == key || !keyPredicate.test(key)) {
                continue;
            }

            val value = toValue.apply(t);
            if(null == value) {
                continue;
            }

            if(null == map) {
                // key 已保证非空，据其类型推断 Map 实现（枚举键用 EnumMap，其余用 LinkedHashMap）
                map = CMapUtils.<K, V>newMap(key.getClass(), collection.size());
            }

            map.compute(key,
                    (k, v) -> CObjUtils.merge(k, v, value, mergeFunction));
        }

        if(null == map) {
            return CMap.of();
        }
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

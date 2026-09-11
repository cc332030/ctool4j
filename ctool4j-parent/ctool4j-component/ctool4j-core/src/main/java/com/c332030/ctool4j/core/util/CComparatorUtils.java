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
 * <h2>能力目录</h2>
 * <p>{@code CComparatorUtils} 为比较工具类，提供：</p>
 * <ul>
 *   <li>{@code min} / {@code max}：取集合或可变参数中的最小/最大值（过滤 null 元素，空集合返回 null）</li>
 *   <li>{@code compareCollection}：按自定义比较器或字段取值后取集合最值</li>
 *   <li>{@code compare}：比较两个值（null 视为最大）</li>
 *   <li>{@code minConsumer} / {@code maxConsumer}：取最值后消费（最值为 null 时不消费）</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>min/max/compareCollection 空集合</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>min/max/compareCollection 全部元素为 null</td>
 *     <td>返回 null（null 元素被过滤）</td>
 *   </tr>
 *   <tr>
 *     <td>字段取值最值：元素或字段值为 null</td>
 *     <td>该元素被过滤，不参与最值</td>
 *   </tr>
 *   <tr>
 *     <td>minConsumer/maxConsumer 最值为 null</td>
 *     <td>不调用 consumer</td>
 *   </tr>
 *   <tr>
 *     <td>compare 双 null</td>
 *     <td>返回 0</td>
 *   </tr>
 *   <tr>
 *     <td>compare(null, x)</td>
 *     <td>返回正数（null 视为最大）</td>
 *   </tr>
 *   <tr>
 *     <td>compare(x, null)</td>
 *     <td>返回负数</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>对可能含 null 元素的集合取最值，避免手写 null 判空。</li>
 *   <li>将 null 排序到末尾的双值比较（{@code compare}）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>最值类方法依赖元素 {@code Comparable} 或显式比较器，非 {@code Comparable} 类型需传比较器。</li>
 *   <li>字段取值最值对字段值为 null 的元素直接过滤，无法表达"null 字段参与排序"。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>最值过滤 null 元素，牺牲"null 参与比较"能力换取无异常取最值便利。</li>
 *   <li>可变参数重载与集合重载语义一致（null 过滤），调用方无需区分。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定</b></p>
 * <ul>
 *   <li>最值类方法（min/max/compareCollection）均<b>过滤 null 元素</b>，对全部为 null 或空集合</li>
 *   <li>返回 null，而非抛异常。</li>
 *   <li>{@code minConsumer} / {@code maxConsumer} 基于 {@code Optional.ofNullable(最值).ifPresent(consumer)}，</li>
 *   <li>最值为 null（空集合/全 null）时不调用 consumer。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>后按比较器 {@code min/max}，{@code .orElse(null)}。</li>
 *   <li>（{@code e != null &amp;&amp; function.apply(e) != null}），再按取值的比较器取最值。</li>
 * </ul>
 *
 * @since 2025/3/26
 * @version 1.0
 */
@UtilityClass
public class CComparatorUtils {

    /**
     * 按比较器取集合中最值（过滤 null 元素）
     * <ul>
     *   <li>基础最值复用 {@code compareCollection(collection, comparator)}：{@code stream.filter(Objects::nonNull)}</li>
     *   <li>{@code min} = {@code compareCollection(collection, Comparable::compareTo)}；</li>
     *   <li>{@code max} = {@code compareCollection(collection, Comparator.reverseOrder())}。</li>
     *   <li>字段取值最值 {@code compareCollection(collection, function, comparator)}：过滤元素与取值双非空</li>
     * </ul>
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
     * <ul>
     *   <li>可变参数重载委托集合重载：{@code min(T... os)} 内部 {@code Arrays.asList(os)} 后走集合路径。</li>
     *   <li>按对象某字段取最值（如 {@code min(list, Item::getPrice)}）。</li>
     * </ul>
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
     * <ul>
     *   <li>{@code compare} 将 null 视为最大：{@code compare(null, x)} 返回正数、{@code compare(x, null)} 返回负数、</li>
     *   <li>{@code compare(null, null)} 返回 0，便于将 null 值排序到末尾。</li>
     * </ul>
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

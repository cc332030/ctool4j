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
 * <h2>能力目录</h2>
 * <ul>
 *   <li>{@code toUnmodifiableList}：收集为不可变 List</li>
 *   <li>{@code toUnmodifiableLinkedMap}：收集为不可变 LinkedHashMap（多个重载：仅键 / 键+冲突合并 / 键值 / 键值+冲突合并）</li>
 *   <li>{@code toLinkedSet}：收集为 LinkedHashSet（保持插入顺序）</li>
 *   <li>{@code toUnmodifiableSet}：收集为不可变 Set</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>收集后调用 add/put</td>
 *     <td>抛 UnsupportedOperationException（不可变）</td>
 *   </tr>
 *   <tr>
 *     <td>LinkedMap 键冲突（未提供 merge）</td>
 *     <td>抛 IllegalStateException（"Conflict key, ..."）</td>
 *   </tr>
 *   <tr>
 *     <td>LinkedMap 键冲突（提供 merge）</td>
 *     <td>按 mergeFunction 合并</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>Stream 结果需要不可变集合，避免外部误改。</li>
 *   <li>需要保持插入顺序的 Map（LinkedHashMap）或 Set（LinkedHashSet）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>键冲突默认抛异常，未显式提供 merge 时对重复键数据不友好；有重复键需传 mergeFunction。</li>
 *   <li>toUnmodifiableSet 基于 {@code Collectors.toSet()}（HashMap），元素顺序不保证。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>不可变收集为视图而非深拷贝；原集合被并发修改时行为未定义（与 JDK 收集器一致）。</li>
 *   <li>默认键冲突抛异常，强制调用方显式决策重复键策略，避免静默丢数据。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>不可变收集语义</b></p>
 * <ul>
 *   <li>{@code toUnmodifiableList} / {@code toUnmodifiableSet} / {@code toUnmodifiableLinkedMap} 通过</li>
 *   <li>{@code Collectors.collectingAndThen(..., Collections::unmodifiableXxx)} 收集后再包一层不可变视图，</li>
 *   <li>调用方后续 {@code add/put} 抛 {@code UnsupportedOperationException}。</li>
 *   <li>{@code toLinkedSet} 收集为 {@code LinkedHashSet}（可变，保持插入顺序）。</li>
 * </ul>
 * <p><b>LinkedHashMap 键冲突约定</b></p>
 * <ul>
 *   <li>未显式提供 mergeFunction 的重载，键冲突时默认抛 {@code IllegalStateException}（"Conflict key, ..."）。</li>
 *   <li>显式传入 mergeFunction 的重载，键冲突时按合并函数处理。</li>
 * </ul>
 * <p><b>键值提取</b></p>
 * <ul>
 *   <li>仅键的重载值使用 {@code Function.identity()}（元素自身为值）。</li>
 *   <li>键值重载分别通过 keyMapper/valueMapper 提取。</li>
 * </ul>
 *
 * @since 2024/4/18
 * @version 1.0
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

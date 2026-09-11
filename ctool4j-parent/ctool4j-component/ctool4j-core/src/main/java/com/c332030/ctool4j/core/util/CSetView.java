package com.c332030.ctool4j.core.util;

import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Description: CSetView，同时持有「可变 Set」与「不可变视图」的容器。
 * </p>
 *
 * <p>
 * 用于需要频繁读不可变副本、又需内部可变的场景：写方直接改可变实例，
 * 读方直接返回不可变视图（{@link Collections#unmodifiableSet} 实时视图，零构建）。
 * </p>
 *
 * <p>
 * 默认可变实例为 {@link HashSet}（非线程安全）；线程安全场景由调用方显式传入
 * 线程安全的 Set（如 {@link java.util.concurrent.ConcurrentHashMap#newKeySet()}）。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CSetView} 为「可变 Set + 不可变视图」双实例容器，实现 {@code ICView&lt;Set&lt;E&gt;&gt;}：</p>
 * <ul>
 *   <li>{@code getMutable}：获取可变实例，供写操作。</li>
 *   <li>{@code getImmutable}：获取不可变视图（{@code Collections.unmodifiableSet(可变实例)}），供读操作，零构建。</li>
 * </ul>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无参构造</td>
 *     <td>可变实例默认为 HashSet</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要「内部可变 + 外部只读视图」的 Set 持有场景。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不可变视图为实时视图（非快照），若需「固定快照」应另用副本构造。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>不可变视图为只读视图（非快照），底层可变实例被修改后视图内容随之变化。</li>
 *   <li>默认非线程安全；线程安全由调用方按需传入具体实现。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>双实例</b></p>
 * <ul>
 *   <li>构造时同时创建可变实例与不可变视图，视图包装可变实例（非快照）。</li>
 *   <li>写方直接改可变实例，读方直接返回视图，避免频繁构建不可变副本。</li>
 * </ul>
 *
 * @param <E> 元素类型
 * @since 2026/8/31
 * @version 1.0
 */
@Getter
public class CSetView<E> implements ICView<Set<E>> {

    /**
     * 可变实例
     */
    private final Set<E> mutable;

    /**
     * 不可变视图（可变实例的只读视图）
     */
    private final Set<E> immutable;

    /**
     * 构造，可变实例与不可变视图成对创建
     *
     * @param mutable 可变实例
     */
    private CSetView(Set<E> mutable) {
        this.mutable = mutable;
        this.immutable = Collections.unmodifiableSet(mutable);
    }

    /**
     * 创建视图容器，可变实例默认为 HashSet
     *
     * @param <E> 元素类型
     * @return 视图容器
     */
    public static <E> CSetView<E> of() {
        return new CSetView<>(new HashSet<>());
    }

    /**
     * 创建视图容器，指定可变实例
     *
     * @param mutable 可变实例
     * @param <E>     元素类型
     * @return 视图容器
     */
    public static <E> CSetView<E> of(Set<E> mutable) {
        return new CSetView<>(mutable);
    }

}

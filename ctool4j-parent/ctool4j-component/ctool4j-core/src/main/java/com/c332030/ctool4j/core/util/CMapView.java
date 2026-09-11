package com.c332030.ctool4j.core.util;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Description: CMapView，同时持有「可变 Map」与「不可变视图」的容器。
 * </p>
 *
 * <p>
 * 用于需要频繁读不可变副本、又需内部可变的场景：写方直接改可变实例，
 * 读方直接返回不可变视图（{@link Collections#unmodifiableMap} 实时视图，零构建）。
 * </p>
 *
 * <p>
 * 默认可变实例为 {@link HashMap}（非线程安全）；线程安全场景由调用方显式传入
 * 线程安全的 Map（如 {@link java.util.concurrent.ConcurrentHashMap}）。
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CMapView} 为「可变 Map + 不可变视图」双实例容器，用于需要频繁读不可变副本、又需内部可变的场景：</p>
 * <ul>
 *   <li>{@code getMutable}：获取可变实例，供写操作。</li>
 *   <li>{@code getImmutable}：获取不可变视图（{@code Collections.unmodifiableMap(可变实例)}），供读操作，零构建。</li>
 * </ul>
 * <p>可变实例与不可变视图成对创建，视图为可变实例的实时视图（live view），底层修改实时反映，无需每次重建不可变副本。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>无参构造</td>
 *     <td>可变实例默认为 HashMap</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要「内部可变 + 外部只读视图」的 Map 持有场景（如 MDC 上下文、配置缓存）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>不可变视图为实时视图（非快照），若需「固定快照」应另用副本构造。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>不可变视图为只读视图（非快照），底层可变实例被修改后视图内容随之变化。</li>
 *   <li>默认非线程安全；线程安全由调用方按需传入具体实现（如 ConcurrentHashMap）。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>双实例</b></p>
 * <ul>
 *   <li>构造时同时创建可变实例与不可变视图，视图包装可变实例（非快照）。</li>
 *   <li>写方直接改可变实例，读方直接返回视图，避免频繁构建不可变副本。</li>
 * </ul>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @since 2026/8/31
 * @version 1.0
 */
@Getter
public class CMapView<K, V> implements ICView<Map<K, V>> {

    /**
     * 可变实例
     */
    private final Map<K, V> mutable;

    /**
     * 不可变视图（可变实例的只读视图）
     */
    private final Map<K, V> immutable;

    /**
     * 构造，可变实例与不可变视图成对创建
     *
     * @param mutable 可变实例
     */
    private CMapView(Map<K, V> mutable) {
        this.mutable = mutable;
        this.immutable = Collections.unmodifiableMap(mutable);
    }

    /**
     * 创建视图容器，可变实例默认为 HashMap
     *
     * @param <K> 键类型
     * @param <V> 值类型
     * @return 视图容器
     */
    public static <K, V> CMapView<K, V> of() {
        return of(new HashMap<>());
    }

    /**
     * 创建视图容器，指定可变实例
     *
     * @param mutable 可变实例
     * @param <K>     键类型
     * @param <V>     值类型
     * @return 视图容器
     */
    public static <K, V> CMapView<K, V> of(Map<K, V> mutable) {
        return new CMapView<>(mutable);
    }

}

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
 * @param <K> 键类型
 * @param <V> 值类型
 * @since 2026/8/31
 * @see "doc/design/core/CMapView.adoc"
 * @see "doc/design/core/CMapViewTests.adoc"
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

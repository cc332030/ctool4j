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
 * @param <E> 元素类型
 * @since 2026/8/31
 * @see "doc/design/core/CSetView.adoc"
 * @see "doc/design/core/CSetViewTests.adoc"
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

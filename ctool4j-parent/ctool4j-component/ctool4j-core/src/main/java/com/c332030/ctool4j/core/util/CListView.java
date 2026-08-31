package com.c332030.ctool4j.core.util;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Description: CListView，同时持有「可变 List」与「不可变视图」的容器。
 * </p>
 *
 * <p>
 * 用于需要频繁读不可变副本、又需内部可变的场景：写方直接改可变实例，
 * 读方直接返回不可变视图（{@link Collections#unmodifiableList} 实时视图，零构建）。
 * </p>
 *
 * <p>
 * 默认可变实例为 {@link ArrayList}（非线程安全）；线程安全场景由调用方显式传入
 * 线程安全的 List（如 {@link java.util.concurrent.CopyOnWriteArrayList}）。
 * </p>
 *
 * @param <E> 元素类型
 * @since 2026/8/31
 * @see "doc/design/core/CListView.adoc"
 * @see "doc/design/core/CListViewTests.adoc"
 */
@Getter
public class CListView<E> implements ICView<List<E>> {

    /**
     * 可变实例
     */
    private final List<E> mutable;

    /**
     * 不可变视图（可变实例的只读视图）
     */
    private final List<E> immutable;

    /**
     * 构造，可变实例与不可变视图成对创建
     *
     * @param mutable 可变实例
     */
    private CListView(List<E> mutable) {
        this.mutable = mutable;
        this.immutable = Collections.unmodifiableList(mutable);
    }

    /**
     * 创建视图容器，可变实例默认为 ArrayList
     *
     * @param <E> 元素类型
     * @return 视图容器
     */
    public static <E> CListView<E> of() {
        return of(new ArrayList<>());
    }

    /**
     * 创建视图容器，指定可变实例
     *
     * @param mutable 可变实例
     * @param <E>     元素类型
     * @return 视图容器
     */
    public static <E> CListView<E> of(List<E> mutable) {
        return new CListView<>(mutable);
    }

}

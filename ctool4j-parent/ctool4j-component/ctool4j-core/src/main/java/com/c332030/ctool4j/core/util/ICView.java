package com.c332030.ctool4j.core.util;

/**
 * <p>
 * Description: ICView，同时提供「可变实例」与「不可变视图」的容器契约。
 * </p>
 *
 * <p>
 * 用于需要频繁读不可变副本、又需内部可变的场景：写方直接改可变实例，
 * 读方直接返回不可变视图（实时视图，零构建）。
 * </p>
 *
 * @param <T> 实例类型
 * @since 2026/8/31
 * @see "doc/design/core/ICView.adoc"
 */
public interface ICView<T> {

    /**
     * 获取可变实例
     *
     * @return 可变实例
     */
    T getMutable();

    /**
     * 获取不可变视图
     *
     * @return 不可变视图
     */
    T getImmutable();

}

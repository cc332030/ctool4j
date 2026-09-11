package com.c332030.ctool4j.core.interfaces;

import cn.hutool.core.util.TypeUtil;

/**
 * <p>
 * Description: 泛型类型解析接口
 * </p>
 *
 * <p>实现类通过 {@link #getGenericClass()} 解析自身第一个泛型实参的运行时 {@code Class}。
 * 适用于抽象基类需要拿到子类指定的泛型类型（如 {@code class XxxService extends CAbstractSessionService<XxxSession>}）的场景。</p>
 *
 * <p>实现要点：基于运行时实例 {@code getClass()} + hutool {@code TypeUtil.getTypeArgument} 解析，无需额外依赖；
 * 需子类以具体类型**直接**继承；若经过中间泛型抽象层（泛型实参仍为类型变量），可能返回 {@code TypeVariable}，
 * 强转为 {@code Class} 会在使用时抛 {@code ClassCastException}，调用方需保证直接继承。</p>
 *
 * <p>边界：静态上下文不可用；每次调用即时解析、未缓存（开销小，可按需局部缓存）。</p>
 *
 * @author c332030
 * @since 2026/9/11
 */
public interface IGenericType<T> {

    /**
     * 解析当前实现类第一个泛型实参的运行时 Class
     *
     * @return 第一个泛型实参的 Class；无法解析时返回 null
     */
    @SuppressWarnings("unchecked")
    default Class<T> getGenericClass() {
        return (Class<T>) TypeUtil.getTypeArgument(getClass(), 0);
    }

}

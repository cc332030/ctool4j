package com.c332030.ctool4j.core.cache;

/**
 * <p>
 * Description: ICClassValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICClassValue&lt;T&gt;} 为按类缓存值接口，提供 {@code get(Class&lt;?&gt;)} 获取指定类的缓存值。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要按类缓存计算结果的场景（实现类提供）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>本接口仅定义 get；需要设置值的能力用 {@code ICRefClassValue}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>作为按类缓存的抽象接口，统一取值语义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义</b></p>
 * <ul>
 *   <li>按类型关联缓存值，实现类 {@code CClassValue} 基于 {@code java.lang.ClassValue}（线程安全、按类弱关联）。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
public interface ICClassValue<T> {

    /**
     * 获取值
     * @param type 类
     * @return 值
     */
    T get(Class<?> type);

}

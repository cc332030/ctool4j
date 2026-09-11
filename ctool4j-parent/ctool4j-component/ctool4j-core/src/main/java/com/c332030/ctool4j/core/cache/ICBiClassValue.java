package com.c332030.ctool4j.core.cache;

/**
 * <p>
 * Description: ICBiClassValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICBiClassValue&lt;T&gt;} 为按双类缓存值接口，提供 {@code get(Class&lt;?&gt; type1, Class&lt;?&gt; type2)} 获取双类组合的缓存值。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要按双类组合缓存计算结果的场景（实现类提供）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>本接口仅定义 get；需要设置值的能力用 {@code ICRefBiClassValue}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>作为双类缓存的抽象接口，统一取值语义。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义</b></p>
 * <ul>
 *   <li>按类型对（type1, type2）关联缓存值，实现类 {@code CBiClassValue} 基于嵌套 {@code CClassValue}。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
public interface ICBiClassValue<T> {

    /**
     * 获取值
     * @param type1 类
     * @param type2 类2
     * @return 值
     */
    T get(Class<?> type1, Class<?> type2);

}

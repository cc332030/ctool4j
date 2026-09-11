package com.c332030.ctool4j.core.cache;

/**
 * <p>
 * Description: ICRefBiClassValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICRefBiClassValue&lt;T&gt;} 为可设置的双类缓存接口，继承 {@code ICBiClassValue&lt;T&gt;}，在 get 基础上增加 {@code set(Class&lt;?&gt; type1, Class&lt;?&gt; type2, T)}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要按双类组合缓存且运行期可覆盖值的场景（实现类提供）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅需一次性计算缓存时用 {@code ICBiClassValue}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>通过可设置接口扩展双类缓存，兼顾缓存与覆盖。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义</b></p>
 * <ul>
 *   <li>在双类缓存基础上支持覆盖值；实现类 {@code CRefBiClassValue} 基于 {@code AtomicReference} 存储。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
public interface ICRefBiClassValue <T> extends ICBiClassValue<T> {

    /**
     * 设置值
     * @param type1 类
     * @param type2 类2
     * @param value 值
     */
    void set(Class<?> type1, Class<?> type2, T value);

}

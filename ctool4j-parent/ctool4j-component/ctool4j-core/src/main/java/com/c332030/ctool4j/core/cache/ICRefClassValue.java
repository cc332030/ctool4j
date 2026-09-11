package com.c332030.ctool4j.core.cache;

/**
 * <p>
 * Description: ICRefClassValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICRefClassValue&lt;T&gt;} 为可设置的按类缓存接口，继承 {@code ICClassValue&lt;T&gt;}，在 get 基础上增加 {@code set(Class&lt;?&gt;, T)}。</p>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要按类缓存且运行期可覆盖值的场景（实现类提供）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅需一次性计算缓存时用 {@code ICClassValue}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>通过可设置接口扩展按类缓存，兼顾缓存与覆盖。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义</b></p>
 * <ul>
 *   <li>在按类缓存基础上支持覆盖值；实现类 {@code CRefClassValue} 基于 {@code AtomicReference} 存储。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
public interface ICRefClassValue<T> extends ICClassValue<T> {

    /**
     * 设置值
     * @param type 类
     * @param value 值
     */
    void set(Class<?> type, T value);

}

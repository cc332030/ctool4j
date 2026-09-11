package com.c332030.ctool4j.core.cache.impl;

import com.c332030.ctool4j.core.cache.ICRefClassValue;
import com.c332030.ctool4j.definition.function.CFunction;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Description: CRefClassValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRefClassValue&lt;T&gt;} 为可设置的按类缓存实现，实现 {@code ICRefClassValue&lt;T&gt;}，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>set 后 get</td>
 *     <td>返回覆盖后的值</td>
 *   </tr>
 *   <tr>
 *     <td>不同类 set</td>
 *     <td>互不影响，各自独立</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>按类缓存且运行期可覆盖值（如动态配置）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅需一次性计算缓存时用 {@code CClassValue}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>通过 AtomicReference 支持原子覆盖，兼顾线程安全。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>基于 AtomicReference</b></p>
 * <ul>
 *   <li>内部基于 {@code CClassValue&lt;AtomicReference&lt;T&gt;&gt;}，每个类对应一个 AtomicReference 存初始计算值。</li>
 * </ul>
 * <p><b>可覆盖</b></p>
 * <ul>
 *   <li>{@code set} 覆盖指定类的引用值，{@code get} 读取当前引用值；不同类相互独立。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
public class CRefClassValue<T> implements ICRefClassValue<T> {

    private final CClassValue<AtomicReference<T>> classValue;

    private CRefClassValue(CFunction<Class<?>, T> function) {
        classValue = CClassValue.of(type -> new AtomicReference<>(function.apply(type)));
    }

    /**
     * 获取类型缓存值
     * <ul>
     *   <li>{@code get(Class&lt;?&gt;)}：获取缓存值</li>
     * </ul>
     *
     * @param type 类型
     * @return 缓存值
     */
    @Override
    public T get(Class<?> type) {
        return classValue.get(type).get();
    }

    /**
     * 设置类型缓存值
     * <ul>
     *   <li>{@code set(Class&lt;?&gt;, T)}：覆盖缓存值</li>
     * </ul>
     *
     * @param type  类型
     * @param value 值
     */
    @Override
    public void set(Class<?> type, T value) {
        classValue.get(type).set(value);
    }

    /**
     * 创建 CRefClassValue
     * <ul>
     *   <li>{@code of(CFunction&lt;Class,T&gt;)}：创建，初始值经值函数计算</li>
     * </ul>
     *
     * @param function 值函数
     * @return CRefClassValue
     * @param <T> 值泛型
     */
    public static <T> CRefClassValue<T> of(CFunction<Class<?>, T> function) {
        return new CRefClassValue<>(function);
    }

}

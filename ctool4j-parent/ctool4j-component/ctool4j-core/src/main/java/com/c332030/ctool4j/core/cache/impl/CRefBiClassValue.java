package com.c332030.ctool4j.core.cache.impl;

import com.c332030.ctool4j.core.cache.ICRefBiClassValue;
import com.c332030.ctool4j.definition.function.CBiFunction;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Description: CRefBiClassValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CRefBiClassValue&lt;T&gt;} 为可设置的双类缓存实现，实现 {@code ICRefBiClassValue&lt;T&gt;}，提供：</p>
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
 *     <td>不同组合 set</td>
 *     <td>互不影响</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>双类组合缓存且运行期可覆盖值。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅需一次性计算缓存时用 {@code CBiClassValue}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>通过 AtomicReference 支持原子覆盖，兼顾线程安全。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>嵌套 AtomicReference</b></p>
 * <ul>
 *   <li>基于 {@code CBiClassValue&lt;AtomicReference&lt;T&gt;&gt;}：每个双类组合对应一个 AtomicReference 存初始计算值。</li>
 * </ul>
 * <p><b>可覆盖</b></p>
 * <ul>
 *   <li>{@code set} 覆盖指定组合的引用值，{@code get} 读取当前值；不同组合相互独立。</li>
 * </ul>
 *
 * @since 2025/11/21
 * @version 1.0
 */
public class CRefBiClassValue<T> implements ICRefBiClassValue<T> {

    private final CBiClassValue<AtomicReference<T>> classValue;

    private CRefBiClassValue(CBiFunction<Class<?>, Class<?>, T> function) {
        classValue = CBiClassValue.of((type1, type2) -> new AtomicReference<>(function.apply(type1, type2)));
    }

    /**
     * 获取双类型缓存值
     * <ul>
     *   <li>{@code get(type1, type2)}：获取双类组合缓存值</li>
     * </ul>
     *
     * @param type1 类型 1
     * @param type2 类型 2
     * @return 缓存值
     */
    @Override
    public T get(Class<?> type1, Class<?> type2) {
        return classValue.get(type1, type2).get();
    }

    /**
     * 设置双类型缓存值
     * <ul>
     *   <li>{@code set(type1, type2, T)}：覆盖双类组合缓存值</li>
     * </ul>
     *
     * @param type1 类型 1
     * @param type2 类型 2
     * @param value 值
     */
    @Override
    public void set(Class<?> type1, Class<?> type2, T value) {
        classValue.get(type1, type2).set(value);
    }

    /**
     * 创建 CRefBiClassValue
     * <ul>
     *   <li>{@code of(CBiFunction&lt;Class,Class,T&gt;)}：创建，初始值经值函数计算</li>
     * </ul>
     *
     * @param function 值函数
     * @return CRefBiClassValue
     * @param <T> 值泛型
     */
    public static <T> CRefBiClassValue<T> of(CBiFunction<Class<?>, Class<?>, T> function) {
        return new CRefBiClassValue<>(function);
    }

}

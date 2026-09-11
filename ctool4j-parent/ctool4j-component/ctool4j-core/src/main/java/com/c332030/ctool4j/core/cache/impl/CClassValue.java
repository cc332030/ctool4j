package com.c332030.ctool4j.core.cache.impl;

import com.c332030.ctool4j.core.cache.ICClassValue;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.NonNull;

/**
 * <p>
 * Description: CClassValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CClassValue&lt;T&gt;} 为按类缓存值实现，实现 {@code ICClassValue&lt;T&gt;}，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>同一类多次 get</td>
 *     <td>值函数只执行一次（缓存）</td>
 *   </tr>
 *   <tr>
 *     <td>remove 后 get</td>
 *     <td>重新计算</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>按类缓存计算结果（如反射元数据、类相关配置），避免重复计算。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要运行期覆盖缓存值时用 {@code CRefClassValue}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>基于 ClassValue 弱关联，类卸载时缓存自动释放，无内存泄漏。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>基于 ClassValue</b></p>
 * <ul>
 *   <li>内部委托 {@code java.lang.ClassValue}：线程安全、按类弱关联（类可被 GC），{@code computeValue} 经值函数计算。</li>
 * </ul>
 * <p><b>惰性计算与缓存</b></p>
 * <ul>
 *   <li>首次 get 经 {@code computeValue} 计算并缓存，后续 get 直接返回缓存值（同一类只计算一次）。</li>
 *   <li>不同类各自独立计算缓存。</li>
 * </ul>
 *
 * @since 2025/11/20
 * @version 1.0
 */
public class CClassValue<T> implements ICClassValue<T> {

    private final ClassValue<T> classValue;

    private CClassValue(CFunction<Class<?>, T> function) {
        classValue = new ClassValue<T>() {
            /**
             * 通过值函数计算指定类的值并缓存
             *
             * @param type 类
             * @return 计算得到的值
             */
            @Override
            protected T computeValue(@NonNull Class<?> type) {
                return function.apply(type);
            }
        };
    }

    /**
     * 获取值
     * <ul>
     *   <li>{@code get(Class&lt;?&gt;)}：获取指定类缓存值（首次经值函数计算并缓存）</li>
     * </ul>
     *
     * @param clazz 类
     * @return 值
     */
    @Override
    public T get(Class<?> clazz) {
        return classValue.get(clazz);
    }

    /**
     * 移除缓存值，下次 get 时重新计算
     * <ul>
     *   <li>{@code remove(Class&lt;?&gt;)}：移除缓存值，下次 get 重新计算</li>
     * </ul>
     *
     * @param clazz 类
     */
    public void remove(Class<?> clazz) {
        classValue.remove(clazz);
    }

    /**
     * 创建 CClassValue
     * <ul>
     *   <li>{@code of(CFunction&lt;Class&lt;?&gt;,T&gt;)}：创建，值函数按类惰性计算</li>
     * </ul>
     *
     * @param function 值函数
     * @return CClassValue
     * @param <T> 值泛型
     */
    public static <T> CClassValue<T> of(CFunction<Class<?>, T> function) {
        return new CClassValue<>(function);
    }

}

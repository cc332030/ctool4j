package com.c332030.ctool4j.core.cache.impl;

import com.c332030.ctool4j.core.cache.ICBiClassValue;
import com.c332030.ctool4j.definition.function.CBiFunction;

/**
 * <p>
 * Description: CBiClassValue
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBiClassValue&lt;T&gt;} 为按双类缓存值实现，实现 {@code ICBiClassValue&lt;T&gt;}，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>同一组合多次 get</td>
 *     <td>值函数只执行一次</td>
 *   </tr>
 *   <tr>
 *     <td>不同组合</td>
 *     <td>各自独立缓存</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>按双类组合缓存计算结果（如两个类间的映射元数据）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>需要运行期覆盖缓存值时用 {@code CRefBiClassValue}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>嵌套缓存实现，按组合去重，避免重复计算。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>嵌套实现</b></p>
 * <ul>
 *   <li>基于 {@code CClassValue&lt;CClassValue&lt;T&gt;&gt;}：外层按 type1 缓存，内层按 type2 缓存，形成二维组合缓存。</li>
 * </ul>
 * <p><b>惰性计算与缓存</b></p>
 * <ul>
 *   <li>首次 get 计算并缓存；同一组合只计算一次；不同组合独立缓存。</li>
 * </ul>
 *
 * @since 2025/11/20
 * @version 1.0
 */
public class CBiClassValue<T> implements ICBiClassValue<T> {

    private final CClassValue<CClassValue<T>> classValue;

    private CBiClassValue(CBiFunction<Class<?>, Class<?>, T> function) {
        classValue = CClassValue.of(type1 ->
                CClassValue.of(type2 ->
                        function.apply(type1, type2)));
    }

    /**
     * 获取值
     * <ul>
     *   <li>{@code get(type1, type2)}：获取双类组合缓存值（首次计算并缓存）</li>
     * </ul>
     *
     * @param type1 类1
     * @param type2 类2
     * @return 值
     */
    @Override
    public T get(Class<?> type1, Class<?> type2) {
        return classValue.get(type1).get(type2);
    }

    /**
     * 移除缓存值，下次 get 时重新计算
     * <ul>
     *   <li>{@code remove(type1, type2)}：移除缓存值</li>
     * </ul>
     *
     * @param type1 类1
     * @param type2 类2
     */
    public void remove(Class<?> type1, Class<?> type2) {
        classValue.get(type1).remove(type2);
    }

    /**
     * 创建 CBiClassValue
     * <ul>
     *   <li>{@code of(CBiFunction&lt;Class,Class,T&gt;)}：创建，值函数按双类组合惰性计算</li>
     * </ul>
     *
     * @param function 值函数
     * @return CClassValue
     * @param <T> 值泛型
     */
    public static <T> CBiClassValue<T> of(CBiFunction<Class<?>, Class<?>, T> function) {
        return new CBiClassValue<>(function);
    }

}

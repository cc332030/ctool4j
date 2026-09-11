package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.classes.CBeanUtils;

/**
 * <p>
 * Description: ICopy
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code ICopy} 为对象拷贝接口，提供默认拷贝方法：</p>
 * <ul>
 *   <li>{@code copyTo(T t)}：拷贝到指定实例（属性覆盖到 t）</li>
 *   <li>{@code copyTo(Class&lt;T&gt; tClass)}：拷贝成指定类型新实例</li>
 * </ul>
 * <p>均委托 {@code CBeanUtils.copy}。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>copyTo(Class)</td>
 *     <td>创建目标类型实例并拷贝</td>
 *   </tr>
 *   <tr>
 *     <td>copyTo(instance)</td>
 *     <td>拷贝到指定实例</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要便捷拷贝能力的对象（DTO/Entity 转换），实现 {@code ICopy} 即获得拷贝语义。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖 CBeanUtils 拷贝规则（同名属性）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>统一委托 CBeanUtils，保证拷贝规则一致。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>委托语义</b></p>
 * <ul>
 *   <li>{@code copyTo(Class)}：{@code CBeanUtils.copy(this, tClass)} 创建目标类型新实例并拷贝同名字段。</li>
 *   <li>{@code copyTo(instance)}：{@code CBeanUtils.copy(this, t)} 将本对象属性拷贝到传入实例。</li>
 * </ul>
 *
 * @since 2025/11/6
 * @version 1.0
 */
public interface ICopy {

    /**
     * 拷贝成指定类型的对象
     * @param t 目标对象
     * @return 目标对象
     * @param <T> 目标对象类型
     */
    default <T> T copyTo(T t) {
        return CBeanUtils.copy(this, t);
    }

    /**
     * 拷贝成指定类型的对象
     * @param tClass 目标对象类型
     * @return 目标对象
     * @param <T> 目标对象类型
     */
    default <T> T copyTo(Class<T> tClass) {
        return CBeanUtils.copy(this, tClass);
    }

}

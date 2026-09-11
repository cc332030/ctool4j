package com.c332030.ctool4j.core.util;

import cn.hutool.core.util.BooleanUtil;
import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CBoolUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CBoolUtils} 为布尔判断工具类，提供布尔值（含 null）的四种判断：</p>
 * <ul>
 *   <li>{@code isTrue} / {@code isNotTrue}：是否等于 true / 是否不等于 true</li>
 *   <li>{@code isFalse} / {@code isNotFalse}：是否等于 false / 是否不等于 false</li>
 * </ul>
 * <p>并提供对象经取值函数后做布尔判断的重载（{@code isTrue(T, function)} 等）。</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>布尔入参为 null</td>
 *     <td>视为 false（isTrue/isFalse 返回 false）</td>
 *   </tr>
 *   <tr>
 *     <td>对象入参为 null（重载）</td>
 *     <td>取值为 null 默认值，视为 false</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>对可能为 null 的布尔值做统一判断，避免手写 null 判空。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>区分——若需区分"null"与"false"应直接比较 {@code Boolean.FALSE.equals(value)}。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>null 视为 false 与 hutool {@code BooleanUtil} 语义一致，统一处理避免分散判断。</li>
 *   <li>{@code isNotTrue}/{@code isNotFalse} 为便捷方法，避免调用方自行取反产生误用。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>语义约定（null 处理）</b></p>
 * <ul>
 *   <li>null 布尔值视为 false：{@code isTrue(null)} 返回 false、{@code isFalse(null)} 返回 false、{@code isNotTrue(null)}</li>
 *   <li>{@code isNotTrue} = {@code !isTrue}，{@code isNotFalse} = {@code !isFalse}，均基于对应的正向判断取反，语义自洽。</li>
 * </ul>
 * <p><b>实现方式</b></p>
 * <ul>
 *   <li>基础判断委托 hutool {@code BooleanUtil.isTrue/isFalse}（其 null 视为 false 的语义一致），</li>
 *   <li>{@code isNotTrue/isNotFalse} 对正向判断取反。</li>
 *   <li>对象重载经 {@code CObjUtils.convert(t, function)} 取值（t 为 null 时得到 null 默认值），再走同一</li>
 *   <li>布尔判断，保证 null 语义与基础判断一致。</li>
 * </ul>
 *
 * @since 2025/12/22
 * @version 1.0
 */
@UtilityClass
public class CBoolUtils {

    /**
     * 判断布尔值是否为 true
     * <ul>
     *   <li>对对象某属性取值后做布尔判断（如 {@code isTrue(user, User::isEnabled)}）。</li>
     * </ul>
     *
     * @param value 布尔值
     * @return 是否为 true，null 视为 false
     */
    public boolean isTrue(Boolean value) {
        return BooleanUtil.isTrue(value);
    }

    /**
     * 判断布尔值是否不为 true
     *
     * @param value 布尔值
     * @return 是否不为 true
     */
    public boolean isNotTrue(Boolean value) {
        return !isTrue(value);
    }

    /**
     * 判断布尔值是否为 false
     * <ul>
     *   <li>{@code isFalse(null)} 返回 false（null 视为 false，而非"是 false"），需注意 null 与显式 false 的语义</li>
     * </ul>
     *
     * @param value 布尔值
     * @return 是否为 false，null 视为 false
     */
    public boolean isFalse(Boolean value){
        return BooleanUtil.isFalse(value);
    }

    /**
     * 判断布尔值是否不为 false
     * <ul>
     *   <li>返回 true、{@code isNotFalse(null)} 返回 true。</li>
     * </ul>
     *
     * @param value 布尔值
     * @return 是否不为 false
     */
    public boolean isNotFalse(Boolean value){
        return !isFalse(value);
    }

    /**
     * 通过函数取值后判断是否为 true
     *
     * @param t        对象
     * @param function 取值函数
     * @param <T>      对象类型
     * @return 是否为 true
     */
    public <T> boolean isTrue(T t, CFunction<T, Boolean> function) {
        val value = CObjUtils.convert(t, function);
        return BooleanUtil.isTrue(value);
    }

    /**
     * 通过函数取值后判断是否不为 true
     *
     * @param t        对象
     * @param function 取值函数
     * @param <T>      对象类型
     * @return 是否不为 true
     */
    public <T> boolean isNotTrue(T t, CFunction<T, Boolean> function) {
        return !isTrue(t, function);
    }

    /**
     * 通过函数取值后判断是否为 false
     *
     * @param t        对象
     * @param function 取值函数
     * @param <T>      对象类型
     * @return 是否为 false
     */
    public <T> boolean isFalse(T t, CFunction<T, Boolean> function) {
        val value = CObjUtils.convert(t, function);
        return BooleanUtil.isFalse(value);
    }

    /**
     * 通过函数取值后判断是否不为 false
     *
     * @param t        对象
     * @param function 取值函数
     * @param <T>      对象类型
     * @return 是否不为 false
     */
    public <T> boolean isNotFalse(T t, CFunction<T, Boolean> function) {
        return !isFalse(t, function);
    }

}

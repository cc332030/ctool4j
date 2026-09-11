package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.core.classes.CObjUtils;
import com.c332030.ctool4j.definition.function.CConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import com.c332030.ctool4j.definition.interfaces.ICValue;
import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * <p>
 * Description: CValueUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CValueUtils} 为取值工具类，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>对象重载：obj 为 null 或函数返回 null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>getValue(ICValue)：枚举为 null</td>
 *     <td>返回 null</td>
 *   </tr>
 *   <tr>
 *     <td>setValue：枚举为 null</td>
 *     <td>不调用 consumer</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>对实现 {@code ICValue&lt;T&gt;} 的枚举/值对象统一取值，避免逐处判空。</li>
 *   <li>枚举值消费（非空才执行）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>仅支持 {@code ICValue} 接口语义；非 ICValue 取值用 {@code CObjUtils.convert} 或原生函数式取值。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>null 语义统一为"返回 null / 跳过"，调用方据此可安全链式操作。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>null 语义</b></p>
 * <ul>
 *   <li>{@code setValue}：枚举为 null 时不调用 consumer（无操作）。</li>
 * </ul>
 *
 * @since 2025/12/4
 * @version 1.0
 */
@UtilityClass
public class CValueUtils {

    /**
     * 通过函数取值后获取枚举值
     * <ul>
     *   <li>{@code getValue(E obj, CFunction&lt;E, ICValue&lt;T&gt;&gt; function)}：对象经取值函数获取枚举值（ICValue）</li>
     *   <li>{@code getValue(ICValue&lt;T&gt; iValue)}：从枚举值对象获取其值</li>
     *   <li>{@code getValue(E, function)}：对象或函数结果为 null 时返回 null（经 {@code CObjUtils.convert}）。</li>
     *   <li>{@code getValue(ICValue)}：枚举为 null 时返回 null。</li>
     *   <li>对象重载先经 {@code CObjUtils.convert(obj, function)} 取到 ICValue，再委托 {@code getValue(ICValue)}。</li>
     * </ul>
     *
     * @param obj      对象
     * @param function 取值函数
     * @param <E>      对象类型
     * @param <T>      枚举值类型
     * @return 枚举值，对象或函数结果为 null 时返回 null
     */
    public <E, T> T getValue(E obj, CFunction<E, ICValue<T>> function) {
        val iValue = CObjUtils.convert(obj, function);
        return getValue(iValue);
    }

    /**
     * 获取枚举值
     *
     * @param iValue 枚举
     * @param <T>    枚举值类型
     * @return 枚举值，枚举为 null 时返回 null
     */
    public <T> T getValue(ICValue<T> iValue) {
        return CObjUtils.convert(iValue, ICValue::getValue);
    }

    /**
     * 枚举值消费（枚举为 null 时跳过）
     * <ul>
     *   <li>{@code setValue(ICValue&lt;T&gt; iValue, CConsumer&lt;T&gt; consumer)}：枚举值消费（枚举为 null 时跳过）</li>
     * </ul>
     *
     * @param iValue   枚举
     * @param consumer 消费函数
     * @param <T>      枚举值类型
     */
    public <T> void setValue(ICValue<T> iValue, CConsumer<T> consumer) {
        if(null != iValue) {
            consumer.accept(iValue.getValue());
        }
    }

}

package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.reflect.Field;

/**
 * <p>
 * Description: CLambdaUtils
 * </p>
 *
 * <h2>能力目录</h2>
 * <p>{@code CLambdaUtils} 为字段 Lambda 工具类，提供：</p>
 * <h2>兜底设计</h2>
 * <table border="1">
 *   <caption>兜底行为</caption>
 *   <tr>
 *     <th>场景</th>
 *     <th>兜底行为</th>
 *   </tr>
 *   <tr>
 *     <td>基本类型字段 get/set</td>
 *     <td>经 MethodHandle 自动装箱/拆箱</td>
 *   </tr>
 * </table>
 * <h2>适用范围</h2>
 * <ul>
 *   <li>需要字段级函数式读取/写入（配合流式/链式处理）。</li>
 * </ul>
 * <h2>不适用与边界场景</h2>
 * <ul>
 *   <li>依赖反射 Field，需字段可访问（setAccessible 语义）。</li>
 * </ul>
 * <h2>已知限制与取舍</h2>
 * <ul>
 *   <li>委托 MethodHandle 保证 JDK 8 兼容且避免直接反射开销；基本类型经装箱处理。</li>
 * </ul>
 * <h2>设计要点</h2>
 * <p><b>MethodHandle 实现</b></p>
 * <ul>
 *   <li>JDK 8 的 {@code LambdaMetafactory} 不支持 getField/putField 直接方法句柄（抛</li>
 *   <li>"Unsupported MethodHandle kind: getField"），故委托 {@code MethodHandle.invoke} 调用。</li>
 *   <li>使用 {@code CMethodHandleUtils#getGetterHandleAsType}/{@code getSetterHandleAsType}（setAccessible 语义，</li>
 *   <li>不受跨包访问级别限制），每次调用做参数适配与装箱（JDK 8 兼容性取舍，避免直接反射 Field.get）。</li>
 * </ul>
 *
 * @author c332030
 * @since 2025/12/20
 * @version 1.0
 */
@UtilityClass
public class CLambdaUtils {

    /**
     * 获取字段读取 Lambda
     * <p>JDK 8 的 LambdaMetafactory 不支持 getField/putField 类直接方法句柄
     * （抛 "Unsupported MethodHandle kind: getField"），故委托 MethodHandle.invoke 调用，
     * 每次调用做参数适配与装箱（JDK 8 兼容性取舍，避免直接反射 Field.get）。
     * 使用 {@link CMethodHandleUtils#getGetterHandleAsType}（setAccessible 语义，不受跨包访问级别限制）</p>
     * <ul>
     *   <li>{@code getFieldGetLambda(Field)}：获取字段读取 Lambda（CFunction&lt;Object,Object&gt;）</li>
     * </ul>
     *
     * @param field 字段
     * @return 字段读取 Lambda
     */
    public CFunction<Object, Object> getFieldGetLambda(Field field) {
        val handle = CMethodHandleUtils.getGetterHandleAsType(field);
        return handle::invoke;
    }

    /**
     * 获取字段写入 Lambda
     * <p>同 {@link #getFieldGetLambda}，委托 MethodHandle.invoke 调用</p>
     * <ul>
     *   <li>{@code getFieldSetLambda(Field)}：获取字段写入 Lambda（CBiConsumer&lt;Object,Object&gt;）</li>
     * </ul>
     *
     * @param field 字段
     * @return 字段写入 Lambda
     */
    public CBiConsumer<Object, Object> getFieldSetLambda(Field field) {
        val handle = CMethodHandleUtils.getSetterHandleAsType(field);
        return handle::invoke;
    }

}

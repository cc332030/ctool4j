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
 * @author c332030
 * @since 2025/12/20
 */
@UtilityClass
public class CLambdaUtils {

    /**
     * 获取字段读取 Lambda
     * <p>JDK 8 的 LambdaMetafactory 不支持 getField/putField 类直接方法句柄
     * （抛 "Unsupported MethodHandle kind: getField"），故委托 MethodHandle.invoke 调用，
     * 每次调用做参数适配与装箱（JDK 8 兼容性取舍，避免直接反射 Field.get）。
     * 使用 {@link CMethodHandleUtils#getGetterHandleAsType}（setAccessible 语义，不受跨包访问级别限制）</p>
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
     *
     * @param field 字段
     * @return 字段写入 Lambda
     */
    public CBiConsumer<Object, Object> getFieldSetLambda(Field field) {
        val handle = CMethodHandleUtils.getSetterHandleAsType(field);
        return handle::invoke;
    }

}

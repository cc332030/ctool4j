package com.c332030.ctool4j.core.classes;

import com.c332030.ctool4j.definition.function.CBiConsumer;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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
     * JDK 9+需注意模块访问权限，这里兼容JDK 8
     */
    final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * 获取指定类的 Lookup
     *
     * @param clazz 类
     * @return Lookup
     */
    public MethodHandles.Lookup getLookup(Class<?> clazz) {
        return LOOKUP.in(clazz);
    }

    /**
     * 获取字段 getter 方法句柄
     *
     * @param clazz 类
     * @param field 字段
     * @return getter 方法句柄
     */
    @SneakyThrows
    public MethodHandle getGetterMethodHandle(Class<?> clazz, Field field) {
        return getLookup(clazz).unreflectGetter(field);
    }

    /**
     * 获取字段 setter 方法句柄
     *
     * @param clazz 类
     * @param field 字段
     * @return setter 方法句柄
     */
    @SneakyThrows
    public MethodHandle getSetterMethodHandle(Class<?> clazz, Field field) {
        return getLookup(clazz).unreflectSetter(field);
    }

    /**
     * 获取字段读取 Lambda
     * <p>JDK 8 的 LambdaMetafactory 不支持 getField/putField 类直接方法句柄
     * （抛 "Unsupported MethodHandle kind: getField"），故委托 MethodHandle.invoke 调用，
     * 每次调用做参数适配与装箱（JDK 8 兼容性取舍，避免直接反射 Field.get）</p>
     *
     * @param clazz 类
     * @param field 字段
     * @return 字段读取 Lambda
     */
    public CFunction<Object, Object> getFieldGetLambda(Class<?> clazz, Field field) {
        val handle = getGetterMethodHandle(clazz, field);
        return handle::invoke;
    }

    /**
     * 获取字段写入 Lambda
     * <p>同 {@link #getFieldGetLambda}，委托 MethodHandle.invoke 调用</p>
     *
     * @param clazz 类
     * @param field 字段
     * @return 字段写入 Lambda
     */
    public CBiConsumer<Object, Object> getFieldSetLambda(Class<?> clazz, Field field) {
        val handle = getSetterMethodHandle(clazz, field);
        return handle::invoke;
    }

}

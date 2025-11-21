package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.function.CFunction;

/**
 * <p>
 * Description: CClassValue
 * </p>
 *
 * @since 2025/11/20
 */
public class CClassValue<T> {

    private final ClassValue<T> classValue;

    protected CClassValue(CFunction<Class<?>, T> function) {
        classValue = CClassUtils.getClassValue(function);
    }

    /**
     * 获取值
     * @param clazz 类
     * @return 值
     */
    public T get(Class<?> clazz) {
        return classValue.get(clazz);
    }

    /**
     * 创建 CClassValue
     * @param function 值函数
     * @return CClassValue
     * @param <T> 值泛型
     */
    public static <T> CClassValue<T> of(CFunction<Class<?>, T> function) {
        return new CClassValue<>(function);
    }

}

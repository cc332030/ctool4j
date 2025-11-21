package com.c332030.ctool4j.core.cache.impl;

import com.c332030.ctool4j.core.cache.impl.ICClassValue;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.NonNull;

/**
 * <p>
 * Description: CClassValue
 * </p>
 *
 * @since 2025/11/20
 */
public class CClassValue<T> implements ICClassValue<T> {

    private final ClassValue<T> classValue;

    private CClassValue(CFunction<Class<?>, T> function) {
        classValue = new ClassValue<T>() {
            @Override
            protected T computeValue(@NonNull Class<?> type) {
                return function.apply(type);
            }
        };
    }

    /**
     * 获取值
     * @param clazz 类
     * @return 值
     */
    @Override
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

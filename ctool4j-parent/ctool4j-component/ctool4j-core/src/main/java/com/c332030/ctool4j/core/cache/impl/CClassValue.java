package com.c332030.ctool4j.core.cache.impl;

import com.c332030.ctool4j.core.cache.ICClassValue;
import com.c332030.ctool4j.definition.function.CFunction;
import lombok.NonNull;

/**
 * <p>
 * Description: CClassValue
 * </p>
 *
 * @since 2025/11/20
 * @see doc/design/core/CClassValue.adoc
 * @see doc/design/core/CClassValueTests.adoc
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
     * @param clazz 类
     * @return 值
     */
    @Override
    public T get(Class<?> clazz) {
        return classValue.get(clazz);
    }

    /**
     * 移除缓存值，下次 get 时重新计算
     * @param clazz 类
     */
    public void remove(Class<?> clazz) {
        classValue.remove(clazz);
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

package com.c332030.ctool4j.core.cache.impl;

import com.c332030.ctool4j.core.cache.ICRefClassValue;
import com.c332030.ctool4j.definition.function.CFunction;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Description: CRefClassValue
 * </p>
 *
 * @since 2025/11/21
 * @see "doc/design/core/CRefClassValue.adoc"
 * @see "doc/design/core/CRefClassValueTests.adoc"
 */
public class CRefClassValue<T> implements ICRefClassValue<T> {

    private final CClassValue<AtomicReference<T>> classValue;

    private CRefClassValue(CFunction<Class<?>, T> function) {
        classValue = CClassValue.of(type -> new AtomicReference<>(function.apply(type)));
    }

    /**
     * 获取类型缓存值
     *
     * @param type 类型
     * @return 缓存值
     */
    @Override
    public T get(Class<?> type) {
        return classValue.get(type).get();
    }

    /**
     * 设置类型缓存值
     *
     * @param type  类型
     * @param value 值
     */
    @Override
    public void set(Class<?> type, T value) {
        classValue.get(type).set(value);
    }

    /**
     * 创建 CRefClassValue
     * @param function 值函数
     * @return CRefClassValue
     * @param <T> 值泛型
     */
    public static <T> CRefClassValue<T> of(CFunction<Class<?>, T> function) {
        return new CRefClassValue<>(function);
    }

}

package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.function.CFunction;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Description: CRefClassValue
 * </p>
 *
 * @since 2025/11/21
 */
public class CRefClassValue<T> extends CClassValue<AtomicReference<T>> {

    protected CRefClassValue(CFunction<Class<?>, T> function) {
        super(type -> new AtomicReference<>(function.apply(type)));
    }

    public T getValue(Class<?> type) {
        return get(type).get();
    }

    public void setValue(Class<?> type, T value) {
        get(type).set(value);
    }

    /**
     * 创建 CRefClassValue
     * @param function 值函数
     * @return CRefClassValue
     * @param <T> 值泛型
     */
    public static <T> CRefClassValue<T> ofRef(CFunction<Class<?>, T> function) {
        return new CRefClassValue<>(function);
    }

}

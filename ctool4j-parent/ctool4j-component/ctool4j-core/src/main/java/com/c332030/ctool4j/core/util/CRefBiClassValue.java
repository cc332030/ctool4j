package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.function.CBiFunction;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>
 * Description: CRefBiClassValue
 * </p>
 *
 * @since 2025/11/21
 */
public class CRefBiClassValue<T> extends CBiClassValue<AtomicReference<T>> {

    protected CRefBiClassValue(CBiFunction<Class<?>, Class<?>, T> function) {
        super((type1, type2) -> new AtomicReference<>(function.apply(type1, type2)));
    }

    public T getValue(Class<?> type1, Class<?> type2) {
        return get(type1, type2).get();
    }

    public void setValue(Class<?> type1, Class<?> type2, T value) {
        get(type1, type2).set(value);
    }

    /**
     * 创建 CRefBiClassValue
     * @param function 值函数
     * @return CRefBiClassValue
     * @param <T> 值泛型
     */
    public static <T> CRefBiClassValue<T> ofRef(CBiFunction<Class<?>, Class<?>, T> function) {
        return new CRefBiClassValue<>(function);
    }

}

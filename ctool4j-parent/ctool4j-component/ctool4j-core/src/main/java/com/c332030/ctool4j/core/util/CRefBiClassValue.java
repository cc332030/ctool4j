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
public class CRefBiClassValue<T> {

    private final CBiClassValue<AtomicReference<T>> classValue;

    private CRefBiClassValue(CBiFunction<Class<?>, Class<?>, T> function) {
        classValue = CBiClassValue.of((type1, type2) -> new AtomicReference<>(function.apply(type1, type2)));
    }

    public T get(Class<?> type1, Class<?> type2) {
        return classValue.get(type1, type2).get();
    }

    public void set(Class<?> type1, Class<?> type2, T value) {
        classValue.get(type1, type2).set(value);
    }

    /**
     * 创建 CRefBiClassValue
     * @param function 值函数
     * @return CRefBiClassValue
     * @param <T> 值泛型
     */
    public static <T> CRefBiClassValue<T> of(CBiFunction<Class<?>, Class<?>, T> function) {
        return new CRefBiClassValue<>(function);
    }

}

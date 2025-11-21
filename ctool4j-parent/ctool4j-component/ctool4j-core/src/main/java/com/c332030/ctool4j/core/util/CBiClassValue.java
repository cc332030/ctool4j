package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.function.CBiFunction;
import org.springframework.lang.NonNull;

/**
 * <p>
 * Description: CBiClassValue
 * </p>
 *
 * @since 2025/11/20
 */
public class CBiClassValue<T> extends ClassValue<CClassValue<T>>{

    private final CBiFunction<Class<?>, Class<?>, T> function;

    protected CBiClassValue(CBiFunction<Class<?>, Class<?>, T> function) {
        this.function = function;
    }

    @Override
    protected CClassValue<T> computeValue(@NonNull Class<?> type1) {
        return CClassValue.of(type2 -> function.apply(type1, type2));
    }

    public T get(Class<?> type1, Class<?> type2) {
        return get(type1).get(type2);
    }

    /**
     * 创建 CBiClassValue
     * @param function 值函数
     * @return CClassValue
     * @param <T> 值泛型
     */
    public static <T> CBiClassValue<T> of(CBiFunction<Class<?>, Class<?>, T> function) {
        return new CBiClassValue<>(function);
    }

}

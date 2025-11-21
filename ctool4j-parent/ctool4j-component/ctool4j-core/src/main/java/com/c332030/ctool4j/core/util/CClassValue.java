package com.c332030.ctool4j.core.util;

import com.c332030.ctool4j.definition.function.CFunction;
import org.springframework.lang.NonNull;

/**
 * <p>
 * Description: CClassValue
 * </p>
 *
 * @since 2025/11/20
 */
public class CClassValue<T> extends ClassValue<T> {

    private final CFunction<Class<?>, T> function;

    protected CClassValue(CFunction<Class<?>, T> function) {
        this.function = function;
    }

    @Override
    protected T computeValue(@NonNull Class<?> type) {
        return function.apply(type);
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

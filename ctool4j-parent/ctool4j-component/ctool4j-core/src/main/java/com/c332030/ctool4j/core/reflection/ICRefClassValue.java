package com.c332030.ctool4j.core.reflection;

/**
 * <p>
 * Description: ICRefClassValue
 * </p>
 *
 * @since 2025/11/21
 */
public interface ICRefClassValue<T> extends ICClassValue<T>{

    /**
     * 设置值
     * @param type 类
     * @param value 值
     */
    void set(Class<?> type, T value);

}

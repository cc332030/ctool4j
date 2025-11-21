package com.c332030.ctool4j.core.util.reflection;

/**
 * <p>
 * Description: ICRefBiClassValue
 * </p>
 *
 * @since 2025/11/21
 */
public interface ICRefBiClassValue <T> extends ICBiClassValue<T> {

    /**
     * 设置值
     * @param type1 类
     * @param type2 类2
     */
    void set(Class<?> type1, Class<?> type2, T value);

}

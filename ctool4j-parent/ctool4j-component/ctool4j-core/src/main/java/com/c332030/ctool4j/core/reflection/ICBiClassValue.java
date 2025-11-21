package com.c332030.ctool4j.core.reflection;

/**
 * <p>
 * Description: ICBiClassValue
 * </p>
 *
 * @since 2025/11/21
 */
public interface ICBiClassValue<T> {

    /**
     * 获取值
     * @param type1 类
     * @param type2 类2
     * @return 值
     */
    T get(Class<?> type1, Class<?> type2);

}

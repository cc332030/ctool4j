package com.c332030.ctool4j.core.cache;

/**
 * <p>
 * Description: ICClassValue
 * </p>
 *
 * @since 2025/11/21
 */
public interface ICClassValue<T> {

    /**
     * 获取值
     * @param type 类
     * @return 值
     */
    T get(Class<?> type);

}

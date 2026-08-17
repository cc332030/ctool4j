package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICValue
 * </p>
 *
 * @since 2025/9/11
 */
public interface ICValue<T> {

    /**
     * value 字段名常量
     */
    String VALUE = "value";

    /**
     * 获取值
     * @return 值
     */
    T getValue();

}

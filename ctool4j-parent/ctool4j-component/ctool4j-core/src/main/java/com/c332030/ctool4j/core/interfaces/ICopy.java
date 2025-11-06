package com.c332030.ctool4j.core.interfaces;

import com.c332030.ctool4j.core.util.CBeanUtils;

/**
 * <p>
 * Description: ICopy
 * </p>
 *
 * @since 2025/11/6
 */
public interface ICopy {

    /**
     * 拷贝成指定类型的对象
     * @param t 目标对象
     * @return 目标对象
     * @param <T> 目标对象类型
     */
    default <T> T copy(T t) {
        return CBeanUtils.copy(this, t);
    }

    /**
     * 拷贝成指定类型的对象
     * @param tClass 目标对象类型
     * @return 目标对象
     * @param <T> 目标对象类型
     */
    default <T> T copy(Class<T> tClass) {
        return CBeanUtils.copy(this, tClass);
    }

}

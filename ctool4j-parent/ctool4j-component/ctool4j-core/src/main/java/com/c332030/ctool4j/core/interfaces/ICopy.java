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

    default <T> T copy(Class<T> tClass) {
        return CBeanUtils.copy(this, tClass);
    }

}

package com.c332030.core.annotation;

import java.io.Serializable;

/**
 * <p>
 * Description: ICEnum
 * </p>
 *
 * @since 2025/9/11
 */
public interface ICEnum<T extends Serializable> {

    T getValue();

}

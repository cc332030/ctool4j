package com.c332030.ctool4j.definition.interfaces;

/**
 * <p>
 * Description: ICError
 * </p>
 *
 * @since 2025/10/24
 */
public interface ICError<T> {

    T getErrorCode();

    String getErrorMsg();

}

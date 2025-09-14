package com.c332030.core.model;

/**
 * <p>
 * Description: ICResult
 * </p>
 *
 * @since 2025/2/24
 */
public interface ICResult<C, T> {

    C getCode();

    String getMessage();

    T getData();

}
